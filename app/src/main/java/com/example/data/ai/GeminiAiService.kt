package com.example.data.ai

import android.content.Context
import com.example.BuildConfig
import com.example.data.model.BrandIdentityResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.UUID

/** AI helper backed by the free FOKOF2 Cloudflare worker (Workers AI text); local rules are the offline fallback. */
class GeminiAiService(private val appContext: Context? = null) {

    private fun deviceId(): String {
        val ctx = appContext ?: return "fokof2-noctx-device"
        val prefs = ctx.getSharedPreferences("fokof2_ai", Context.MODE_PRIVATE)
        val existing = prefs.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", created).apply()
        return created
    }

    suspend fun generateCopy(
        topic: String,
        targetAudience: String,
        tone: String,
        toolType: String,
        isArabic: Boolean
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = if (isArabic) {
            "أنت كاتب إعلانات مصري محترف. اكتب دايماً بالعامية المصرية الخفيفة (زي: دلوقتي، عايز، كده، يلا، ماتفوتش، بسعر ينفع)، بأسلوب جذاب ومقنع، من غير أي لهجة خليجية أو شامية. استخدم إيموجي بشكل خفيف واختم بعبارة دعوة للشراء وهاشتاجات."
        } else {
            "You are the Chief Copywriter & Digital Marketing Strategist for FOKOF2. Write compelling, high-converting copy with clear CTA."
        }

        val prompt = if (isArabic) {
            "المطلوب: كتابة ($toolType) حول الموضوع: ($topic)، موجه إلى: ($targetAudience)، بنبرة: ($tone). قدم نصاً متكاملاً مع عبارة دعوة لاتخاذ إجراء قوية وهاشتاجات ذات صلة."
        } else {
            "Task: Generate ($toolType) for product/topic: ($topic), targeting: ($targetAudience), tone: ($tone). Include punchy hooks, value proposition, CTA, and relevant hashtags."
        }

        val remoteResult = callGeminiApi(systemPrompt, prompt)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult
        }

        // Smart procedural fallback for instant offline/keyless reliability
        fallbackMarketingCopy(topic, targetAudience, tone, toolType, isArabic)
    }

    suspend fun enhancePrompt(rawPrompt: String, isArabic: Boolean): String = withContext(Dispatchers.IO) {
        val systemPrompt = "You are a world-class prompt engineer for creative image generation and graphic design."
        val prompt = "Enhance the following creative prompt for graphic design, adding details on cinematic studio lighting, luxurious gold (#D4AF37) accents, elegant textures, composition, and high-end aesthetic:\n\"$rawPrompt\"\nKeep the enhanced prompt concise (under 50 words)."

        val remoteResult = callGeminiApi(systemPrompt, prompt)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult
        }

        if (isArabic) {
            "$rawPrompt، بلمسات ذهبية فاخرة (#D4AF37)، إضاءة استوديو سينمائية ناعمة، تفاصيل ثلاثية الأبعاد متقنة وخلفية فخمة بأعلى دقة 8K"
        } else {
            "$rawPrompt, cinematic studio lighting, polished luxury gold (#D4AF37) accents, volumetric glow, ultra-detailed 8k resolution, elegant modern composition"
        }
    }

    suspend fun chatWithDirector(
        history: List<Pair<Boolean, String>>,
        userMessage: String,
        isArabic: Boolean
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = if (isArabic) {
            "أنت المساعد الإبداعي الذكي لمنصة 'FOKOF2'. شعارنا: 'أفكارك تتحول إلى تصاميم قبل أن تتكلم'. أنت مستشار فني خبير في التصميم الجرافيكي، تناغم الألوان، الخطوط العربية، وتصميم الهويات البصرية والشعارات. أجب بأسلوب راقٍ، ملهم، عملي، ومختصر ومباشر."
        } else {
            "You are the FOKOF2 creative helper. Provide concise, practical design advice on color harmony, Arabic/Latin typography, branding, and layouts. Answer briefly and practically in the user's language."
        }

        val remoteResult = callGeminiApi(systemPrompt, userMessage)
        if (!remoteResult.isNullOrBlank()) {
            return@withContext remoteResult
        }

        fallbackChatResponse(userMessage, isArabic)
    }

    suspend fun generateBrandKit(
        brandName: String,
        industry: String,
        isArabic: Boolean
    ): BrandIdentityResult = withContext(Dispatchers.IO) {
        val systemPrompt = "You are an elite brand identity designer. Return a JSON object with: sloganAr, sloganEn, primaryColorHex, secondaryColorHex, accentColorHex, darkBaseHex, lightSurfaceHex, fontHeading, fontBody, brandVoiceAr, brandVoiceEn, logoConceptAr, logoConceptEn."
        val prompt = "Create a luxury brand identity for brand '$brandName' in industry '$industry'. Return JSON only."

        val remoteJson = callGeminiApi(systemPrompt, prompt)
        if (!remoteJson.isNullOrBlank()) {
            try {
                val cleaned = remoteJson.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleaned}")
                return@withContext BrandIdentityResult(
                    brandName = brandName,
                    industry = industry,
                    sloganAr = json.optString("sloganAr", "$brandName: عنوان التميز والإبداع"),
                    sloganEn = json.optString("sloganEn", "$brandName: The Pinnacle of Excellence"),
                    primaryColorHex = json.optString("primaryColorHex", "#D4AF37"),
                    secondaryColorHex = json.optString("secondaryColorHex", "#E5C158"),
                    accentColorHex = json.optString("accentColorHex", "#B89726"),
                    darkBaseHex = json.optString("darkBaseHex", "#111116"),
                    lightSurfaceHex = json.optString("lightSurfaceHex", "#FDFBF7"),
                    fontHeading = json.optString("fontHeading", "Cairo Bold / Cinzel"),
                    fontBody = json.optString("fontBody", "Cairo Regular"),
                    brandVoiceAr = json.optString("brandVoiceAr", "ملهم، فاخر، احترافي، يركز على الأصالة والحداثة"),
                    brandVoiceEn = json.optString("brandVoiceEn", "Inspiring, prestigious, modern, and trustworthy"),
                    logoConceptAr = json.optString("logoConceptAr", "شعار يدمج الحرف الأول مع عنصر رمزي من $industry بلمسات ذهبية عصرية"),
                    logoConceptEn = json.optString("logoConceptEn", "Minimalist monogram with symbolic motif of $industry embossed in gold")
                )
            } catch (e: Exception) {
                // fall back to procedural
            }
        }

        // Procedural creative kit
        BrandIdentityResult(
            brandName = brandName,
            industry = industry,
            sloganAr = "$brandName – أفق جديد من التميز والإبداع",
            sloganEn = "$brandName – Redefining Modern Excellence",
            primaryColorHex = "#D4AF37",
            secondaryColorHex = "#E5C158",
            accentColorHex = "#2A2A38",
            darkBaseHex = "#0E0E12",
            lightSurfaceHex = "#FAF8F2",
            fontHeading = "Cairo Bold & Cinzel",
            fontBody = "Cairo Regular",
            brandVoiceAr = "فخم، استثنائي، ذو حضور قوي في قطاع $industry",
            brandVoiceEn = "Sophisticated, authoritative, visionary for the $industry sector",
            logoConceptAr = "حرف متداخل مستوحى من $brandName مع لمعان ذهبي متدرج يعكس الموثوقية والرقي.",
            logoConceptEn = "Interlocking monogram of $brandName with metallic gold foil gradient over rich matte black."
        )
    }

    /** Brand name ideas (Egyptian/Arabic friendly). Free text AI first, local list as fallback. */
    suspend fun suggestBrandNames(industry: String, keywords: String): List<String> = withContext(Dispatchers.IO) {
        val sys = "أنت خبير تسمية علامات تجارية مصري. اقترح أسماء قصيرة وسهلة النطق ومناسبة للسوق المصري والعربي."
        val user = "اقترح 8 أسماء لعلامة تجارية في مجال ($industry) والكلمات المفتاحية ($keywords). اكتب كل اسم في سطر منفصل بدون ترقيم أو شرح."
        val remote = callGeminiApi(sys, user)
        val parsed = remote?.lines()?.map { it.trim().trimStart('-', '*', '•', ' ').replace(Regex("^[0-9٠-٩]+[.)-]\\s*"), "").trim() }
            ?.filter { it.isNotBlank() && it.length <= 30 }?.take(8).orEmpty()
        if (parsed.size >= 4) return@withContext parsed
        val base = keywords.trim().ifBlank { industry.trim() }.ifBlank { "براند" }
        listOf("$base بلس", "لمسة $base", "$base هاوس", "دار $base", "$base ستوديو", "نجمة $base", "$base برو", "ركن $base")
    }

    /** Smart reply: drafts a business reply to a customer message. Remote free AI first, local template as fallback. */
    suspend fun draftSmartReply(businessInfo: String, customerMessage: String, isArabic: Boolean): String = withContext(Dispatchers.IO) {
        val systemPrompt = if (isArabic) {
            "أنت مساعد خدمة عملاء محترف لنشاط تجاري مصري. اكتب ردًا مهذبًا ومختصرًا ومقنعًا بالعامية المصرية المحترمة على رسالة العميل، مناسبًا للإرسال مباشرة في واتساب أو ماسنجر. التزم بمعلومات النشاط المعطاة ولا تخترع التزامات غير موجودة فيها."
        } else {
            "You are a professional customer-service assistant for a small business. Write a polite, concise reply to the customer's message, ready to send as-is. Stick to the provided business info; never invent commitments."
        }
        val userPrompt = (if (isArabic) "معلومات النشاط: " else "Business info: ") + businessInfo.ifBlank { "-" } +
            (if (isArabic) "\nرسالة العميل: " else "\nCustomer message: ") + customerMessage
        val remote = callGeminiApi(systemPrompt, userPrompt)
        if (!remote.isNullOrBlank()) return@withContext remote
        if (isArabic) {
            "أهلًا بحضرتك، شكرًا لتواصلك معنا.\nاستلمنا رسالتك وهنراجعها ونرد عليك بكل التفاصيل في أقرب وقت.\nلو عايز تضيف أي تفاصيل تانية ابعتها هنا واحنا في الخدمة."
        } else {
            "Hello, thank you for reaching out.\nWe received your message and will get back to you with all the details shortly. Feel free to add anything else here."
        }
    }

    /** Free text AI through the FOKOF2 Cloudflare worker (Workers AI, no key on device). Null on any failure so callers use local fallbacks. */
    private fun callGeminiApi(systemPrompt: String, userPrompt: String): String? {
        return try {
            if (BuildConfig.AI_IMAGE_WORKER_URL.isBlank() || BuildConfig.AI_IMAGE_APP_SECRET.isBlank()) return null
            val url = URL(BuildConfig.AI_IMAGE_WORKER_URL.trimEnd('/') + "/chat")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("User-Agent", "FOKOF2-Android/1.2")
            val body = JSONObject().apply {
                put("secret", BuildConfig.AI_IMAGE_APP_SECRET)
                put("device_id", deviceId())
                put("system", systemPrompt)
                put("prompt", userPrompt)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode != 200) return null
            val raw = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val json = JSONObject(raw)
            if (!json.optBoolean("ok")) return null
            val text = json.optString("text").trim()
            if (text.length < 2) null else text
        } catch (e: Exception) {
            null
        }
    }

    private fun fallbackMarketingCopy(
        topic: String,
        targetAudience: String,
        tone: String,
        toolType: String,
        isArabic: Boolean
    ): String {
        return if (isArabic) {
            """
            🌟 إعلان استثنائي لـ: $topic
            
            هل تبحث عن التميز الذي يخطف الأنظار؟
            نقدم لـ ($targetAudience) تجربة لا مثيل لها تجمع بين الفخامة والابتكار المتقن.
            
            ✨ لماذا تختارنا؟
            • تصميم فريد يلهم عملائك قبل أن تتكلم.
            • جودة متناهية بلمسة احترافية تعزز مكانتك في السوق.
            • نتائج ملموسة تعكس هوية مشروعك الحقيقية.
            
            🏷️ عرض حصري لفترة محدودة: احصل على ميزتك التنافسية اليوم!
            
            📲 تواصل معنا الآن وابدأ رحلة التألق!
            
            #$topic #FOKOF2 #تصميم_احترافي #تسويق #ريادة_الأعمال
            """.trimIndent()
        } else {
            """
            Elevate Your Vision: $topic
            
            Designed exclusively for $targetAudience who demand perfection.
            
            Why It Stands Out:
            ✦ Iconic aesthetic with unmatched precision
            ✦ Tailored to capture attention before you even speak
            ✦ Engineered for performance, prestige, and growth
            
            Exclusive Offer: Unlock your full potential today.
            
            Take action now & redefine what is possible!
            
            #$topic #FOKOF2 #LuxuryDesign #CreativeMarketing #Innovation
            """.trimIndent()
        }
    }

    private fun fallbackChatResponse(query: String, isArabic: Boolean): String {
        val q = query.lowercase()
        return if (isArabic) {
            when {
                q.contains("لون") || q.contains("الوان") ->
                    "نوصي في تصاميم FOKOF2 باعتماد قاعدة 60-30-10: 60% للأسود الداكن الفاخر (#0E0E12) لخلق العمق، 30% للرمادي الدافئ أو اللؤلؤي للنصوص والمساحات، و10% لبريق الذهب الملكي (#D4AF37) لزر الدعوة لاتخاذ الإجراء والأشكال المميزة!"
                q.contains("خط") || q.contains("فونط") ->
                    "تناغم الخطوط هو روح التصميم! ننصحك بدمج خط 'Cairo' للأوزان العريضة في العناوين والترويسات مع خط نسخ أنيق أو أوزان خفيفة للنصوص الشارحة لضمان القراءة السلسة والمظهر العصري."
                q.contains("شعار") || q.contains("لوجو") ->
                    "الشعار الناجح يتميز بثلاثة شروط: البساطة القصوى، التميز في ذاكرة العميل، وقابلية التكيف بحجم صغير وكبير. استخدم رمزاً هندسياً أو مونوغراماً ذهبياً واضح المعالم."
                else ->
                    "أهلاً بك في FOKOF2! يمكنك تخصيص القوالب في الاستوديو أو إنشاء معاينة تصميم محلية. تذكّر أن توليد الصور الفعلي غير مفعّل في هذه النسخة."
            }
        } else {
            when {
                q.contains("color") ->
                    "In FOKOF2 luxury layouts, we recommend the 60-30-10 rule: 60% Obsidian Black (#0E0E12) for grounding, 30% Pearl/Warm Gray for readability, and 10% Royal Gold (#D4AF37) for accent highlights and high-converting CTAs!"
                q.contains("font") || q.contains("typography") ->
                    "Typography pairing is vital: pair bold Cairo or Cinzel for hero headlines with clean geometric weights for body text to maintain visual hierarchy."
                q.contains("logo") ->
                    "An iconic logo must be simple, memorable, and scalable. A clean geometric monogram with gold foil styling always elevates brand authority."
                else ->
                    "Welcome to FOKOF2! You can customize templates in the Studio or create a local design preview. Actual AI image generation is not enabled in this build."
            }
        }
    }
}
