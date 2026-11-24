#!/usr/bin/env python3
"""Lightweight source-contract checks for the FOKOF2 Android handoff.
These checks are not a substitute for Gradle compilation or runtime testing.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

checks = {
    "Room schema version 13": 'version = 13' in read('app/src/main/java/com/example/data/local/AppDatabase.kt'),
    "Room migration 11 to 12": 'Migration(11, 12)' in read('app/src/main/java/com/example/data/local/AppDatabase.kt'),
    "Room migration 12 to 13": 'Migration(12, 13)' in read('app/src/main/java/com/example/data/local/AppDatabase.kt'),
    "Purchase records scoped by user": 'WHERE userId = :userId' in read('app/src/main/java/com/example/data/local/Daos.kt'),
    "Purchase records carry user id": 'val userId: String = ""' in read('app/src/main/java/com/example/data/local/Entities.kt'),
    "Saved projects query by owner": 'getProjectsByUser(current.id)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Project mutations check owner": 'if (existing.userId != current.id) return' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Manual payments require current account": 'current.email.trim().lowercase() != cleanEmail' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Payment approval checks admin": 'if (!hasPaymentAdminPermission()) return@withTransaction false' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Payment approval only processes pending": 'if (payment.status != "PENDING") return@withTransaction false' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Bundle credits stored independently": 'data class BundleCreditEntity' in read('app/src/main/java/com/example/data/local/Entities.kt'),
    "Bundle approval creates credits": 'bundleCreditDao.insertCredit(' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Bundle credits can be redeemed": 'suspend fun redeemBundleCredit(templateId: String)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Bundle redemption requires published paid template": 'if (!template.isPublished || template.status != "PUBLISHED" || template.priceEgp <= 0)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Analytics scoped to site owner": 'fun getWebsiteAnalytics(siteId: String)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt') and 'ownerControlState' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Bookings scoped to site owner": 'fun getWebsiteBookings(siteId: String)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt') and 'site.userId != current.id' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Booking submission requires published site": 'if (!site.isPublished) return' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Booking input length limited": 'booking.notes.trim().take(500)' in read('app/src/main/java/com/example/data/repository/BarayaRepository.kt'),
    "Gemini remote calls disabled": 'private fun callGeminiApi' in read('app/src/main/java/com/example/data/ai/GeminiAiService.kt') and 'return null' in read('app/src/main/java/com/example/data/ai/GeminiAiService.kt'),
    "Android app backup disabled": 'android:allowBackup="false"' in read('app/src/main/AndroidManifest.xml'),
    "App version 1.2.0": 'versionName = "1.2.0"' in read('app/build.gradle.kts'),
}

failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'}  {name}")
print(f"\n{len(checks) - len(failed)}/{len(checks)} source-contract checks passed")
if failed:
    raise SystemExit(1)
