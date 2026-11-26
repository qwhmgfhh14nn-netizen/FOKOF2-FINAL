package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SiteExtras
import com.example.data.model.UserWebsite
import com.example.data.model.WebsiteItem
import com.example.ui.screens.generateStandaloneHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Product photos and the store cover photo reach the exported site; sites without them are unchanged. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SiteImagesTest {
    private fun site(items: List<WebsiteItem>, extras: SiteExtras) = UserWebsite(
        id = "img", templateId = "t", siteTitle = "متجر", siteSlug = "s", businessType = "FASHION",
        headline = "h", subheadline = "s", aboutText = "a", whatsappNumber = "201000000000", phoneNumber = "01000000000",
        address = "x", workingHours = "y", items = items, extras = extras
    )

    @Test fun product_and_cover_images_are_embedded() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val f = java.io.File(ctx.filesDir, "prod.jpg"); f.writeBytes(ByteArray(2000) { 7 })
        val h = java.io.File(ctx.filesDir, "cover.jpg"); h.writeBytes(ByteArray(3000) { 9 })
        val withImg = generateStandaloneHtml(
            site(listOf(WebsiteItem(nameAr = "فستان", priceEgp = 100, imagePath = f.absolutePath), WebsiteItem(nameAr = "بدون صورة", priceEgp = 50)),
                SiteExtras(heroImage = h.absolutePath)), ctx)
        assertEquals("one product image", 1, Regex("<img class=\"pimg\"").findAll(withImg).count())
        assertTrue(withImg.contains("url('data:image/jpeg;base64,CQkJ"))
        val without = generateStandaloneHtml(site(listOf(WebsiteItem(nameAr = "فستان", priceEgp = 100)), SiteExtras()), ctx)
        assertFalse(without.contains("<img class=\"pimg\""))
        println("SITE_IMAGES_OK")
    }
}
