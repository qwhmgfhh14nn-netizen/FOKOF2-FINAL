package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.BarayaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A phone that already holds the old banded designs must get the clean ones after updating. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplateUpgradeTest {
    @Test fun stale_installed_designs_are_rewritten() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        BarayaRepository(ctx).ensureTemplatesInitialized()
        val dao = AppDatabase.getDatabase(ctx).templateDao()
        val before = dao.getTemplateById("free_social_1") ?: dao.getTemplateById(
            (com.example.data.templates.FreeTemplatesData.getTemplates().first()).id
        )!!
        // Simulate the previous release: old design content and an older stored sync version.
        dao.insertAll(listOf(before.copy(elementsJson = "[{\"type\":\"STALE_BAND\"}]", priceEgp = 77)))
        ctx.getSharedPreferences("baraya_user_prefs", android.content.Context.MODE_PRIVATE)
            .edit().putInt("default_templates_version", 3).commit()
        BarayaRepository(ctx).ensureTemplatesInitialized()
        val after = dao.getTemplateById(before.id)!!
        assertFalse("stale layers were not rewritten", after.elementsJson.contains("STALE_BAND"))
        assertTrue("owner price must be preserved", after.priceEgp == 77)
        val all = com.example.data.templates.FreeTemplatesData.getTemplates()
        println("UPGRADE_REWRITE_OK id=${before.id} total_free=${all.size}")
    }
}
