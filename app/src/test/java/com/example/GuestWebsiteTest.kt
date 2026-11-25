package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserWebsite
import com.example.data.repository.BarayaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A visitor without an account can build, save and list a website on the device. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GuestWebsiteTest {
    @Test fun guest_can_save_and_list_a_website() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = BarayaRepository(ctx)
        repo.ensureTemplatesInitialized()
        val site = UserWebsite(id = "site_t1", userId = "guest", templateId = "t", siteTitle = "موقع تجريبي", siteSlug = "site-1", businessType = "AGENCY", headline = "h", subheadline = "s", aboutText = "a", whatsappNumber = "201000000000", phoneNumber = "0100", address = "القاهرة", workingHours = "9-5")
        repo.saveUserWebsite(site)
        val list = repo.userWebsites.first()
        println("GUEST_WEBSITE_COUNT=${list.size}")
        assertEquals(1, list.count { it.id == "site_t1" })
    }
}
