package com.example.data.repository

import androidx.room.withTransaction
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BundleEntity
import com.example.data.local.BundleCreditEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.PurchaseEntity
import com.example.data.local.SubscriptionPlanEntity
import com.example.data.local.TemplateEntity
import com.example.data.local.UserEntity
import com.example.data.local.UserWebsiteEntity
import com.example.data.local.WebsiteAnalyticsEntity
import com.example.data.local.WebsiteBookingEntity
import com.example.data.local.WebsiteSettingsEntity
import com.example.data.local.WebsiteTemplateEntity
import com.example.data.local.PaymentMethodConfigEntity
import com.example.data.local.FeatureAvailabilityEntity
import com.example.data.local.DiscountCouponEntity
import com.example.util.SecurityUtils
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.CategoryModel
import com.example.data.model.DesignTemplate
import com.example.data.model.DiscountCoupon
import com.example.data.model.FeatureAvailability
import com.example.data.model.PaymentMethodConfig
import com.example.data.model.ProTier
import com.example.data.model.PurchasedTemplateItem
import com.example.data.model.ShapeType
import com.example.data.model.SubscriptionPlan
import com.example.data.model.TemplateBundle
import com.example.data.model.UserProfile
import com.example.data.model.UserWebsite
import com.example.data.model.SiteExtras
import com.example.data.model.SiteTestimonial
import com.example.data.model.WebsiteAnalytics
import com.example.data.model.WebsiteBooking
import com.example.data.model.WebsiteBuilderSettings
import com.example.data.model.WebsiteFeatureToggles
import com.example.data.model.WebsiteItem
import com.example.data.model.WebsiteTemplate
import com.example.data.templates.BundlesData
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import com.example.data.templates.WebsiteTemplatesData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val DEFAULT_TEMPLATES_VERSION = 4
// Bump when assets/templates/catalog.json gains templates (append-only file).
private const val CATALOG_TEMPLATES_VERSION = 5

class BarayaRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val projectDao = database.projectDao()
    private val paymentDao = database.paymentDao()
    private val purchaseDao = database.purchaseDao()
    private val templateDao = database.templateDao()
    private val categoryDao = database.categoryDao()
    private val bundleDao = database.bundleDao()
    private val bundleCreditDao = database.bundleCreditDao()
    private val subscriptionPlanDao = database.subscriptionPlanDao()
    private val userWebsiteDao = database.userWebsiteDao()
    private val websiteTemplateDao = database.websiteTemplateDao()
    private val websiteSettingsDao = database.websiteSettingsDao()
    private val websiteAnalyticsDao = database.websiteAnalyticsDao()
    private val websiteBookingDao = database.websiteBookingDao()
    private val paymentMethodConfigDao = database.paymentMethodConfigDao()
    private val featureAvailabilityDao = database.featureAvailabilityDao()
    private val discountCouponDao = database.discountCouponDao()
    private val prefs = context.getSharedPreferences("baraya_user_prefs", Context.MODE_PRIVATE)
    private val initializationMutex = Mutex()

    // Sensitive owner controls require an explicit password check in the current session.
    @Volatile private var ownerControlUnlocked = false
    private val ownerControlState = MutableStateFlow(false)

    private fun hasOwnerControlPermission(): Boolean {
        val user = _currentUser.value
        return ownerControlUnlocked && !user.isGuest && user.role in setOf("OWNER", "ADMIN")
    }

    private fun hasOwnerOnlyPermission(): Boolean {
        val user = _currentUser.value
        return ownerControlUnlocked && !user.isGuest && user.role == "OWNER"
    }

    // Start as a guest; a saved authenticated session is restored only after database initialization.
    private val _currentUser = MutableStateFlow(getGuestProfile())
    val currentUser = _currentUser.asStateFlow()

    // Persistent Payment Methods Flow
    val allPaymentMethods: Flow<List<PaymentMethodConfig>> = paymentMethodConfigDao.getAllPaymentMethods().map { list ->
        list.map {
            PaymentMethodConfig(
                id = it.id,
                nameAr = it.nameAr,
                nameEn = it.nameEn,
                accountNumber = it.accountNumber,
                confirmationContact = it.confirmationContact,
                whatsappLink = it.whatsappLink,
                telegramLink = it.telegramLink,
                instructionsAr = it.instructionsAr,
                instructionsEn = it.instructionsEn,
                isEnabled = it.isEnabled,
                statusNoteAr = it.statusNoteAr,
                statusNoteEn = it.statusNoteEn,
                iconName = it.iconName
            )
        }
    }

    val enabledPaymentMethods: Flow<List<PaymentMethodConfig>> = paymentMethodConfigDao.getEnabledPaymentMethods().map { list ->
        list.map {
            PaymentMethodConfig(
                id = it.id,
                nameAr = it.nameAr,
                nameEn = it.nameEn,
                accountNumber = it.accountNumber,
                confirmationContact = it.confirmationContact,
                whatsappLink = it.whatsappLink,
                telegramLink = it.telegramLink,
                instructionsAr = it.instructionsAr,
                instructionsEn = it.instructionsEn,
                isEnabled = it.isEnabled,
                statusNoteAr = it.statusNoteAr,
                statusNoteEn = it.statusNoteEn,
                iconName = it.iconName
            )
        }
    }

    private fun hasPaymentAdminPermission(): Boolean = hasOwnerControlPermission()

    suspend fun togglePaymentMethod(id: String, isEnabled: Boolean): Boolean {
        if (!hasPaymentAdminPermission()) return false
        val method = paymentMethodConfigDao.getPaymentMethodById(id) ?: return false
        if (isEnabled && (method.accountNumber.isBlank() || method.instructionsAr.isBlank())) {
            return false
        }
        paymentMethodConfigDao.updateStatus(id, isEnabled)
        return true
    }

    suspend fun updatePaymentMethodDetails(
        id: String,
        accountNumber: String,
        contact: String,
        wa: String,
        tg: String,
        instrAr: String,
        statusNoteAr: String
    ): Boolean {
        if (!hasPaymentAdminPermission()) return false
        if (paymentMethodConfigDao.getPaymentMethodById(id) == null) return false
        paymentMethodConfigDao.updateDetails(id, accountNumber, contact, wa, tg, instrAr, statusNoteAr)
        if (accountNumber.isBlank() || instrAr.isBlank()) {
            paymentMethodConfigDao.updateStatus(id, false)
        }
        return true
    }

    // Persistent Feature Availability Flow
    val allFeatures: Flow<List<FeatureAvailability>> = featureAvailabilityDao.getAllFeatures().map { list ->
        list.map {
            FeatureAvailability(
                featureKey = it.featureKey,
                titleAr = it.titleAr,
                titleEn = it.titleEn,
                status = it.status,
                messageAr = it.messageAr,
                messageEn = it.messageEn
            )
        }
    }

    suspend fun updateFeatureStatus(key: String, status: String, messageAr: String = "") {
        if (!hasOwnerControlPermission()) return
        if (status !in setOf("AVAILABLE", "UNAVAILABLE", "MAINTENANCE")) return
        featureAvailabilityDao.updateFeatureStatus(key, status)
    }

    // Persistent Coupons Flow
    val allCoupons: Flow<List<DiscountCoupon>> = discountCouponDao.getAllCoupons().map { list ->
        list.map {
            DiscountCoupon(
                code = it.code,
                discountPercent = it.discountPercent,
                description = it.description,
                expirationDate = it.expirationDate,
                usageLimit = it.usageLimit,
                usageCount = it.usageCount,
                isActive = it.isActive
            )
        }
    }

    suspend fun addDiscountCoupon(coupon: DiscountCoupon) {
        if (!hasOwnerControlPermission()) return
        if (coupon.code.isBlank() || coupon.discountPercent !in 1..100 || coupon.usageLimit < 1) return
        discountCouponDao.insertCoupon(
            DiscountCouponEntity(
                code = coupon.code.uppercase().trim(),
                discountPercent = coupon.discountPercent,
                description = coupon.description.trim(),
                expirationDate = coupon.expirationDate,
                usageLimit = coupon.usageLimit,
                usageCount = coupon.usageCount,
                isActive = coupon.isActive
            )
        )
    }

    suspend fun deleteDiscountCoupon(code: String) {
        if (!hasOwnerControlPermission()) return
        discountCouponDao.deleteCoupon(code.trim().uppercase())
    }

    // Pre-populated Discounts for Admin (backward-compat StateFlow)
    private val _availableCoupons = MutableStateFlow(
        listOf(
            DiscountCoupon("BARAYA50", 50, "خصم 50% بمناسبة الانطلاق"),
            DiscountCoupon("CAIRO20", 20, "خصم 20% لعملاء القاهرة ومصر"),
            DiscountCoupon("VIPGOLD", 30, "خصم خاص للمشتركين الذهبيين"),
            DiscountCoupon("ORANGE", 25, "عرض خاص لعملاء الدفع عبر أورنج كاش")
        )
    )
    val availableCoupons = _availableCoupons.asStateFlow()

    fun addCoupon(code: String, percent: Int, description: String) {
        if (!hasOwnerControlPermission() || code.isBlank() || percent !in 1..100) return
        _availableCoupons.value = _availableCoupons.value + DiscountCoupon(
            code = code.uppercase(),
            discountPercent = percent,
            description = description,
            isActive = true
        )
    }

    fun removeCoupon(code: String) {
        if (!hasOwnerControlPermission()) return
        _availableCoupons.value = _availableCoupons.value.filter { it.code != code.trim().uppercase() }
    }

    // Dynamic Editable Bundles in Egyptian Pounds (EGP)
    private val _bundles = MutableStateFlow(BundlesData.getDefaultBundles())
    val bundles = _bundles.asStateFlow()

    fun updateBundlePrice(bundleId: String, newPriceEgp: Int) {
        if (!hasOwnerControlPermission() || newPriceEgp !in 0..1_000_000) return
        _bundles.value = _bundles.value.map { b ->
            if (b.id == bundleId) b.copy(priceEgp = newPriceEgp) else b
        }
    }

    // Tier Base Pricing editable by Admin
    private val _tierPrices = MutableStateFlow(
        mapOf(
            ProTier.BASIC to 5,
            ProTier.PROFESSIONAL to 10,
            ProTier.PREMIUM to 15,
            ProTier.EXCLUSIVE to 20
        )
    )
    val tierPrices = _tierPrices.asStateFlow()

    fun updateTierPrice(tier: ProTier, newPriceEgp: Int) {
        if (!hasOwnerControlPermission() || newPriceEgp !in 0..1_000_000) return
        _tierPrices.value = _tierPrices.value.toMutableMap().apply { put(tier, newPriceEgp) }
    }

    // Categories Flow from Room Database
    val allCategories: Flow<List<CategoryModel>> = categoryDao.getAllCategories().map { list ->
        list.map {
            CategoryModel(
                id = it.id,
                nameAr = it.nameAr,
                nameEn = it.nameEn,
                iconName = it.iconName,
                orderIndex = it.orderIndex,
                isDefault = it.isDefault
            )
        }
    }

    suspend fun addCategory(nameAr: String, nameEn: String, iconName: String = "folder") {
        if (!hasOwnerControlPermission() || nameAr.isBlank()) return
        val newCat = CategoryEntity(
            id = "cat_${UUID.randomUUID().toString().take(8)}",
            nameAr = nameAr.trim(),
            nameEn = nameEn.trim().ifBlank { nameAr.trim() },
            iconName = iconName,
            orderIndex = categoryDao.getCategoryCount(),
            isDefault = false
        )
        categoryDao.insertCategory(newCat)
    }

    suspend fun updateCategory(id: String, nameAr: String, nameEn: String) {
        if (!hasOwnerControlPermission() || nameAr.isBlank()) return
        categoryDao.updateCategory(id, nameAr.trim(), nameEn.trim())
    }

    suspend fun deleteCategory(id: String) {
        if (!hasOwnerControlPermission()) return
        categoryDao.deleteCategory(id)
    }

    // Bundles & Subscriptions Flows from Room Database
    val allBundles: Flow<List<TemplateBundle>> = bundleDao.getAllBundles().map { list ->
        list.map {
            TemplateBundle(
                id = it.id,
                count = it.count,
                priceEgp = it.priceEgp,
                originalPriceEgp = it.originalPriceEgp,
                titleAr = "باقة ${it.count} قوالب",
                titleEn = "${it.count} Templates Bundle",
                descriptionAr = "وصول واستخدام ${it.count} قالب احترافي من اختيارك بدون قيود",
                descriptionEn = "Access and use ${it.count} professional design templates",
                isPopular = it.isPopular
            )
        }
    }

    suspend fun updateBundlePriceInDb(bundleId: String, newPriceEgp: Int) {
        if (!hasOwnerControlPermission() || newPriceEgp !in 0..1_000_000) return
        bundleDao.updateBundlePrice(bundleId, newPriceEgp)
        updateBundlePrice(bundleId, newPriceEgp)
    }

    val allSubscriptionPlans: Flow<List<SubscriptionPlan>> = subscriptionPlanDao.getAllPlans().map { list ->
        list.map {
            SubscriptionPlan(
                id = it.id,
                nameAr = it.nameAr,
                nameEn = it.nameEn,
                priceEgp = it.priceEgp,
                originalPriceEgp = it.originalPriceEgp,
                billingCycle = it.billingCycle,
                descriptionAr = it.descriptionAr,
                descriptionEn = it.descriptionEn,
                isPopular = it.isPopular
            )
        }
    }

    suspend fun updateSubscriptionPlanPriceInDb(planId: String, newPriceEgp: Int) {
        if (!hasOwnerControlPermission() || newPriceEgp !in 0..1_000_000) return
        subscriptionPlanDao.updatePlanPrice(planId, newPriceEgp)
    }

    // ---------------- WEBSITE BUILDER FLOWS & METHODS ----------------
    val allWebsiteTemplates: Flow<List<WebsiteTemplate>> = websiteTemplateDao.getAllTemplates().map { list ->
        list.map { entityToWebsiteTemplate(it) }
    }

    val publishedWebsiteTemplates: Flow<List<WebsiteTemplate>> = websiteTemplateDao.getPublishedTemplates().map { list ->
        list.map { entityToWebsiteTemplate(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userWebsites: Flow<List<UserWebsite>> = _currentUser.flatMapLatest { current ->
        userWebsiteDao.getUserWebsites(current.id)
    }.map { list -> list.map { entityToUserWebsite(it) } }

    val websiteSettings: Flow<WebsiteBuilderSettings> = websiteSettingsDao.getSettings().map { entity ->
        if (entity != null) {
            WebsiteBuilderSettings(
                freeWebsitesLimit = entity.freeWebsitesLimit,
                extraWebsitePriceEgp = entity.extraWebsitePriceEgp,
                customDomainPriceEgp = entity.customDomainPriceEgp,
                isBuilderActive = true
            )
        } else {
            WebsiteBuilderSettings()
        }
    }

    suspend fun saveUserWebsite(website: UserWebsite) {
        val current = _currentUser.value
        if (website.userId != current.id) return
        val existing = userWebsiteDao.getWebsiteById(website.id)
        if (existing != null && existing.userId != current.id) return
        val entity = userWebsiteToEntity(website)
        userWebsiteDao.insertWebsite(entity)
    }

    suspend fun deleteUserWebsite(id: String) {
        val current = _currentUser.value
        val existing = userWebsiteDao.getWebsiteById(id) ?: return
        if (existing.userId != current.id && !hasOwnerControlPermission()) return
        userWebsiteDao.deleteWebsite(id)
    }

    suspend fun updateWebsiteTemplatePrice(id: String, priceEgp: Int) {
        if (!hasOwnerControlPermission() || priceEgp !in 0..1_000_000) return
        websiteTemplateDao.updateTemplatePrice(id, priceEgp)
    }

    suspend fun setWebsiteTemplatePublished(id: String, published: Boolean) {
        if (!hasOwnerControlPermission()) return
        websiteTemplateDao.updateTemplatePublished(id, published)
    }

    suspend fun updateWebsiteTemplateFeatureToggles(id: String, toggles: WebsiteFeatureToggles) {
        if (!hasOwnerControlPermission()) return
        val json = featureTogglesToJson(toggles)
        websiteTemplateDao.updateFeatureToggles(id, json)
    }

    suspend fun updateWebsiteSettings(freeLimit: Int, extraPriceEgp: Int, domainPriceEgp: Int) {
        if (!hasOwnerControlPermission()) return
        if (freeLimit !in 0..10_000 || extraPriceEgp !in 0..1_000_000 || domainPriceEgp !in 0..1_000_000) return
        websiteSettingsDao.insertOrUpdate(
            WebsiteSettingsEntity(
                id = "global_settings",
                freeWebsitesLimit = freeLimit,
                extraWebsitePriceEgp = extraPriceEgp,
                customDomainPriceEgp = domainPriceEgp
            )
        )
    }

    suspend fun getUserWebsiteCount(userId: String): Int {
        val current = _currentUser.value
        if (current.isGuest || (userId != current.id && !hasOwnerControlPermission())) return 0
        return userWebsiteDao.getUserWebsiteCount(userId)
    }

    // Database initialization: Empties template library until owner adds real templates, seeds categories, bundles & subscriptions
    suspend fun ensureTemplatesInitialized() = initializationMutex.withLock {
        ensureTemplatesInitializedInternal()
    }

    /**
     * Guarantees the 50 free + 14 pro default templates exist and are current, also on upgrades from
     * older builds. Owner-created templates, purchases and user data are never touched; for existing
     * defaults only the design content is refreshed (price/publish/featured state stays as the owner set it).
     */
    private suspend fun syncDefaultTemplates() {
        val seeds = (FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates())
            .map { com.example.data.templates.TemplateDesigner.enrich(it) }
        val syncedVersion = prefs.getInt("default_templates_version", 0)
        val toWrite = ArrayList<TemplateEntity>()
        for (seed in seeds) {
            val existing = templateDao.getTemplateById(seed.id)
            val fresh = templateToEntity(seed)
            if (existing == null) {
                toWrite += fresh
            } else if (syncedVersion < DEFAULT_TEMPLATES_VERSION) {
                toWrite += fresh.copy(
                    priceEgp = existing.priceEgp,
                    isPublished = existing.isPublished,
                    isFeatured = existing.isFeatured,
                    status = existing.status,
                    rating = existing.rating,
                    salesCount = existing.salesCount
                )
            }
        }
        if (toWrite.isNotEmpty()) templateDao.insertAll(toWrite)
        if (syncedVersion < DEFAULT_TEMPLATES_VERSION) {
            prefs.edit().putInt("default_templates_version", DEFAULT_TEMPLATES_VERSION).apply()
        }
    }


    /** Adds the generated preset catalog (append-only asset). Existing rows keep the owner's price/publish/featured state. */
    private suspend fun syncCatalogTemplates() {
        val done = prefs.getInt("catalog_templates_version", 0)
        if (done >= CATALOG_TEMPLATES_VERSION) return
        val seeds = try { com.example.data.templates.CatalogTemplates.load(appContext) } catch (e: Exception) { return }
        val toWrite = ArrayList<TemplateEntity>()
        for (seed in seeds) {
            val existing = templateDao.getTemplateById(seed.id)
            val fresh = templateToEntity(seed)
            toWrite += if (existing == null) fresh else fresh.copy(
                priceEgp = existing.priceEgp, isPublished = existing.isPublished, isFeatured = existing.isFeatured,
                status = existing.status, rating = existing.rating, salesCount = existing.salesCount
            )
        }
        if (toWrite.isNotEmpty()) templateDao.insertAll(toWrite)
        prefs.edit().putInt("catalog_templates_version", CATALOG_TEMPLATES_VERSION).apply()
    }

    private suspend fun ensureTemplatesInitializedInternal() {
        // Preserve existing owner-created templates when upgrading; never wipe a user's library.
        // 1. Seed 10 Categories as requested if empty
        if (categoryDao.getCategoryCount() == 0) {
            val initialCategories = listOf(
                CategoryEntity("cat_food", "مطاعم ومأكولات", "Restaurants and Food", "restaurant", 0, true),
                CategoryEntity("cat_fashion", "أزياء وجمال", "Fashion and Beauty", "style", 1, true),
                CategoryEntity("cat_cars", "سيارات ومحركات", "Cars", "directions_car", 2, true),
                CategoryEntity("cat_realestate", "عقارات ومقاولات", "Real Estate", "apartment", 3, true),
                CategoryEntity("cat_social", "سوشيال ميديا", "Social Media", "share", 4, true),
                CategoryEntity("cat_business", "أعمال وتسويق", "Business and Marketing", "business_center", 5, true),
                CategoryEntity("cat_events", "مناسبات واحتفالات", "Events and Celebrations", "celebration", 6, true),
                CategoryEntity("cat_education", "تعليم وتدريب", "Education", "school", 7, true),
                CategoryEntity("cat_tech", "تكنولوجيا وتقنية", "Technology", "devices", 8, true),
                CategoryEntity("cat_other", "أخرى ومتنوعة", "Other categories", "more_horiz", 9, true)
            )
            categoryDao.insertAll(initialCategories)
        }

        // 2. Seed the designed template library (50 free + 14 pro) when it is empty
        syncDefaultTemplates()
        syncCatalogTemplates()

        // 3. Seed Bundles if empty
        if (bundleDao.getBundleCount() == 0) {
            val defaultBundles = listOf(
                BundleEntity("bundle_5", 5, 20, 50, 60, "توفير 60%", "Save 60%"),
                BundleEntity("bundle_10", 10, 35, 100, 65, "توفير 65%", "Save 65%", isPopular = true),
                BundleEntity("bundle_20", 20, 60, 200, 70, "توفير 70%", "Save 70%"),
                BundleEntity("bundle_50", 50, 125, 500, 75, "توفير 75%", "Save 75%"),
                BundleEntity("bundle_100", 100, 199, 1000, 80, "أفضل قيمة", "Best Value")
            )
            bundleDao.insertAll(defaultBundles)
        }

        // 4. Seed Subscription Plans if empty
        if (subscriptionPlanDao.getPlanCount() == 0) {
            val defaultPlans = listOf(
                SubscriptionPlanEntity("sub_monthly", "الاشتراك الشهري", "Monthly Plan", 99, 199, "MONTHLY", "وصول لكافة قوالب المنصة وتحديثات أسبوعية"),
                SubscriptionPlanEntity("sub_annual", "الاشتراك السنوي", "Annual Plan", 599, 1199, "ANNUAL", "توفير 50% مع ميزات استوديو غير محدودة", isPopular = true),
                SubscriptionPlanEntity("sub_lifetime", "اشتراك مدى الحياة", "Lifetime Access", 1499, 2999, "LIFETIME", "دفع لمرة واحدة وامتلاك كل القوالب والميزات للأبد")
            )
            subscriptionPlanDao.insertAll(defaultPlans)
        }

        // 5. Remove the known default owner credential from older app builds.
        // The first account registered on this local installation can claim OWNER.
        val legacyOwnerCandidates = listOfNotNull(
            userDao.getUserByEmail("admin@fokof2.ai"),
            userDao.getUserByEmail("mohamed.baraya@baraya.ai")
        ).distinctBy { it.id }
        legacyOwnerCandidates.forEach { legacyOwner ->
            if (legacyOwner.role == "OWNER" &&
                SecurityUtils.isKnownInsecureDefaultPassword(legacyOwner.password)
            ) {
                userDao.updateUser(
                    legacyOwner.copy(
                        password = SecurityUtils.hashPassword(UUID.randomUUID().toString()),
                        role = "OWNER_SETUP_REQUIRED"
                    )
                )
            }
        }

        // 6. Seed Payment Methods (Orange Cash enabled ONLY on a fresh installation)
        val initialMethods = listOf(
                PaymentMethodConfigEntity(
                    id = "orange_cash",
                    nameAr = "أورنج كاش (Orange Cash)",
                    nameEn = "Orange Cash",
                    accountNumber = "01207164416",
                    confirmationContact = "01206449934",
                    whatsappLink = "https://wa.me/201206449934",
                    telegramLink = "https://t.me/+201206449934",
                    instructionsAr = "حول المبلغ المطلوب إلى رقم محفظة أورنج كاش: 01207164416 ثم ارفع صورة إيصال التحويل واضغط تأكيد الدفع للمراجعة الفورية.",
                    instructionsEn = "Transfer required amount to Orange Cash: 01207164416 and upload receipt.",
                    isEnabled = true,
                    statusNoteAr = "متاح ونشط حاليًا",
                    statusNoteEn = "Available & Active",
                    iconName = "orange"
                ),
                PaymentMethodConfigEntity(
                    id = "vodafone_cash",
                    nameAr = "فودافون كاش (Vodafone Cash)",
                    nameEn = "Vodafone Cash",
                    accountNumber = "",
                    confirmationContact = "",
                    instructionsAr = "غير متوفر حاليًا. سيتم الإعلان عند التفعيل.",
                    isEnabled = false,
                    statusNoteAr = "غير متوفر حاليًا – Currently Unavailable",
                    statusNoteEn = "Currently Unavailable",
                    iconName = "vodafone"
                ),
                PaymentMethodConfigEntity(
                    id = "instapay",
                    nameAr = "تطبيق إنستاباي (InstaPay)",
                    nameEn = "InstaPay Egypt",
                    accountNumber = "",
                    confirmationContact = "",
                    instructionsAr = "غير متوفر حاليًا. سيتم الإعلان عند التفعيل.",
                    isEnabled = false,
                    statusNoteAr = "غير متوفر حاليًا – Currently Unavailable",
                    statusNoteEn = "Currently Unavailable",
                    iconName = "instapay"
                ),
                PaymentMethodConfigEntity(
                    id = "paypal",
                    nameAr = "بايبال (PayPal)",
                    nameEn = "PayPal",
                    accountNumber = "",
                    confirmationContact = "",
                    instructionsAr = "غير متوفر حاليًا. سيتم الإعلان عند التفعيل.",
                    isEnabled = false,
                    statusNoteAr = "غير متوفر حاليًا – Currently Unavailable",
                    statusNoteEn = "Currently Unavailable",
                    iconName = "paypal"
                ),
                PaymentMethodConfigEntity(
                    id = "cards",
                    nameAr = "البطاقات البنكية (Visa / Mastercard)",
                    nameEn = "Visa & Mastercard",
                    accountNumber = "",
                    confirmationContact = "",
                    instructionsAr = "غير متوفر حاليًا. سيتم الإعلان عند التفعيل.",
                    isEnabled = false,
                    statusNoteAr = "غير متوفر حاليًا – Currently Unavailable",
                    statusNoteEn = "Currently Unavailable",
                    iconName = "credit_card"
                ),
                PaymentMethodConfigEntity(
                    id = "wallets",
                    nameAr = "المحافظ الإلكترونية الأخرى",
                    nameEn = "Other E-Wallets",
                    accountNumber = "",
                    confirmationContact = "",
                    instructionsAr = "غير متوفر حاليًا. سيتم الإعلان عند التفعيل.",
                    isEnabled = false,
                    statusNoteAr = "غير متوفر حاليًا – Currently Unavailable",
                    statusNoteEn = "Currently Unavailable",
                    iconName = "wallet"
                )
            )
        if (paymentMethodConfigDao.getMethodCount() == 0) {
            paymentMethodConfigDao.insertAll(initialMethods)
        } else {
            // Add missing standard choices as disabled; preserve all existing owner settings.
            initialMethods.forEach { method ->
                if (paymentMethodConfigDao.getPaymentMethodById(method.id) == null) {
                    paymentMethodConfigDao.insertMethod(method.copy(isEnabled = false))
                }
            }
        }

        // Clear only known placeholder account strings from older builds; preserve owner-configured values.
        val legacyPaymentPlaceholders = mapOf(
            "vodafone_cash" to "010xxxxxxxx",
            "instapay" to "baraya@instapay",
            "paypal" to "pay@baraya.ai",
            "cards" to "بوابة دفع إلكتروني",
            "wallets" to "محافظ مصرية"
        )
        legacyPaymentPlaceholders.forEach { (methodId, placeholderValue) ->
            val method = paymentMethodConfigDao.getPaymentMethodById(methodId)
            if (method?.accountNumber == placeholderValue) {
                paymentMethodConfigDao.updateDetails(
                    methodId,
                    accountNumber = "",
                    contact = "",
                    wa = "",
                    tg = "",
                    instrAr = "غير متوفر حاليًا. يجب إضافة بيانات صحيحة قبل التفعيل.",
                    statusNoteAr = "غير متوفر حاليًا – يحتاج إعداد المالك"
                )
                paymentMethodConfigDao.updateStatus(methodId, false)
            }
        }

        // 7. Seed Feature Availability
        if (featureAvailabilityDao.getFeatureCount() == 0) {
            val initialFeatures = listOf(
                FeatureAvailabilityEntity("AI_IMAGE", "توليد الصور بالذكاء الاصطناعي", "AI Image Generation", "AVAILABLE"),
                FeatureAvailabilityEntity("AI_COPYWRITER", "المحرر التسويقي الذكي", "AI Copywriter", "AVAILABLE"),
                FeatureAvailabilityEntity("AI_BRAND_KIT", "مولد الهوية البصرية", "AI Brand Kit", "AVAILABLE"),
                FeatureAvailabilityEntity("MOCKUP_STUDIO", "معمل المجسمات 3D", "3D Mockup Studio", "AVAILABLE"),
                FeatureAvailabilityEntity("QR_STUDIO", "استوديو باركود المتاجر", "Store QR Studio", "AVAILABLE"),
                FeatureAvailabilityEntity("WEBSITE_ANALYTICS", "تحليلات وزيارات المواقع", "Website Analytics", "AVAILABLE"),
                FeatureAvailabilityEntity("ONLINE_BOOKING", "حجز المواعيد والاستشارات", "Online Booking", "AVAILABLE"),
                FeatureAvailabilityEntity("WEBSITE_BUILDER", "منشئ ومحرر المواقع", "Website Builder", "AVAILABLE"),
                FeatureAvailabilityEntity("ADVANCED_DESIGN_TOOLS", "أدوات الاستوديو المتقدمة", "Advanced Design Tools", "AVAILABLE"),
                FeatureAvailabilityEntity("SUBSCRIPTION_SERVICES", "باقات الاشتراكات و VIP", "Subscription Services", "AVAILABLE"),
                FeatureAvailabilityEntity("PAYMENT_METHODS", "طرق الدفع الإلكتروني", "Payment Methods", "AVAILABLE")
            )
            featureAvailabilityDao.insertAll(initialFeatures)
        }
        // Keep capability labels honest without changing owner-configured enable/disable statuses.
        featureAvailabilityDao.updateFeatureTitle("AI_IMAGE", "توليد الصور بالذكاء الاصطناعي", "AI Image Generation")
        featureAvailabilityDao.updateFeatureTitle("AI_COPYWRITER", "اقتراحات تسويقية محلية", "Local Marketing Suggestions")
        featureAvailabilityDao.updateFeatureTitle("AI_BRAND_KIT", "اقتراحات هوية محلية", "Local Brand Suggestions")

        // 8. Seed Discount Coupons
        if (discountCouponDao.getCouponCount() == 0) {
            val initialCoupons = listOf(
                DiscountCouponEntity("BARAYA50", 50, "خصم 50% بمناسبة الانطلاق الرسمي", "2026/12/31", 500, 12, true),
                DiscountCouponEntity("CAIRO20", 20, "خصم 20% لعملاء القاهرة ومصر", "2026/12/31", 1000, 45, true),
                DiscountCouponEntity("VIPGOLD", 30, "خصم خاص للمشتركين الذهبيين", "2026/12/31", 200, 18, true),
                DiscountCouponEntity("ORANGE", 25, "عرض خاص لعملاء الدفع عبر أورنج كاش", "2026/12/31", 300, 5, true)
            )
            discountCouponDao.insertAll(initialCoupons)
        }

        // 9. Seed 10 Real Commercial Website Templates if empty
        run {
            val defaultSiteTemplates = WebsiteTemplatesData.getDefaultTemplates().filter { websiteTemplateDao.getTemplateById(it.id) == null }.map { tpl ->
                WebsiteTemplateEntity(
                    id = tpl.id,
                    businessType = tpl.businessType,
                    businessTypeNameAr = tpl.businessTypeNameAr,
                    titleAr = tpl.titleAr,
                    titleEn = tpl.titleEn,
                    taglineAr = tpl.taglineAr,
                    descriptionAr = tpl.descriptionAr,
                    primaryColorHex = tpl.primaryColorHex,
                    secondaryColorHex = tpl.secondaryColorHex,
                    darkBgHex = tpl.darkBgHex,
                    priceEgp = tpl.priceEgp,
                    isPremium = tpl.isPremium,
                    isPublished = tpl.isPublished,
                    featureTogglesJson = featureTogglesToJson(tpl.featureToggles),
                    sampleHeadline = tpl.sampleHeadline,
                    sampleSubheadline = tpl.sampleSubheadline,
                    sampleAbout = tpl.sampleAbout,
                    sampleAddress = tpl.sampleAddress,
                    sampleWorkingHours = tpl.sampleWorkingHours,
                    samplePhone = tpl.samplePhone,
                    sampleWhatsapp = tpl.sampleWhatsapp,
                    sampleItemsJson = websiteItemsToJson(tpl.sampleItems)
                )
            }
            if (defaultSiteTemplates.isNotEmpty()) websiteTemplateDao.insertAll(defaultSiteTemplates)
        }

        // 10. Seed Website Builder Settings if empty
        if (websiteSettingsDao.getSettingsSync() == null) {
            websiteSettingsDao.insertOrUpdate(
                WebsiteSettingsEntity(
                    id = "global_settings",
                    freeWebsitesLimit = 1,
                    extraWebsitePriceEgp = 99,
                    customDomainPriceEgp = 199
                )
            )
        }

        // Restore saved session from preferences
        val activeUserId = prefs.getString("active_user_id", "guest")
        if (activeUserId.isNullOrBlank() || activeUserId == "guest") {
            _currentUser.value = getGuestProfile()
        } else {
            val user = userDao.getUserById(activeUserId)
            if (user != null && user.role != "OWNER_SETUP_REQUIRED") {
                _currentUser.value = entityToProfile(user)
            } else {
                _currentUser.value = getGuestProfile()
                prefs.edit().putString("active_user_id", "guest").apply()
            }
        }
    }

    // ---------------- AUTHENTICATION & PROFILE METHODS ----------------

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String = "",
        bio: String = ""
    ): Result<UserProfile> {
        ensureTemplatesInitialized()
        val cleanEmail = email.trim().lowercase()
        if (!SecurityUtils.isPasswordValid(password)) {
            return Result.failure(Exception("كلمة المرور يجب أن لا تقل عن 8 أحرف لحماية حسابك."))
        }
        val existing = userDao.getUserByEmail(cleanEmail)
        val isFirstOwner = userDao.getUserCount() == 0
        val canRestoreLegacyOwner = userDao.getPrivilegedUserCount() == 0
        val hashedPassword = SecurityUtils.hashPassword(password)

        // Only the matching legacy owner email can reclaim a pending owner account.
        // New registrations remain regular users while that recovery is pending.
        if (existing != null) {
            if (existing.role == "OWNER_SETUP_REQUIRED" && canRestoreLegacyOwner) {
                val restoredOwner = existing.copy(
                    name = name.trim().ifBlank { "مالك FOKOF2" },
                    password = hashedPassword,
                    phone = phone.trim(),
                    bio = bio.trim().ifBlank { "مالك منصة FOKOF2" },
                    isVip = true,
                    role = "OWNER"
                )
                userDao.updateUser(restoredOwner)
                val ownerProfile = entityToProfile(restoredOwner)
                ownerControlUnlocked = false
                ownerControlState.value = false
                _currentUser.value = ownerProfile
                prefs.edit().putString("active_user_id", ownerProfile.id).apply()
                return Result.success(ownerProfile)
            }
            return Result.failure(Exception("هذا البريد الإلكتروني مسجل بالفعل. يرجى تسجيل الدخول."))
        }
        val newUser = UserEntity(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifBlank { "مستخدم FOKOF2" },
            email = cleanEmail,
            password = hashedPassword,
            phone = phone.trim(),
            bio = bio.trim().ifBlank { "مصمم مبدع على منصة FOKOF2 للإبداع والتصميم" },
            isVip = false,
            role = if (isFirstOwner) "OWNER" else "USER",
            avatarIndex = (0..5).random(),
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        val profile = entityToProfile(newUser)
        _currentUser.value = profile
        prefs.edit().putString("active_user_id", profile.id).apply()

        // Seed a welcome project for the newly registered user
        saveProject(
            title = "مشروعي الإبداعي الأول",
            category = "سوشيال ميديا",
            elements = listOf(
                CanvasElement(
                    id = UUID.randomUUID().toString(),
                    type = CanvasElementType.TEXT,
                    text = "مشروعي الإبداعي الأول",
                    x = 140f,
                    y = 150f,
                    colorHex = "#D4AF37",
                    fontSize = 24f,
                    isBold = true
                )
            ),
            bgStart = "#4338CA",
            bgEnd = "#7C3AED",
            userId = profile.id
        )

        return Result.success(profile)
    }

    suspend fun loginUser(email: String, password: String): Result<UserProfile> {
        ensureTemplatesInitialized()
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(Exception("الحساب غير موجود. يرجى التحقق من البريد أو إنشاء حساب جديد."))
        if (user.role == "OWNER_SETUP_REQUIRED") {
            return Result.failure(Exception("حساب الإدارة القديم يحتاج إلى إعادة إعداد. أنشئ حساب المالك الأول على هذا الجهاز."))
        }
        if (!SecurityUtils.verifyPassword(password, user.password)) {
            return Result.failure(Exception("كلمة المرور غير صحيحة. يرجى إعادة المحاولة."))
        }
        // Upgrade legacy plaintext or fixed-salt hashes after a successful login.
        val authenticatedUser = if (SecurityUtils.needsUpgrade(user.password)) {
            user.copy(password = SecurityUtils.hashPassword(password)).also { userDao.updateUser(it) }
        } else user
        val profile = entityToProfile(authenticatedUser)
        ownerControlUnlocked = false
        ownerControlState.value = false
        _currentUser.value = profile
        prefs.edit().putString("active_user_id", profile.id).apply()
        return Result.success(profile)
    }

    fun logout() {
        ownerControlUnlocked = false
        ownerControlState.value = false
        val guest = getGuestProfile()
        _currentUser.value = guest
        prefs.edit().putString("active_user_id", "guest").apply()
    }

    fun continueAsGuest() {
        logout()
    }

    suspend fun updateProfile(
        name: String,
        email: String,
        phone: String,
        bio: String,
        avatarIndex: Int
    ): Result<UserProfile> {
        val current = _currentUser.value
        if (current.isGuest) {
            val updated = current.copy(
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                bio = bio.trim(),
                avatarIndex = avatarIndex
            )
            _currentUser.value = updated
            return Result.success(updated)
        }
        val existing = userDao.getUserById(current.id)
            ?: return Result.failure(Exception("المستخدم غير موجود"))
        val requestedEmail = email.trim().lowercase().ifBlank { existing.email }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(requestedEmail).matches()) {
            return Result.failure(Exception("صيغة البريد الإلكتروني غير صحيحة."))
        }
        val emailOwner = userDao.getUserByEmail(requestedEmail)
        if (emailOwner != null && emailOwner.id != existing.id) {
            return Result.failure(Exception("البريد الإلكتروني مستخدم بالفعل لحساب آخر."))
        }
        val updatedEntity = existing.copy(
            name = name.trim().ifBlank { existing.name },
            email = requestedEmail,
            phone = phone.trim(),
            bio = bio.trim(),
            avatarIndex = avatarIndex
        )
        userDao.updateUser(updatedEntity)
        val updatedProfile = entityToProfile(updatedEntity)
        _currentUser.value = updatedProfile
        return Result.success(updatedProfile)
    }

    private fun getGuestProfile(): UserProfile {
        return UserProfile(
            id = "guest",
            name = "زائر FOKOF2 (Guest)",
            email = "guest@fokof2.ai",
            phone = "",
            bio = "وضع الزائر • استكشف القوالب وصمم بحرية عبر استوديو FOKOF2",
            isVip = false,
            isGuest = true,
            avatarIndex = 0,
            memberSince = "2026"
        )
    }

    private fun entityToProfile(entity: UserEntity): UserProfile {
        val dateStr = SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(Date(entity.createdAt))
        return UserProfile(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
            bio = entity.bio,
            isVip = entity.isVip,
            isGuest = false,
            role = entity.role,
            avatarIndex = entity.avatarIndex,
            memberSince = dateStr
        )
    }

    fun lockOwnerControl() {
        ownerControlUnlocked = false
        ownerControlState.value = false
    }

    suspend fun verifyOwnerCredentials(password: String): Result<Boolean> {
        ensureTemplatesInitialized()
        val cleanPass = password
        if (cleanPass.isBlank()) {
            return Result.failure(Exception("يرجى إدخال كلمة المرور للتحقق من هوية المالك"))
        }
        val current = _currentUser.value
        if (!current.isGuest && (current.role == "OWNER" || current.role == "ADMIN")) {
            val user = userDao.getUserById(current.id)
            if (user != null && SecurityUtils.verifyPassword(cleanPass, user.password)) {
                ownerControlUnlocked = true
                ownerControlState.value = true
                return Result.success(true)
            }
        }
        return Result.failure(Exception("كلمة مرور إدارة المالك غير صحيحة. لا يمكن الوصول بدون تصريح معتمد."))
    }

    // ---------------- USER & ADMIN MANAGEMENT ----------------
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun updateUserRole(userId: String, role: String) {
        if (!hasOwnerOnlyPermission() || role !in setOf("USER", "ADMIN")) return
        val target = userDao.getUserById(userId) ?: return
        if (target.role == "OWNER" || userId == _currentUser.value.id) return
        userDao.updateUserRole(userId, role)
    }

    suspend fun updateUserVipStatus(userId: String, isVip: Boolean) {
        if (!hasOwnerControlPermission()) return
        val target = userDao.getUserById(userId) ?: return
        if (target.role == "OWNER") return
        userDao.updateUserVipStatus(userId, isVip)
    }

    suspend fun deleteUser(userId: String) {
        if (!hasOwnerOnlyPermission() || userId == _currentUser.value.id) return
        val target = userDao.getUserById(userId) ?: return
        if (target.role == "OWNER") return
        userDao.deleteUser(userId)
    }

    suspend fun recoverPassword(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(Exception("البريد الإلكتروني المدخل غير مسجل لدينا."))
        return Result.failure(Exception("استعادة كلمة المرور عبر البريد غير مفعّلة في هذه النسخة. لم يتم إرسال أي رسالة.") )
    }

    // ---------------- USER-PROTECTED PROJECTS CRUD ----------------

    private fun projectEntityToTemplate(entity: ProjectEntity): DesignTemplate = DesignTemplate(
        id = entity.id,
        titleAr = entity.title,
        titleEn = entity.title,
        category = entity.category,
        aspectRatio = "1:1",
        isPremium = false,
        priceEgp = 0,
        bgStartHex = entity.bgStartHex,
        bgEndHex = entity.bgEndHex,
        previewDrawableRes = entity.bgImageRes,
        imageUri = entity.bgImageUri,
        elements = parseElementsJson(entity.elementsJson)
    )

    // Project queries always use the authenticated account; guests see no saved private work.
    fun getProjectsForUser(userId: String): Flow<List<DesignTemplate>> {
        val current = _currentUser.value
        if (current.isGuest || userId != current.id) return flowOf(emptyList())
        return projectDao.getProjectsByUser(current.id).map { rows -> rows.map(::projectEntityToTemplate) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedProjects: Flow<List<DesignTemplate>> = _currentUser.flatMapLatest { current ->
        if (current.isGuest) flowOf(emptyList())
        else projectDao.getProjectsByUser(current.id).map { rows -> rows.map(::projectEntityToTemplate) }
    }

    suspend fun saveProject(
        title: String,
        category: String,
        elements: List<CanvasElement>,
        bgStart: String,
        bgEnd: String,
        userId: String = _currentUser.value.id,
        bgImageRes: Int? = null,
        bgImageUri: String? = null
    ): String {
        val current = _currentUser.value
        if (current.isGuest || userId != current.id) return ""
        val newId = "prj_${UUID.randomUUID().toString().take(8)}"
        val entity = ProjectEntity(
            id = newId,
            userId = current.id,
            title = title,
            category = category,
            elementsJson = serializeElementsJson(elements),
            bgStartHex = bgStart,
            bgEndHex = bgEnd,
            bgImageRes = bgImageRes,
            bgImageUri = bgImageUri,
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(entity)
        return newId
    }

    suspend fun renameProject(projectId: String, newTitle: String) {
        val current = _currentUser.value
        if (current.isGuest || newTitle.isBlank()) return
        val existing = projectDao.getProjectById(projectId) ?: return
        if (existing.userId != current.id) return
        projectDao.renameProject(projectId, newTitle.trim())
    }

    suspend fun duplicateProject(projectId: String, copySuffix: String = " (نسخة)") {
        val current = _currentUser.value
        if (current.isGuest) return
        val existing = projectDao.getProjectById(projectId) ?: return
        if (existing.userId != current.id) return
        val newId = "prj_${UUID.randomUUID().toString().take(8)}"
        val duplicate = existing.copy(
            id = newId,
            userId = current.id,
            title = "${existing.title}$copySuffix",
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(duplicate)
    }

    suspend fun deleteProject(id: String) {
        val current = _currentUser.value
        if (current.isGuest) return
        val existing = projectDao.getProjectById(id) ?: return
        if (existing.userId != current.id) return
        projectDao.deleteProject(id)
    }

    // ---------------- TEMPLATES STREAM & ADMIN ----------------

    val publishedTemplates: Flow<List<DesignTemplate>> = templateDao.getPublishedTemplates().map { entities ->
        entities.map { entityToTemplate(it) }.filter { com.example.data.templates.CatalogTemplates.isShownInTemplatesTab(it) }
    }

    val allTemplatesForAdmin: Flow<List<DesignTemplate>> = templateDao.getAllTemplates().map { entities ->
        entities.map { entityToTemplate(it) }
    }

    suspend fun addTemplate(template: DesignTemplate) {
        if (!hasOwnerControlPermission()) return
        templateDao.insertTemplate(templateToEntity(template))
    }

    suspend fun updateTemplate(template: DesignTemplate) {
        if (!hasOwnerControlPermission()) return
        templateDao.insertTemplate(templateToEntity(template))
    }

    suspend fun duplicateTemplate(template: DesignTemplate) {
        if (!hasOwnerControlPermission()) return
        val newId = "tpl_copy_${System.currentTimeMillis() % 100000}"
        val copy = template.copy(
            id = newId,
            titleAr = "${template.titleAr} (نسخة)",
            titleEn = "${template.titleEn} (Copy)"
        )
        templateDao.insertTemplate(templateToEntity(copy))
    }

    suspend fun deleteTemplate(id: String) {
        if (!hasOwnerControlPermission()) return
        templateDao.deleteTemplate(id)
    }

    suspend fun updateTemplatePrice(id: String, newPriceEgp: Int) {
        if (!hasOwnerControlPermission() || newPriceEgp !in 0..1_000_000) return
        templateDao.updatePrice(id, newPriceEgp)
    }

    suspend fun toggleTemplatePublishStatus(id: String, isPublished: Boolean) {
        if (!hasOwnerControlPermission()) return
        templateDao.updatePublishStatus(id, isPublished)
    }

    // ---------------- PURCHASES FLOW & ACTIONS ----------------

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPurchases: Flow<List<PurchasedTemplateItem>> = _currentUser.flatMapLatest { current ->
        if (current.isGuest) {
            flowOf(emptyList())
        } else {
            combine(
                purchaseDao.getPurchasesByUser(current.id),
                bundleCreditDao.getCreditsByUser(current.id)
            ) { purchases, credits ->
                val templateItems = purchases.map { entity ->
                    val elements = parseElementsJson(entity.elementsJson)
                    val template = DesignTemplate(
                        id = entity.templateId,
                        titleAr = entity.title,
                        titleEn = entity.title,
                        category = "Purchased",
                        priceEgp = entity.priceEgp,
                        isPremium = true,
                        elements = elements,
                        bgStartHex = entity.bgStartHex,
                        bgEndHex = entity.bgEndHex
                    )
                    PurchasedTemplateItem(
                        orderId = entity.orderId,
                        templateId = entity.templateId,
                        titleAr = entity.title,
                        titleEn = entity.title,
                        priceEgp = entity.priceEgp,
                        purchaseDate = entity.purchaseDate,
                        paymentMethod = entity.paymentMethod,
                        template = template
                    )
                }
                val creditItems = credits.map { credit ->
                    val emptyTemplate = DesignTemplate(
                        id = "bundle-credit-${credit.id}",
                        titleAr = credit.bundleTitle,
                        titleEn = credit.bundleTitle,
                        category = "Bundle credits",
                        priceEgp = credit.priceEgp,
                        isPremium = true,
                        elements = emptyList(),
                        bgStartHex = "#F8F5EC",
                        bgEndHex = "#E7D7A5"
                    )
                    PurchasedTemplateItem(
                        orderId = credit.id,
                        templateId = credit.bundleId,
                        titleAr = credit.bundleTitle,
                        titleEn = credit.bundleTitle,
                        priceEgp = credit.priceEgp,
                        purchaseDate = credit.purchaseDate,
                        paymentMethod = credit.paymentMethod,
                        template = emptyTemplate,
                        isBundleCredit = true,
                        totalCredits = credit.totalCredits,
                        remainingCredits = credit.remainingCredits
                    )
                }
                templateItems + creditItems
            }
        }
    }

    /** Uses one purchased bundle credit for the selected template. */
    suspend fun redeemBundleCredit(templateId: String): Boolean = database.withTransaction {
        val current = _currentUser.value
        if (current.isGuest) return@withTransaction false
        val template = templateDao.getTemplateById(templateId) ?: return@withTransaction false
        if (!template.isPublished || template.status != "PUBLISHED" || template.priceEgp <= 0) return@withTransaction false
        if (purchaseDao.getPurchaseByUserAndTemplate(current.id, templateId) != null) return@withTransaction false
        val credit = bundleCreditDao.getAvailableCredit(current.id) ?: return@withTransaction false
        if (credit.remainingCredits <= 0) return@withTransaction false
        purchaseDao.insertPurchase(
            PurchaseEntity(
                orderId = "ORD-${UUID.randomUUID()}",
                templateId = template.id,
                title = template.titleAr,
                priceEgp = 0,
                purchaseDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()),
                paymentMethod = "رصيد باقة القوالب",
                elementsJson = template.elementsJson,
                bgStartHex = template.bgStartHex,
                bgEndHex = template.bgEndHex,
                userId = current.id
            )
        )
        bundleCreditDao.updateRemaining(credit.id, credit.remainingCredits - 1)
        true
    }

    // Paid template and bundle ownership is granted only after manual payment approval.

    // ---------------- MANUAL PAYMENTS ----------------

    val manualPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    suspend fun submitManualPayment(
        userEmail: String,
        planName: String,
        amountFormatted: String,
        paymentMethod: String,
        transactionRef: String,
        receiptImageUri: String? = null,
        notes: String = ""
    ): Boolean {
        val current = _currentUser.value
        val cleanEmail = userEmail.trim().lowercase()
        if (current.isGuest || current.email.trim().lowercase() != cleanEmail) return false

        val enabledMethods = paymentMethodConfigDao.getEnabledPaymentMethodsOnce()
        val suppliedMethod = paymentMethod.trim().lowercase()
        val selectedMethod = enabledMethods.firstOrNull { method ->
            val displayWithAccount = if (method.accountNumber.isBlank()) method.nameAr else "${method.nameAr} - ${method.accountNumber}"
            suppliedMethod == method.id.lowercase() ||
                suppliedMethod == method.nameAr.trim().lowercase() ||
                suppliedMethod == method.nameEn.trim().lowercase() ||
                suppliedMethod == displayWithAccount.trim().lowercase() ||
                (method.accountNumber.isNotBlank() && suppliedMethod.contains(method.accountNumber) &&
                    (suppliedMethod.contains(method.nameEn.lowercase()) || suppliedMethod.contains(method.nameAr.substringBefore("(").trim().lowercase())))
        } ?: return false

        if (selectedMethod.accountNumber.isBlank() || selectedMethod.instructionsAr.isBlank()) return false
        if (userEmail.isBlank() || planName.isBlank() || amountFormatted.isBlank() || transactionRef.isBlank()) {
            return false
        }
        val registeredUser = userDao.getUserByEmail(cleanEmail) ?: return false
        if (registeredUser.role == "OWNER_SETUP_REQUIRED") return false
        val submittedAmount = amountFormatted.filter(Char::isDigit).toIntOrNull() ?: return false
        val expectedAmount = when {
            planName.startsWith("TEMPLATE:") -> {
                val templateId = planName.substringAfter("TEMPLATE:").trim()
                if (purchaseDao.getPurchaseByUserAndTemplate(current.id, templateId) != null) return false
                templateDao.getTemplateById(templateId)?.priceEgp
            }
            planName.startsWith("BUNDLE:") -> {
                val bundleId = planName.substringAfter("BUNDLE:").trim()
                bundleDao.getBundleById(bundleId)?.priceEgp
            }
            else -> subscriptionPlanDao.getPlanByName(planName.trim())?.priceEgp
        } ?: return false
        if (submittedAmount != expectedAmount || expectedAmount <= 0) return false

        val payment = PaymentEntity(
            id = "PAY-${UUID.randomUUID()}",
            userEmail = cleanEmail,
            planName = planName.trim(),
            amountFormatted = amountFormatted.trim(),
            paymentMethod = selectedMethod.nameAr,
            transactionRef = transactionRef.trim(),
            dateString = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
            status = "PENDING",
            receiptImageUri = receiptImageUri,
            notes = notes
        )
        paymentDao.insertPayment(payment)
        return true
    }

    suspend fun approvePayment(paymentId: String): Boolean = database.withTransaction {
        if (!hasPaymentAdminPermission()) return@withTransaction false

        val payment = paymentDao.getPaymentById(paymentId) ?: return@withTransaction false
        if (payment.status != "PENDING") return@withTransaction false
        val user = userDao.getUserByEmail(payment.userEmail) ?: return@withTransaction false
        val amount = payment.amountFormatted.filter(Char::isDigit).toIntOrNull() ?: return@withTransaction false

        // This local app records the owner's manual decision; it never verifies bank transfers itself.
        when {
            payment.planName.startsWith("TEMPLATE:") -> {
                val templateId = payment.planName.substringAfter("TEMPLATE:").trim()
                val template = templateDao.getTemplateById(templateId) ?: return@withTransaction false
                if (amount != template.priceEgp || amount <= 0) return@withTransaction false
                if (purchaseDao.getPurchaseByUserAndTemplate(user.id, templateId) != null) return@withTransaction false
                val orderId = "ORD-${UUID.randomUUID()}"
                purchaseDao.insertPurchase(
                    PurchaseEntity(
                        orderId = orderId,
                        templateId = template.id,
                        title = template.titleAr,
                        priceEgp = amount,
                        purchaseDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()),
                        paymentMethod = payment.paymentMethod,
                        elementsJson = template.elementsJson,
                        bgStartHex = template.bgStartHex,
                        bgEndHex = template.bgEndHex,
                        userId = user.id
                    )
                )
            }
            payment.planName.startsWith("BUNDLE:") -> {
                val bundleId = payment.planName.substringAfter("BUNDLE:").trim()
                val bundle = bundleDao.getBundleById(bundleId) ?: return@withTransaction false
                if (amount != bundle.priceEgp || amount <= 0 || bundle.count <= 0) return@withTransaction false
                bundleCreditDao.insertCredit(
                    BundleCreditEntity(
                        id = "BCR-${UUID.randomUUID()}",
                        userId = user.id,
                        bundleId = bundle.id,
                        bundleTitle = "باقة ${bundle.count} قوالب",
                        totalCredits = bundle.count,
                        remainingCredits = bundle.count,
                        priceEgp = amount,
                        purchaseDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()),
                        paymentMethod = payment.paymentMethod
                    )
                )
            }
            else -> {
                val plan = subscriptionPlanDao.getPlanByName(payment.planName) ?: return@withTransaction false
                if (amount != plan.priceEgp || amount <= 0) return@withTransaction false
                val vipUser = user.copy(isVip = true)
                userDao.updateUser(vipUser)
                if (_currentUser.value.id == vipUser.id) {
                    _currentUser.value = entityToProfile(vipUser)
                }
            }
        }
        paymentDao.updateStatus(paymentId, "APPROVED")
        true
    }

    suspend fun rejectPayment(paymentId: String): Boolean = database.withTransaction {
        if (!hasPaymentAdminPermission()) return@withTransaction false
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withTransaction false
        if (payment.status != "PENDING") return@withTransaction false
        paymentDao.updateStatus(paymentId, "REJECTED")
        true
    }

    suspend fun toggleFeaturedTemplate(id: String, isFeatured: Boolean) {
        if (!hasOwnerControlPermission()) return
        templateDao.updateFeaturedStatus(id, isFeatured)
    }

    // ---------------- JSON SERIALIZATION HELPERS ----------------

    private fun templateToEntity(t: DesignTemplate): TemplateEntity {
        return TemplateEntity(
            id = t.id,
            titleAr = t.titleAr,
            titleEn = t.titleEn,
            descriptionAr = t.descriptionAr,
            descriptionEn = t.descriptionEn,
            category = t.category,
            aspectRatio = t.aspectRatio,
            isPremium = t.isPremium,
            pricingType = t.pricingType,
            proTierName = t.proTier?.name,
            priceEgp = t.priceEgp,
            rating = t.rating,
            salesCount = t.salesCount,
            elementsJson = serializeElementsJson(t.elements),
            bgStartHex = t.bgStartHex,
            bgEndHex = t.bgEndHex,
            isPublished = t.isPublished,
            isFeatured = t.isFeatured,
            status = t.status,
            templateType = t.templateType,
            previewDrawableRes = t.previewDrawableRes,
            imageUri = t.imageUri,
            designFileUri = t.designFileUri,
            designFileName = t.designFileName
        )
    }

    private fun entityToTemplate(e: TemplateEntity): DesignTemplate {
        val tier = e.proTierName?.let { name ->
            runCatching { ProTier.valueOf(name) }.getOrNull()
        }
        return DesignTemplate(
            id = e.id,
            titleAr = e.titleAr,
            titleEn = e.titleEn,
            descriptionAr = e.descriptionAr,
            descriptionEn = e.descriptionEn,
            category = e.category,
            aspectRatio = e.aspectRatio,
            isPremium = e.isPremium,
            pricingType = e.pricingType,
            proTier = tier,
            priceEgp = e.priceEgp,
            rating = e.rating,
            salesCount = e.salesCount,
            previewDrawableRes = e.previewDrawableRes,
            elements = parseElementsJson(e.elementsJson),
            bgStartHex = e.bgStartHex,
            bgEndHex = e.bgEndHex,
            isPublished = e.isPublished,
            isFeatured = e.isFeatured,
            status = e.status,
            templateType = e.templateType,
            imageUri = e.imageUri,
            designFileUri = e.designFileUri,
            designFileName = e.designFileName
        )
    }

    private fun serializeElementsJson(elements: List<CanvasElement>): String {
        val jsonArray = JSONArray()
        elements.forEach { elem ->
            val obj = JSONObject().apply {
                put("id", elem.id)
                put("type", elem.type.name)
                put("text", elem.text)
                put("x", elem.x.toDouble())
                put("y", elem.y.toDouble())
                put("scale", elem.scale.toDouble())
                put("rotation", elem.rotation.toDouble())
                put("colorHex", elem.colorHex)
                put("fontSize", elem.fontSize.toDouble())
                put("isBold", elem.isBold)
                put("shapeType", elem.shapeType.name)
                put("width", elem.width.toDouble())
                put("height", elem.height.toDouble())
                put("opacity", elem.opacity.toDouble())
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseElementsJson(jsonStr: String?): List<CanvasElement> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CanvasElement>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val type = runCatching { CanvasElementType.valueOf(obj.optString("type", "TEXT")) }.getOrDefault(CanvasElementType.TEXT)
                val shapeType = runCatching { ShapeType.valueOf(obj.optString("shapeType", "ROUNDED_RECT")) }.getOrDefault(ShapeType.ROUNDED_RECT)
                list.add(
                    CanvasElement(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = type,
                        text = obj.optString("text", ""),
                        x = obj.optDouble("x", 150.0).toFloat(),
                        y = obj.optDouble("y", 150.0).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        colorHex = obj.optString("colorHex", "#D4AF37"),
                        fontSize = obj.optDouble("fontSize", 22.0).toFloat(),
                        isBold = obj.optBoolean("isBold", true),
                        shapeType = shapeType,
                        width = obj.optDouble("width", 140.0).toFloat(),
                        height = obj.optDouble("height", 70.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat()
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ---------------- WEBSITE CONVERTERS ----------------
    private fun featureTogglesToJson(toggles: WebsiteFeatureToggles): String {
        return JSONObject().apply {
            put("whatsapp", toggles.featureWhatsappOrders)
            put("booking", toggles.featureBookingSystem)
            put("catalog", toggles.featureProductCatalog)
            put("reviews", toggles.featureCustomerReviews)
            put("faq", toggles.featureFaqSection)
            put("maps", toggles.featureGoogleMaps)
            put("calling", toggles.featureDirectCalling)
            put("domain", toggles.featureCustomDomain)
        }.toString()
    }

    private fun jsonToFeatureToggles(jsonStr: String?): WebsiteFeatureToggles {
        if (jsonStr.isNullOrBlank()) return WebsiteFeatureToggles()
        return try {
            val obj = JSONObject(jsonStr)
            WebsiteFeatureToggles(
                featureWhatsappOrders = obj.optBoolean("whatsapp", true),
                featureBookingSystem = obj.optBoolean("booking", false),
                featureProductCatalog = obj.optBoolean("catalog", true),
                featureCustomerReviews = obj.optBoolean("reviews", true),
                featureFaqSection = obj.optBoolean("faq", true),
                featureGoogleMaps = obj.optBoolean("maps", true),
                featureDirectCalling = obj.optBoolean("calling", true),
                featureCustomDomain = obj.optBoolean("domain", false)
            )
        } catch (e: Exception) {
            WebsiteFeatureToggles()
        }
    }

    private fun websiteItemsToJson(items: List<WebsiteItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("nameAr", item.nameAr)
                put("nameEn", item.nameEn)
                put("priceEgp", item.priceEgp)
                put("originalPriceEgp", item.originalPriceEgp)
                put("descriptionAr", item.descriptionAr)
                put("category", item.category)
                put("badgeAr", item.badgeAr ?: "")
                put("isAvailable", item.isAvailable)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToWebsiteItems(jsonStr: String?): List<WebsiteItem> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<WebsiteItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val badge = obj.optString("badgeAr", "")
                list.add(
                    WebsiteItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        nameAr = obj.optString("nameAr", ""),
                        nameEn = obj.optString("nameEn", ""),
                        priceEgp = obj.optInt("priceEgp", 0),
                        originalPriceEgp = obj.optInt("originalPriceEgp", 0),
                        descriptionAr = obj.optString("descriptionAr", ""),
                        category = obj.optString("category", "عام"),
                        badgeAr = if (badge.isNotBlank()) badge else null,
                        isAvailable = obj.optBoolean("isAvailable", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun entityToWebsiteTemplate(e: WebsiteTemplateEntity): WebsiteTemplate {
        return WebsiteTemplate(
            id = e.id,
            businessType = e.businessType,
            businessTypeNameAr = e.businessTypeNameAr,
            titleAr = e.titleAr,
            titleEn = e.titleEn,
            taglineAr = e.taglineAr,
            descriptionAr = e.descriptionAr,
            primaryColorHex = e.primaryColorHex,
            secondaryColorHex = e.secondaryColorHex,
            darkBgHex = e.darkBgHex,
            priceEgp = e.priceEgp,
            isPremium = e.isPremium,
            isPublished = e.isPublished,
            featureToggles = jsonToFeatureToggles(e.featureTogglesJson),
            sampleHeadline = e.sampleHeadline,
            sampleSubheadline = e.sampleSubheadline,
            sampleAbout = e.sampleAbout,
            sampleAddress = e.sampleAddress,
            sampleWorkingHours = e.sampleWorkingHours,
            samplePhone = e.samplePhone,
            sampleWhatsapp = e.sampleWhatsapp,
            sampleItems = jsonToWebsiteItems(e.sampleItemsJson)
        )
    }

    private fun entityToUserWebsite(e: UserWebsiteEntity): UserWebsite {
        return UserWebsite(
            id = e.id,
            userId = e.userId,
            templateId = e.templateId,
            siteTitle = e.siteTitle,
            siteSlug = e.siteSlug,
            businessType = e.businessType,
            headline = e.headline,
            subheadline = e.subheadline,
            aboutText = e.aboutText,
            whatsappNumber = e.whatsappNumber,
            phoneNumber = e.phoneNumber,
            address = e.address,
            workingHours = e.workingHours,
            primaryColorHex = e.primaryColorHex,
            items = jsonToWebsiteItems(e.itemsJson),
            featureToggles = jsonToFeatureToggles(e.featureTogglesJson),
            isPublished = e.isPublished,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt,
            extras = jsonToSiteExtras(e.extrasJson)
        )
    }

    private fun userWebsiteToEntity(w: UserWebsite): UserWebsiteEntity {
        return UserWebsiteEntity(
            id = w.id,
            userId = w.userId,
            templateId = w.templateId,
            siteTitle = w.siteTitle,
            siteSlug = w.siteSlug,
            businessType = w.businessType,
            headline = w.headline,
            subheadline = w.subheadline,
            aboutText = w.aboutText,
            whatsappNumber = w.whatsappNumber,
            phoneNumber = w.phoneNumber,
            address = w.address,
            workingHours = w.workingHours,
            primaryColorHex = w.primaryColorHex,
            itemsJson = websiteItemsToJson(w.items),
            featureTogglesJson = featureTogglesToJson(w.featureToggles),
            isPublished = w.isPublished,
            createdAt = w.createdAt,
            updatedAt = w.updatedAt,
            extrasJson = siteExtrasToJson(w.extras)
        )
    }

    private fun siteExtrasToJson(x: SiteExtras): String {
        val o = JSONObject()
        val t = JSONArray()
        x.testimonials.forEach { t.put(JSONObject().put("name", it.name).put("text", it.text).put("stars", it.stars)) }
        o.put("testimonials", t)
        val g = JSONArray(); x.gallery.forEach { g.put(it) }
        o.put("gallery", g)
        o.put("facebook", x.facebook).put("instagram", x.instagram).put("tiktok", x.tiktok)
        o.put("youtube", x.youtube).put("telegram", x.telegram).put("website", x.website)
        return o.toString()
    }

    private fun jsonToSiteExtras(json: String?): SiteExtras {
        if (json.isNullOrBlank()) return SiteExtras()
        return try {
            val o = JSONObject(json)
            val ta = o.optJSONArray("testimonials") ?: JSONArray()
            val ga = o.optJSONArray("gallery") ?: JSONArray()
            SiteExtras(
                testimonials = (0 until ta.length()).map { i -> ta.getJSONObject(i).let { SiteTestimonial(it.optString("name"), it.optString("text"), it.optInt("stars", 5)) } },
                gallery = (0 until ga.length()).map { ga.getString(it) },
                facebook = o.optString("facebook"), instagram = o.optString("instagram"), tiktok = o.optString("tiktok"),
                youtube = o.optString("youtube"), telegram = o.optString("telegram"), website = o.optString("website")
            )
        } catch (e: Exception) { SiteExtras() }
    }

    // ---------------- WEBSITE ANALYTICS & BOOKINGS ----------------

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWebsiteAnalytics(siteId: String): Flow<WebsiteAnalytics> =
        combine(_currentUser, ownerControlState) { current, unlocked -> current to unlocked }
            .flatMapLatest { (current, unlocked) ->
            if (current.isGuest) {
                flowOf(WebsiteAnalytics(siteId = siteId))
            } else {
                flow {
                    val site = userWebsiteDao.getWebsiteById(siteId)
                    if (site == null || (site.userId != current.id && !(unlocked && current.role in setOf("OWNER", "ADMIN")))) {
                        emit(WebsiteAnalytics(siteId = siteId))
                    } else {
                        emitAll(websiteAnalyticsDao.getAnalytics(siteId).map { entity ->
                            if (entity != null) WebsiteAnalytics(
                                siteId = entity.siteId,
                                viewsCount = entity.viewsCount,
                                whatsappClicksCount = entity.whatsappClicksCount,
                                catalogViewsCount = entity.catalogViewsCount,
                                lastVisitedAt = entity.lastVisitedAt
                            ) else WebsiteAnalytics(siteId = siteId)
                        })
                    }
                }
            }
        }

    suspend fun recordWebsiteView(siteId: String) {
        val existing = websiteAnalyticsDao.getAnalyticsSync(siteId)
        if (existing == null) {
            websiteAnalyticsDao.insertOrUpdate(
                WebsiteAnalyticsEntity(
                    siteId = siteId,
                    viewsCount = 1,
                    whatsappClicksCount = 0,
                    catalogViewsCount = 1
                )
            )
        } else {
            websiteAnalyticsDao.incrementViews(siteId)
        }
    }

    suspend fun recordWhatsappClick(siteId: String) {
        val existing = websiteAnalyticsDao.getAnalyticsSync(siteId)
        if (existing == null) {
            websiteAnalyticsDao.insertOrUpdate(
                WebsiteAnalyticsEntity(
                    siteId = siteId,
                    viewsCount = 1,
                    whatsappClicksCount = 1,
                    catalogViewsCount = 1
                )
            )
        } else {
            websiteAnalyticsDao.incrementWhatsappClicks(siteId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWebsiteBookings(siteId: String): Flow<List<WebsiteBooking>> =
        combine(_currentUser, ownerControlState) { current, unlocked -> current to unlocked }
            .flatMapLatest { (current, unlocked) ->
            if (current.isGuest) {
                flowOf(emptyList())
            } else {
                flow {
                    val site = userWebsiteDao.getWebsiteById(siteId)
                    if (site == null || (site.userId != current.id && !(unlocked && current.role in setOf("OWNER", "ADMIN")))) {
                        emit(emptyList())
                    } else {
                        emitAll(websiteBookingDao.getBookingsForSite(siteId).map { entities ->
                            entities.map { e ->
                                WebsiteBooking(
                                    id = e.id,
                                    siteId = e.siteId,
                                    customerName = e.customerName,
                                    customerPhone = e.customerPhone,
                                    serviceName = e.serviceName,
                                    bookingDate = e.bookingDate,
                                    bookingTime = e.bookingTime,
                                    notes = e.notes,
                                    status = e.status,
                                    createdAt = e.createdAt
                                )
                            }
                        })
                    }
                }
            }
        }

    suspend fun submitBooking(booking: WebsiteBooking) {
        val site = userWebsiteDao.getWebsiteById(booking.siteId) ?: return
        if (!site.isPublished) return
        val name = booking.customerName.trim().take(80)
        val phone = booking.customerPhone.trim().take(32)
        val service = booking.serviceName.trim().take(120)
        val date = booking.bookingDate.trim().take(32)
        val time = booking.bookingTime.trim().take(24)
        val notes = booking.notes.trim().take(500)
        if (name.isBlank() || phone.isBlank() || service.isBlank() || date.isBlank() || time.isBlank()) return
        websiteBookingDao.insertBooking(
            WebsiteBookingEntity(
                id = "BOOK-${UUID.randomUUID()}",
                siteId = site.id,
                customerName = name,
                customerPhone = phone,
                serviceName = service,
                bookingDate = date,
                bookingTime = time,
                notes = notes,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateBookingStatus(id: String, status: String) {
        if (status !in setOf("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED")) return
        val booking = websiteBookingDao.getBookingById(id) ?: return
        val site = userWebsiteDao.getWebsiteById(booking.siteId) ?: return
        val current = _currentUser.value
        if (current.isGuest) return
        // Site owners manage their own bookings; authenticated owner/admin can manage all.
        if (site.userId != current.id && !hasOwnerControlPermission()) return
        websiteBookingDao.updateBookingStatus(id, status)
    }
}


