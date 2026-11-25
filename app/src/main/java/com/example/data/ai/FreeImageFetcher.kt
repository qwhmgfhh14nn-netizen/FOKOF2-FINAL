package com.example.data.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads a free generated image (Pollinations) with the rate limit in mind: the free tier allows
 * 1 image per ~45 s per IP and answers HTTP 402 otherwise. Requests are serialized, retried after the
 * advertised wait, and the bytes are cached on disk so the UI never shows a silently broken image.
 */
object FreeImageFetcher {
    private val lock = Mutex()
    private var lastStart = 0L
    private const val MIN_GAP_MS = 46_000L

    sealed class Result {
        data class Ok(val fileUri: String) : Result()
        data class Fail(val reason: String) : Result()
    }

    suspend fun fetch(context: Context, url: String, onWaiting: (Int) -> Unit = {}): Result = lock.withLock {
        withContext(Dispatchers.IO) {
            var lastReason = "تعذر الاتصال بخدمة الصور"
            repeat(4) { attempt ->
                val wait = lastStart + MIN_GAP_MS - System.currentTimeMillis()
                if (wait > 0 && lastStart != 0L && attempt == 0) {
                    waitWithCountdown(wait, onWaiting)
                }
                lastStart = System.currentTimeMillis()
                try {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 20000
                    conn.readTimeout = 90000
                    val code = conn.responseCode
                    if (code == 200) {
                        val bytes = conn.inputStream.use { it.readBytes() }
                        val type = conn.contentType.orEmpty()
                        if (bytes.size > 2000 && type.startsWith("image")) {
                            val dir = File(context.cacheDir, "gen").apply { mkdirs() }
                            val f = File(dir, "img_${System.currentTimeMillis()}.jpg")
                            f.writeBytes(bytes)
                            return@withContext Result.Ok(android.net.Uri.fromFile(f).toString())
                        }
                        lastReason = "الخدمة رجّعت رد غير صالح"
                    } else {
                        val err = runCatching { conn.errorStream?.bufferedReader()?.readText().orEmpty() }.getOrDefault("")
                        val secs = Regex("Retry after (\\d+)s").find(err)?.groupValues?.get(1)?.toLongOrNull()
                        lastReason = if (code == 402 || code == 429) "الخدمة المجانية مشغولة، جاري إعادة المحاولة" else "خطأ من الخدمة ($code)"
                        if (code == 402 || code == 429) {
                            waitWithCountdown(((secs ?: 45L) + 2L) * 1000L, onWaiting)
                        } else {
                            delay(3000)
                        }
                    }
                } catch (e: Exception) {
                    lastReason = "مفيش اتصال بالإنترنت أو الخدمة بطيئة"
                    delay(3000)
                }
            }
            Result.Fail(lastReason)
        }
    }

    private suspend fun waitWithCountdown(ms: Long, onWaiting: (Int) -> Unit) {
        var left = ms
        while (left > 0) {
            onWaiting((left / 1000).toInt() + 1)
            val step = minOf(1000L, left)
            delay(step)
            left -= step
        }
        onWaiting(0)
    }
}
