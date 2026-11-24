package com.example

import com.example.data.model.FeatureAvailability
import com.example.ui.AppNavDestination
import com.example.ui.blockedFeatureFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FeatureGateTest {
    private fun f(key: String, status: String) = FeatureAvailability(key, "t", "t", status)

    @Test
    fun switchedOffServiceBlocksItsScreens() {
        val features = listOf(f("AI_IMAGE", "DISABLED"), f("WEBSITE_BUILDER", "UNAVAILABLE"), f("QR_STUDIO", "AVAILABLE"))
        assertNotNull(blockedFeatureFor(AppNavDestination.AI_IMAGE, features))
        assertNotNull(blockedFeatureFor(AppNavDestination.WEBSITES, features))
        assertNotNull(blockedFeatureFor(AppNavDestination.WEBSITE_EDITOR, features))
        assertNull(blockedFeatureFor(AppNavDestination.STORE_QR_STUDIO, features))
    }

    @Test
    fun coreScreensAreNeverBlocked() {
        val all = listOf("AI_IMAGE", "AI_COPYWRITER", "AI_BRAND_KIT", "MOCKUP_STUDIO", "QR_STUDIO", "WEBSITE_BUILDER").map { f(it, "DISABLED") }
        listOf(AppNavDestination.HOME, AppNavDestination.TEMPLATES, AppNavDestination.STUDIO, AppNavDestination.ACCOUNT, AppNavDestination.PURCHASES)
            .forEach { assertNull(blockedFeatureFor(it, all)) }
        assertEquals("AI_COPYWRITER", blockedFeatureFor(AppNavDestination.AI_MARKETING, all)?.featureKey)
    }
}
