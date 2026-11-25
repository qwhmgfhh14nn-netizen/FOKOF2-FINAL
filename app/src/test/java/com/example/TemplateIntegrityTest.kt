package com.example

import com.example.data.model.CanvasElementType
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import com.example.data.templates.TemplateDesigner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Plain JVM checks on the 64 default templates (no Gradle-green-means-nice-design shortcuts). */
class TemplateIntegrityTest {
    private val all = FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates()

    private fun heightFor(r: String) = 360f / when (r) { "16:9" -> 16f / 9f; "9:16" -> 9f / 16f; "4:5" -> 0.8f; else -> 1f }

    @Test fun counts_and_unique_ids() {
        assertEquals(50, FreeTemplatesData.getTemplates().size)
        assertEquals(14, ProTemplatesData.getTemplates().size)
        assertEquals(64, all.map { it.id }.toSet().size)
    }

    @Test fun every_template_has_local_art_file() {
        val missing = all.filter { t ->
            val uri = t.imageUri ?: return@filter true
            val name = uri.substringAfterLast('/')
            val f = File("src/main/assets/templates/art/$name")
            !(f.exists() && f.length() > 1_000)
        }.map { it.id }
        assertTrue("templates without a usable local art file: $missing", missing.isEmpty())
    }

    @Test fun designed_layouts_fit_their_own_aspect_ratio() {
        val problems = ArrayList<String>()
        for (t in all) {
            val d = TemplateDesigner.enrich(t)
            val h = heightFor(d.aspectRatio)
            if (d.elements.size <= t.elements.size) problems += "${t.id}: no design layers added"
            if (d.elements.none { it.type == CanvasElementType.TEXT && it.text.isNotBlank() }) problems += "${t.id}: no text"
            d.elements.filter { it.type == CanvasElementType.TEXT }.forEach { e ->
                if (e.x < 0f || e.x > 360f || e.y < 0f || e.y > h) problems += "${t.id}: text '${e.text}' outside canvas (${e.x},${e.y}) h=$h"
            }
            if (d.elements.map { it.id }.toSet().size != d.elements.size) problems += "${t.id}: duplicate element ids"
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun layouts_vary_between_templates() {
        val signatures = all.map { t ->
            TemplateDesigner.enrich(t).elements.filter { it.id.startsWith("d_") }.map { it.shapeType.name + it.width.toInt() }.joinToString("|")
        }.toSet()
        assertTrue("expected varied archetypes, got ${signatures.size}", signatures.size >= 6)
    }
}
