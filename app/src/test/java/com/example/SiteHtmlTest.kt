package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserWebsite
import com.example.data.model.WebsiteItem
import com.example.ui.screens.generateStandaloneHtml
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Generated sites embed a real hero photo per business type, carry motion, and escape user text. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SiteHtmlTest {
    @Test fun every_business_type_embeds_photo_and_effects() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val types = listOf("RESTAURANT", "PHARMACY", "FASHION", "CLINIC", "REAL_ESTATE", "SALON", "MOTORS", "GROCERY", "AGENCY", "ACADEMY")
        for (t in types) {
            val site = UserWebsite(id = "x_$t", templateId = "t", siteTitle = "<b>اسم</b>", siteSlug = "s", businessType = t, headline = "h", subheadline = "s", aboutText = "a", whatsappNumber = "201000000000", phoneNumber = "0100", address = "القاهرة", workingHours = "9-5", items = listOf(WebsiteItem(nameAr = "منتج", priceEgp = 10)))
            val html = generateStandaloneHtml(site, ctx)
            assertTrue("hero photo missing for $t", html.contains("data:image/jpeg;base64,/9j/"))
            assertTrue(html.contains("IntersectionObserver"))
            assertTrue(html.contains("@keyframes kenburns"))
            assertFalse("unescaped title for $t", html.contains("<b>اسم</b>"))
            println("SITE_HTML_OK $t bytes=${html.length}")
        }
    }
}
