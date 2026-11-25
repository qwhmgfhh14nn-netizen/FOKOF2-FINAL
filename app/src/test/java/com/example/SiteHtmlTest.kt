package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserWebsite
import com.example.data.templates.WebsiteTemplatesData
import com.example.ui.screens.generateStandaloneHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Every website template has a bundled photo; generated sites carry sections, motion, and escape user text. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SiteHtmlTest {
    @Test fun every_template_embeds_photo_sections_and_effects() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val templates = WebsiteTemplatesData.getDefaultTemplates()
        assertEquals("expected 18 website fields", 18, templates.map { it.businessType }.distinct().size)
        for (tpl in templates) {
            val site = UserWebsite(
                id = "x_" + tpl.businessType, templateId = tpl.id, siteTitle = "<b>اسم</b>", siteSlug = "s",
                businessType = tpl.businessType, headline = tpl.sampleHeadline, subheadline = tpl.sampleSubheadline,
                aboutText = tpl.sampleAbout, whatsappNumber = tpl.sampleWhatsapp, phoneNumber = tpl.samplePhone,
                address = tpl.sampleAddress, workingHours = tpl.sampleWorkingHours,
                items = tpl.sampleItems, featureToggles = tpl.featureToggles
            )
            val html = generateStandaloneHtml(site, ctx)
            val t = tpl.businessType
            assertTrue("hero photo missing for $t", html.contains("data:image/jpeg;base64,/9j/"))
            assertTrue("effects missing for $t", html.contains("IntersectionObserver") && html.contains("@keyframes kenburns"))
            assertTrue("booking form missing for $t", html.contains("id=\"bf\""))
            assertTrue("map missing for $t", html.contains("maps.google.com/maps?q="))
            assertTrue("faq missing for $t", html.contains("<details>"))
            assertTrue("dark toggle missing for $t", html.contains("id=\"theme\""))
            assertTrue("filter chips missing for $t", html.contains("class=\"chip on\""))
            assertFalse("unescaped title for $t", html.contains("<b>اسم</b>"))
            println("SITE_HTML_OK $t bytes=${html.length}")
            if (t == "RESTAURANT" || t == "HOTEL" || t == "LAWYER") {
                println("SITE_HTML_BEGIN $t")
                println(html.replace(Regex("data:image/jpeg;base64,[A-Za-z0-9+/=]+"), "HERO.jpg"))
                println("SITE_HTML_END")
            }
        }
    }
}
