package com.example.ui

import com.example.data.model.FeatureAvailability

/** Maps a screen to the owner-controlled service key (see the owner Admin page). Core screens return null. */
fun featureKeyFor(nav: AppNavDestination): String? = when (nav) {
    AppNavDestination.AI_IMAGE -> "AI_IMAGE"
    AppNavDestination.AI_MARKETING -> "AI_COPYWRITER"
    AppNavDestination.BRAND_KIT -> "AI_BRAND_KIT"
    AppNavDestination.MOCKUP_STUDIO -> "MOCKUP_STUDIO"
    AppNavDestination.STORE_QR_STUDIO -> "QR_STUDIO"
    AppNavDestination.WEBSITES, AppNavDestination.WEBSITE_EDITOR -> "WEBSITE_BUILDER"
    else -> null
}

/** Returns the switched-off service for this screen, or null when the screen may be shown. */
fun blockedFeatureFor(nav: AppNavDestination, features: List<FeatureAvailability>): FeatureAvailability? {
    val key = featureKeyFor(nav) ?: return null
    val feature = features.firstOrNull { it.featureKey == key } ?: return null
    return if (feature.status == "AVAILABLE") null else feature
}
