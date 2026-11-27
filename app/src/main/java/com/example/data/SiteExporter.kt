package com.example.data

import android.content.Context
import android.util.Base64
import com.example.data.model.SiteExtras
import com.example.data.model.SiteTestimonial
import com.example.data.model.UserWebsite
import com.example.data.model.WebsiteFeatureToggles
import com.example.data.model.WebsiteItem
import com.example.ui.screens.generateStandaloneHtml
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Website builder phase 1: real ZIP export (index.html + style.css + script.js + image files
 * + site.json settings + README) built from the same generator used for publishing, and
 * compatible-project import. The exported site is static and runs on any static host.
 */
object SiteExporter {

    class ZipResult(val file: File, val entryNames: List<String>, val imageCount: Int)

    private val DATA_URI = Regex("data:image/(jpeg|jpg|png|webp);base64,([A-Za-z0-9+/=]+)")
    private val STYLE_BLOCK = Regex("<style>(.*?)</style>", RegexOption.DOT_MATCHES_ALL)
    private val SCRIPT_BLOCK = Regex("<script>(.*?)</script>", RegexOption.DOT_MATCHES_ALL)

    /** Builds the full site ZIP into the app cache and returns it, or null on failure.
     *  Multi-page sites (extras.pages.size > 1) export one HTML file per page: index.html is the
     *  home page and page-2.html, page-3.html ... are the extra pages, all linked in the nav. */
    fun buildZip(context: Context, site: UserWebsite): ZipResult? {
        return try {
            val entries = LinkedHashMap<String, ByteArray>()
            var imgN = 0
            // 1) Images: decode each embedded data URI into a real file, rewrite references.
            //    All html files and img/ sit at the ZIP root, so "img/..." resolves everywhere.
            val seen = HashMap<String, String>()
            fun processImages(html: String): String = DATA_URI.replace(html) { m ->
                val ext = when (m.groupValues[1]) { "png" -> "png"; "webp" -> "webp"; else -> "jpg" }
                val key = m.groupValues[2].take(96)
                seen.getOrPut(key) {
                    val bytes = Base64.decode(m.groupValues[2], Base64.DEFAULT)
                    imgN++
                    val name = "img/img-%02d.%s".format(imgN, ext)
                    entries[name] = bytes
                    name
                }
            }

            val pages = site.extras.pages
            val multi = pages.size > 1
            val pageFiles: List<Pair<String, String>> = if (multi) {
                val rel = com.example.ui.screens.relativePageLinks(site)
                pages.indices.map { i ->
                    com.example.ui.screens.pageFileName(i) to processImages(com.example.ui.screens.generateSitePageHtml(site, context, i, rel))
                }
            } else {
                listOf("index.html" to processImages(generateStandaloneHtml(site, context)))
            }

            // 2) CSS: move the home page's inline <style> block into style.css (all pages share the design)
            var firstHtml = pageFiles[0].second
            STYLE_BLOCK.find(firstHtml)?.let { m ->
                entries["style.css"] = m.groupValues[1].trim().toByteArray(Charsets.UTF_8)
                firstHtml = firstHtml.replaceRange(m.range, "<link rel=\"stylesheet\" href=\"style.css\">")
            }

            // 3) JS: move the home page's last plain inline <script> into script.js
            SCRIPT_BLOCK.findAll(firstHtml).lastOrNull()?.let { m ->
                entries["script.js"] = m.groupValues[1].trim().toByteArray(Charsets.UTF_8)
                firstHtml = firstHtml.replaceRange(m.range, "<script src=\"script.js\"></script>")
            }

            entries[pageFiles[0].first] = firstHtml.toByteArray(Charsets.UTF_8)
            pageFiles.drop(1).forEach { (name, html) -> entries[name] = html.toByteArray(Charsets.UTF_8) }
            entries["site.json"] = toJson(site).toString(2).toByteArray(Charsets.UTF_8)
            entries["README.md"] = readme(site).toByteArray(Charsets.UTF_8)

            val outDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val safeSlug = site.siteSlug.ifBlank { "site" }.replace(Regex("[^a-zA-Z0-9-]"), "").ifBlank { "site" }
            val out = File(outDir, "$safeSlug.zip")
            ZipOutputStream(out.outputStream().buffered()).use { z ->
                for ((name, bytes) in entries) {
                    z.putNextEntry(ZipEntry(name))
                    z.write(bytes)
                    z.closeEntry()
                }
            }
            ZipResult(out, entries.keys.toList(), imgN)
        } catch (e: Exception) {
            null
        }
    }

    /** Serializes a site project so it can be re-imported later (spec section 12). */
    fun toJson(site: UserWebsite): JSONObject {
        val o = JSONObject()
        o.put("fokof2Site", 1)
        o.put("templateId", site.templateId)
        o.put("siteTitle", site.siteTitle)
        o.put("siteSlug", site.siteSlug)
        o.put("businessType", site.businessType)
        o.put("headline", site.headline)
        o.put("subheadline", site.subheadline)
        o.put("aboutText", site.aboutText)
        o.put("whatsappNumber", site.whatsappNumber)
        o.put("phoneNumber", site.phoneNumber)
        o.put("address", site.address)
        o.put("workingHours", site.workingHours)
        o.put("primaryColorHex", site.primaryColorHex)
        o.put("isPublished", site.isPublished)
        val t = JSONObject()
        t.put("featureWhatsappOrders", site.featureToggles.featureWhatsappOrders)
        t.put("featureBookingSystem", site.featureToggles.featureBookingSystem)
        t.put("featureProductCatalog", site.featureToggles.featureProductCatalog)
        t.put("featureCustomerReviews", site.featureToggles.featureCustomerReviews)
        t.put("featureFaqSection", site.featureToggles.featureFaqSection)
        t.put("featureGoogleMaps", site.featureToggles.featureGoogleMaps)
        t.put("featureDirectCalling", site.featureToggles.featureDirectCalling)
        t.put("featureCustomDomain", site.featureToggles.featureCustomDomain)
        o.put("featureToggles", t)
        val items = JSONArray()
        site.items.forEach { i ->
            val io = JSONObject()
            io.put("nameAr", i.nameAr); io.put("nameEn", i.nameEn)
            io.put("priceEgp", i.priceEgp); io.put("originalPriceEgp", i.originalPriceEgp)
            io.put("descriptionAr", i.descriptionAr); io.put("category", i.category)
            io.put("badgeAr", i.badgeAr ?: ""); io.put("isAvailable", i.isAvailable)
            io.put("imagePath", i.imagePath)
            items.put(io)
        }
        o.put("items", items)
        val ex = JSONObject()
        ex.put("heroImage", site.extras.heroImage)
        ex.put("announcement", site.extras.announcement)
        ex.put("theme", site.extras.theme)
        ex.put("font", site.extras.font)
        ex.put("hideFooter", site.extras.hideFooter)
        ex.put("facebook", site.extras.facebook); ex.put("instagram", site.extras.instagram)
        ex.put("tiktok", site.extras.tiktok); ex.put("youtube", site.extras.youtube)
        ex.put("telegram", site.extras.telegram); ex.put("website", site.extras.website)
        ex.put("gallery", JSONArray(site.extras.gallery))
        val revs = JSONArray()
        site.extras.testimonials.forEach { r ->
            val ro = JSONObject()
            ro.put("name", r.name); ro.put("text", r.text); ro.put("stars", r.stars)
            revs.put(ro)
        }
        ex.put("testimonials", revs)
        val pgs = JSONArray()
        site.extras.pages.forEach { p ->
            val po = JSONObject()
            po.put("id", p.id); po.put("title", p.title)
            val secs = JSONArray()
            p.sections.forEach { s ->
                val so = JSONObject()
                so.put("type", s.type); so.put("text", s.text)
                secs.put(so)
            }
            po.put("sections", secs)
            pgs.put(po)
        }
        ex.put("pages", pgs)
        ex.put("logoImage", site.extras.logoImage)
        ex.put("couponCode", site.extras.couponCode); ex.put("couponPercent", site.extras.couponPercent)
        val cfs = JSONArray()
        site.extras.customFields.forEach { f ->
            val fo = JSONObject()
            fo.put("label", f.label); fo.put("kind", f.kind)
            cfs.put(fo)
        }
        ex.put("customFields", cfs)
        o.put("extras", ex)
        return o
    }

    /** Parses a site.json produced by [toJson]. Returns null when the file is not a FOKOF2 project. */
    fun parseSiteJson(text: String): UserWebsite? {
        return try {
            val o = JSONObject(text)
            if (o.optInt("fokof2Site") != 1) return null
            val t = o.optJSONObject("featureToggles") ?: JSONObject()
            val toggles = WebsiteFeatureToggles(
                featureWhatsappOrders = t.optBoolean("featureWhatsappOrders", true),
                featureBookingSystem = t.optBoolean("featureBookingSystem", false),
                featureProductCatalog = t.optBoolean("featureProductCatalog", true),
                featureCustomerReviews = t.optBoolean("featureCustomerReviews", true),
                featureFaqSection = t.optBoolean("featureFaqSection", true),
                featureGoogleMaps = t.optBoolean("featureGoogleMaps", true),
                featureDirectCalling = t.optBoolean("featureDirectCalling", true),
                featureCustomDomain = t.optBoolean("featureCustomDomain", false)
            )
            val itemsArr = o.optJSONArray("items") ?: JSONArray()
            val items = (0 until itemsArr.length()).map { i ->
                val io = itemsArr.getJSONObject(i)
                WebsiteItem(
                    nameAr = io.optString("nameAr"),
                    nameEn = io.optString("nameEn"),
                    priceEgp = io.optInt("priceEgp"),
                    originalPriceEgp = io.optInt("originalPriceEgp"),
                    descriptionAr = io.optString("descriptionAr"),
                    category = io.optString("category", "عام"),
                    badgeAr = io.optString("badgeAr").ifBlank { null },
                    isAvailable = io.optBoolean("isAvailable", true),
                    imagePath = io.optString("imagePath")
                )
            }
            val exo = o.optJSONObject("extras") ?: JSONObject()
            val galleryArr = exo.optJSONArray("gallery") ?: JSONArray()
            val revsArr = exo.optJSONArray("testimonials") ?: JSONArray()
            val extras = SiteExtras(
                heroImage = exo.optString("heroImage"),
                announcement = exo.optString("announcement"),
                theme = exo.optInt("theme"),
                font = exo.optInt("font"),
                hideFooter = exo.optBoolean("hideFooter", false),
                facebook = exo.optString("facebook"), instagram = exo.optString("instagram"),
                tiktok = exo.optString("tiktok"), youtube = exo.optString("youtube"),
                telegram = exo.optString("telegram"), website = exo.optString("website"),
                gallery = (0 until galleryArr.length()).map { galleryArr.getString(it) },
                testimonials = (0 until revsArr.length()).map {
                    val ro = revsArr.getJSONObject(it)
                    SiteTestimonial(ro.optString("name"), ro.optString("text"), ro.optInt("stars", 5))
                },
                pages = run {
                    val pgsArr = exo.optJSONArray("pages") ?: JSONArray()
                    (0 until pgsArr.length()).map {
                        val po = pgsArr.getJSONObject(it)
                        val secsArr = po.optJSONArray("sections") ?: JSONArray()
                        com.example.data.model.SitePage(
                            id = po.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                            title = po.optString("title", "صفحة"),
                            sections = (0 until secsArr.length()).map { k ->
                                val so = secsArr.getJSONObject(k)
                                com.example.data.model.PageSection(so.optString("type", "text"), so.optString("text"))
                            }
                        )
                    }
                },
                logoImage = exo.optString("logoImage"),
                couponCode = exo.optString("couponCode"), couponPercent = exo.optInt("couponPercent", 0),
                customFields = run {
                    val cfArr = exo.optJSONArray("customFields") ?: JSONArray()
                    (0 until cfArr.length()).map {
                        val fo = cfArr.getJSONObject(it)
                        com.example.data.model.SiteFormField(fo.optString("label"), fo.optString("kind", "text"))
                    }
                }
            )
            UserWebsite(
                templateId = o.optString("templateId", "imported"),
                siteTitle = o.optString("siteTitle"),
                siteSlug = o.optString("siteSlug").ifBlank { "my-site" },
                businessType = o.optString("businessType", "AGENCY"),
                headline = o.optString("headline"),
                subheadline = o.optString("subheadline"),
                aboutText = o.optString("aboutText"),
                whatsappNumber = o.optString("whatsappNumber"),
                phoneNumber = o.optString("phoneNumber"),
                address = o.optString("address"),
                workingHours = o.optString("workingHours"),
                primaryColorHex = o.optString("primaryColorHex", "#D4AF37"),
                items = items,
                featureToggles = toggles,
                isPublished = o.optBoolean("isPublished", true),
                extras = extras
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun readme(site: UserWebsite): String {
        val pageLines = if (site.extras.pages.size > 1)
            site.extras.pages.mapIndexed { i, p -> "- " + com.example.ui.screens.pageFileName(i) + " — صفحة \"" + p.title + "\"." }.joinToString("\n")
        else "- index.html — صفحة الموقع."
        val multiNote = if (site.extras.pages.size > 1)
            "- موقعك متعدد الصفحات: افتح index.html وستجد روابط باقي الصفحات في القائمة العلوية. ارفع كل الملفات معاً في نفس المجلد."
        else "- افتح index.html للمعاينة محلياً في المتصفح."
        return """
# موقع "${site.siteTitle}" — مُصدَّر من FOKOF2

هذا الملف يحتوي موقعك كاملاً كملفات مستقلة تملكها وتشغّلها على أي استضافة ثابتة.

## المحتويات
$pageLines
- style.css — ملف التنسيقات.
- script.js — ملف التفاعل (السلة، البحث، النموذج).
- img/ — الصور المستخدمة في الموقع.
- site.json — ملف إعدادات المشروع؛ يمكن إعادة استيراده داخل تطبيق FOKOF2 (زر "استيراد مشروع" في صفحة المواقع).
- README.md — هذا الملف.

## التشغيل والنشر
1. افكَ ضغط الملف.
2. ارفع كل الملفات كما هي إلى أي استضافة ثابتة: Netlify أو Vercel أو GitHub Pages أو cPanel (المجلد العام public_html).
3. $multiNote

## متطلبات وحدود مهمة
- الموقع ثابت (Static): لا يحتاج قاعدة بيانات ولا خادماً خاصاً.
- يحتاج اتصالاً بالإنترنت لتحميل الخطوط (Google Fonts) وخريطة جوجل وروابط واتساب.
- نموذج الطلب/الحجز يرسل عبر واتساب مباشرة؛ لا يُخزَّن في قاعدة بيانات بعد التصدير.
- زر السلة وطلبات واتساب تعمل على رقم الواتساب المسجل داخل الموقع.
""".trimIndent()
    }
}
