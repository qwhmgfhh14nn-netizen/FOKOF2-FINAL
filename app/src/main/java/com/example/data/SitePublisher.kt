package com.example.data

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Publishes a generated site to the free FOKOF2 Cloudflare Worker and returns a public link. */
object SitePublisher {
    private const val BASE = "https://fokof2-api.qwhmgfhh14nn.workers.dev"

    class Result(val ok: Boolean, val url: String?, val error: String?)

    fun savedUrl(context: Context, siteId: String): String? =
        context.getSharedPreferences("published_sites", Context.MODE_PRIVATE).getString("url_$siteId", null)

    fun publish(context: Context, siteId: String, slug: String, title: String, html: String): Result {
        return try {
            val prefs = context.getSharedPreferences("published_sites", Context.MODE_PRIVATE)
            val body = JSONObject()
            body.put("title", title)
            body.put("html", html)
            val savedSlug = prefs.getString("slug_$siteId", null)
            val savedKey = prefs.getString("key_$siteId", null)
            if (savedSlug != null && savedKey != null) {
                body.put("slug", savedSlug); body.put("key", savedKey)
            } else {
                val clean = slug.lowercase().replace(Regex("[^a-z0-9-]"), "").take(30)
                if (clean.length >= 3) body.put("slug", clean)
            }
            val conn = URL("$BASE/sites").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true
            conn.setRequestProperty("content-type", "application/json; charset=utf-8")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            val json = JSONObject(text)
            if (json.optBoolean("ok")) {
                val url = json.getString("url")
                val newKey = json.optString("key", "")
                prefs.edit()
                    .putString("slug_$siteId", json.getString("slug"))
                    .apply { if (newKey.isNotEmpty()) putString("key_$siteId", newKey) }
                    .putString("url_$siteId", url)
                    .apply()
                Result(true, url, null)
            } else Result(false, null, json.optString("error", "http_$code"))
        } catch (e: Exception) {
            Result(false, null, e.message ?: "network_error")
        }
    }
}
