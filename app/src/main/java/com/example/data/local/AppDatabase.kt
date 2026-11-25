package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        PaymentEntity::class,
        PurchaseEntity::class,
        TemplateEntity::class,
        CategoryEntity::class,
        BundleEntity::class,
        SubscriptionPlanEntity::class,
        UserWebsiteEntity::class,
        WebsiteTemplateEntity::class,
        WebsiteSettingsEntity::class,
        WebsiteAnalyticsEntity::class,
        WebsiteBookingEntity::class,
        PaymentMethodConfigEntity::class,
        FeatureAvailabilityEntity::class,
        DiscountCouponEntity::class,
        BundleCreditEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun paymentDao(): PaymentDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun templateDao(): TemplateDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bundleDao(): BundleDao
    abstract fun subscriptionPlanDao(): SubscriptionPlanDao
    abstract fun userWebsiteDao(): UserWebsiteDao
    abstract fun websiteTemplateDao(): WebsiteTemplateDao
    abstract fun websiteSettingsDao(): WebsiteSettingsDao
    abstract fun websiteAnalyticsDao(): WebsiteAnalyticsDao
    abstract fun websiteBookingDao(): WebsiteBookingDao
    abstract fun paymentMethodConfigDao(): PaymentMethodConfigDao
    abstract fun featureAvailabilityDao(): FeatureAvailabilityDao
    abstract fun discountCouponDao(): DiscountCouponDao
    abstract fun bundleCreditDao(): BundleCreditDao

    companion object {
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Existing purchases remain stored but are intentionally unassigned until
                // an administrator can verify their owner; they are not exposed to other accounts.
                database.execSQL("ALTER TABLE user_purchases ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_websites ADD COLUMN extrasJson TEXT NOT NULL DEFAULT '{}'")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS bundle_credits (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "userId TEXT NOT NULL, " +
                        "bundleId TEXT NOT NULL, " +
                        "bundleTitle TEXT NOT NULL, " +
                        "totalCredits INTEGER NOT NULL, " +
                        "remainingCredits INTEGER NOT NULL, " +
                        "priceEgp INTEGER NOT NULL, " +
                        "purchaseDate TEXT NOT NULL, " +
                        "paymentMethod TEXT NOT NULL" +
                    ")"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "baraya_ai_database"
                )
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
