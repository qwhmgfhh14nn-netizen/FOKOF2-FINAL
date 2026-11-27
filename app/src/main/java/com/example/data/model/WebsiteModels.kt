package com.example.data.model

import java.util.UUID

/**
 * Feature Toggles: Configurable by the owner for each website template.
 * Controls which functional modules are active in the website.
 */
data class WebsiteFeatureToggles(
    val featureWhatsappOrders: Boolean = true,
    val featureBookingSystem: Boolean = false,
    val featureProductCatalog: Boolean = true,
    val featureCustomerReviews: Boolean = true,
    val featureFaqSection: Boolean = true,
    val featureGoogleMaps: Boolean = true,
    val featureDirectCalling: Boolean = true,
    val featureCustomDomain: Boolean = false
)

/**
 * Represents an item in the website catalog:
 * - A meal/dish for restaurants
 * - A medicine/skincare product for pharmacies
 * - A dress/outfit for fashion stores
 * - A service/treatment for clinics or salons
 * - A property/unit for real estate
 * - A car/service for automotive
 * - A course for academies
 */
data class WebsiteItem(
    val id: String = UUID.randomUUID().toString(),
    val nameAr: String,
    val nameEn: String = "",
    val priceEgp: Int,
    val originalPriceEgp: Int = 0,
    val descriptionAr: String = "",
    val category: String = "عام",
    val badgeAr: String? = null,
    val isAvailable: Boolean = true,
    val imagePath: String = ""
)

/**
 * Ready-to-use production website template.
 */
data class WebsiteTemplate(
    val id: String,
    val businessType: String, // RESTAURANT, PHARMACY, FASHION, CLINIC, REAL_ESTATE, SALON, MOTORS, GROCERY, AGENCY, ACADEMY
    val businessTypeNameAr: String,
    val titleAr: String,
    val titleEn: String,
    val taglineAr: String,
    val descriptionAr: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val darkBgHex: String,
    val priceEgp: Int = 0, // 0 = Free tier eligible
    val isPremium: Boolean = false,
    val isPublished: Boolean = true,
    val featureToggles: WebsiteFeatureToggles = WebsiteFeatureToggles(),
    val sampleHeadline: String,
    val sampleSubheadline: String,
    val sampleAbout: String,
    val sampleAddress: String,
    val sampleWorkingHours: String,
    val samplePhone: String = "01023456789",
    val sampleWhatsapp: String = "201023456789",
    val sampleItems: List<WebsiteItem> = emptyList()
)

/**
 * A website created and customized by a user or business owner.
 */
/** One customer review shown on the site. */
data class SiteTestimonial(val name: String = "", val text: String = "", val stars: Int = 5)

/** One section inside an extra site page. Types: hero, text, items, gallery, reviews, faq, map, contact. */
data class PageSection(val type: String = "text", val text: String = "")

/** One custom field the owner adds to the booking/contact form. kind: text | phone | note. */
data class SiteFormField(val label: String = "", val kind: String = "text")

/** An extra page of a multi-page site. Page 0 is always the home page (its content comes from the main site fields). */
data class SitePage(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "صفحة",
    val sections: List<PageSection> = emptyList()
)

/** Extra per-site content: reviews, gallery photos (urls or local file paths) and social links. */
data class SiteExtras(
    val testimonials: List<SiteTestimonial> = emptyList(),
    val gallery: List<String> = emptyList(),
    val facebook: String = "",
    val instagram: String = "",
    val tiktok: String = "",
    val youtube: String = "",
    val telegram: String = "",
    val website: String = "",
    val heroImage: String = "",
    /** Announcement bar text (empty = none), colour theme (0 = owner colour, 1..12 presets), font (0..5) and footer credit toggle. */
    val announcement: String = "",
    val theme: Int = 0,
    val font: Int = 0,
    val hideFooter: Boolean = false,
    /** Extra pages of a multi-page site; empty = classic single-page site. When used, page 0 is the home page. */
    val pages: List<SitePage> = emptyList(),
    /** Optional site logo (local file path or url), shown in the nav and used as favicon. */
    val logoImage: String = "",
    /** Custom fields appended to the booking/contact form; submissions arrive on WhatsApp. */
    val customFields: List<SiteFormField> = emptyList(),
    /** Optional discount coupon: code the customer types in the cart and the percent taken off. */
    val couponCode: String = "",
    val couponPercent: Int = 0
)

data class UserWebsite(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "guest",
    val templateId: String,
    val siteTitle: String,
    val siteSlug: String,
    val businessType: String,
    val headline: String,
    val subheadline: String,
    val aboutText: String,
    val whatsappNumber: String,
    val phoneNumber: String,
    val address: String,
    val workingHours: String,
    val primaryColorHex: String = "#D4AF37",
    val items: List<WebsiteItem> = emptyList(),
    val featureToggles: WebsiteFeatureToggles = WebsiteFeatureToggles(),
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val extras: SiteExtras = SiteExtras()
)

/**
 * Owner-controlled global website builder limits and prices.
 */
data class WebsiteBuilderSettings(
    val freeWebsitesLimit: Int = 1, // Number of free websites allowed per user
    val extraWebsitePriceEgp: Int = 99, // Price in EGP for additional websites
    val customDomainPriceEgp: Int = 199, // Price in EGP for custom domain linking
    val isBuilderActive: Boolean = true
)

/**
 * Realistic Mockup Showcase Item.
 */
enum class MockupType(val titleAr: String, val titleEn: String, val iconName: String) {
    SHOPPING_BAG("كيس تسوق فاخر", "Shopping Bag", "shopping_bag"),
    COFFEE_CUP("كوب قهوة ومشروبات", "Coffee Cup", "local_cafe"),
    BUSINESS_CARD("كارت عمل شخصي", "Business Card", "badge"),
    BILLBOARD("لوحة إعلانية خارجية", "Outdoor Billboard", "crop_landscape"),
    BURGER_BOX("علبة وجبات سريعة", "Food Packaging", "lunch_dining"),
    SMARTPHONE("شاشة هاتف ذكي", "Mobile Screen", "smartphone"),
    STOREFRONT("لافتة واجهة محل", "Storefront Sign", "storefront"),
    TSHIRT("تيشيرت تجاري قطني", "Apparel T-Shirt", "checkroom")
}

data class MockupShowcase(
    val type: MockupType,
    val titleAr: String,
    val subtitleAr: String,
    val frameColorHex: String,
    val bgHex: String
)

/**
 * Item in the customer's cart
 */
data class CartItem(
    val item: WebsiteItem,
    val quantity: Int = 1,
    val notes: String = ""
)

/**
 * Analytics summary for a user's website
 */
data class WebsiteAnalytics(
    val siteId: String,
    val viewsCount: Int = 0,
    val whatsappClicksCount: Int = 0,
    val catalogViewsCount: Int = 0,
    val lastVisitedAt: Long = System.currentTimeMillis()
)

/**
 * Appointment or reservation booking inquiry
 */
data class WebsiteBooking(
    val id: String = UUID.randomUUID().toString(),
    val siteId: String,
    val customerName: String,
    val customerPhone: String,
    val serviceName: String,
    val bookingDate: String,
    val bookingTime: String,
    val notes: String = "",
    val status: String = "PENDING", // PENDING, CONFIRMED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Visual frame styles for store QR code printouts
 */
enum class QrFrameStyle(val titleAr: String, val subtitleAr: String) {
    MINIMAL("بسيط بدون إطار", "شعار ورمز QR فقط"),
    TABLE_TENT("مثلث طاولة مطعم", "امسح واطلب من المنيو"),
    STOREFRONT_STICKER("ملصق واجهة محل", "تابع عروضنا ومنتجاتنا"),
    BUSINESS_CARD("كارت عمل أنيق", "تواصل معنا مباشرة")
}

