package com.example

import com.example.data.templates.CatalogTemplates
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Templates tab must show only photo-art templates plus the designed default categories (logos, cards, CVs). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplateVisibilityTest {
    private val catalog by lazy {
        val dir = File("src/main/assets/templates")
        CatalogTemplates.fileNames(dir.list() ?: emptyArray()).flatMap { CatalogTemplates.parse(File(dir, it).readText()) }
    }

    @Test fun procedural_catalog_is_hidden_and_photo_catalog_is_shown() {
        val procedural = catalog.filter { it.id.startsWith("cg_") }
        val photo = catalog.filter { it.id.startsWith("ph") }
        assertTrue("expected procedural catalog templates", procedural.size >= 720)
        assertTrue("expected photo templates", photo.size >= 78)
        assertTrue("a procedural template is visible", procedural.none { CatalogTemplates.isShownInTemplatesTab(it) })
        assertTrue("a photo template is hidden", photo.all { CatalogTemplates.isShownInTemplatesTab(it) })
        assertEquals(catalog.size, procedural.size + photo.size)
    }

    @Test fun only_designed_default_categories_stay_visible() {
        val defaults = FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates()
        val shown = defaults.filter { CatalogTemplates.isShownInTemplatesTab(it) }
        assertTrue("no defaults should stay visible outside designed categories",
            shown.all { it.category in CatalogTemplates.DESIGNED_DEFAULT_CATEGORIES })
        assertTrue("designed categories must not go empty", CatalogTemplates.DESIGNED_DEFAULT_CATEGORIES.all { c -> shown.any { it.category == c } })
        assertFalse(defaults.none { CatalogTemplates.isShownInTemplatesTab(it) })
        println("VISIBILITY_OK defaults_total=${defaults.size} defaults_shown=${shown.size} by_category=" +
            shown.groupBy { it.category }.mapValues { it.value.size } + " photo=${catalog.count { it.id.startsWith("ph") }}")
    }
}
