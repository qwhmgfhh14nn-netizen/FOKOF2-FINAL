package com.example.data.ai

import android.content.Context
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Calls the FOKOF2 image proxy (Cloudflare Worker in front of Workers AI) so the provider key
 * stays server-side. The worker enforces per-device daily limits and caches repeated prompts.
 * When the worker URL/secret are not baked into the build, [isConfigured] is false and callers
 * fall back to the keyless Pollinations path. Any proxy failure also falls back, so the user
 * always gets an answer; the daily limits only cap the shared free quota.
 */
object AiImageProxy {

    sealed class Result {
        data class Ok(val fileUri: String, val cached: Boolean, val remaining: Int?) : Result()
        data class Fail(val reason: String, val code: String?) : Result()
    }

    fun isConfigured(): Boolean =
        BuildConfig.AI_IMAGE_WORKER_URL.isNotBlank() && BuildConfig.AI_IMAGE_APP_SECRET.isNotBlank()

    private fun deviceId(context: Context): String {
        val prefs = context.getSharedPreferences("fokof2_ai", Context.MODE_PRIVATE)
        val existing = prefs.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", created).apply()
        return created
    }

    suspend fun generate(context: Context, prompt: String, width: Int = 1024, height: Int = 1024, hd: Boolean = false): Result =
        withContext(Dispatchers.IO) {
            val endpoint = BuildConfig.AI_IMAGE_WORKER_URL.trimEnd('/') + "/generate"
            var lastReason = "تعذر الاتصال بخدمة الصور"
            var lastCode: String? = null
            repeat(2) { attempt ->
                try {
                    val conn = URL(endpoint).openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 20000
                    conn.readTimeout = 120000
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    val body = JSONObject().apply {
                        put("secret", BuildConfig.AI_IMAGE_APP_SECRET)
                        put("device_id", deviceId(context))
                        put("prompt", prompt)
                        put("width", width)
                        put("height", height)
                        put("hd", hd)
                    }
                    conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                    val status = conn.responseCode
                    val text = (if (status in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val json = runCatching { JSONObject(text) }.getOrNull()
                    if (status == 200 && json != null && json.optBoolean("ok")) {
                        val b64 = json.optString("image")
                        if (b64.isNotBlank()) {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            if (bytes.size > 2000) {
                                val dir = File(context.cacheDir, "gen").apply { mkdirs() }
                                val f = File(dir, "img_${System.currentTimeMillis()}.jpg")
                                f.writeBytes(bytes)
                                return@withContext Result.Ok(
                                    android.net.Uri.fromFile(f).toString(),
                                    json.optBoolean("cache"),
                                    if (json.has("remaining")) json.optInt("remaining") else null
                                )
                            }
                        }
                        lastReason = "الخدمة رجّعت رد غير صالح"
                    } else {
                        lastCode = json?.optString("code")?.takeIf { it.isNotBlank() }
                        lastReason = when (lastCode) {
                            "rate_limited" -> "وصلت للحد المجاني بتاع النهاردة"
                            "quota_exhausted" -> "الخدمة المجانية وصلت لحدها النهاردة"
                            "unauthorized" -> "إعداد الخدمة ناقص"
                            else -> "خطأ من الخدمة ($status)"
                        }
                        // Daily-limit answers are final; retrying only wastes time.
                        if (lastCode == "rate_limited" || lastCode == "quota_exhausted" || lastCode == "unauthorized") {
                            return@withContext Result.Fail(lastReason, lastCode)
                        }
                        if (attempt == 0) delay(2000)
                    }
                } catch (e: Exception) {
                    lastReason = "مفيش اتصال بالإنترنت أو الخدمة بطيئة"
                    if (attempt == 0) delay(2000)
                }
            }
            Result.Fail(lastReason, lastCode)
        }
}
