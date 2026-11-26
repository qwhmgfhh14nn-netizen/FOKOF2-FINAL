package com.example

import com.example.data.templates.DesignEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignEngineTest {
    @Test
    fun cv_templates_unique_and_in_bounds() {
        val all = DesignEngine.all()
        assertEquals(200, com.example.data.templates.BookEngine.templates().size)
        assertEquals(200, com.example.data.templates.LogoEngine.templates().size)
        assertEquals(200, com.example.data.templates.InviteEngine.templates().size)
        assertEquals(180, DesignEngine.cvTemplates().size)
        assertEquals(200, com.example.data.templates.CertEngine.templates().size)
        assertEquals(200, com.example.data.templates.CardEngine.templates().size)
        assertEquals(200, com.example.data.templates.MenuEngine.templates().size)
        assertEquals(1380, all.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertEquals(all.size, all.map { it.category + "|" + it.titleAr }.toSet().size)
        all.forEach { t -> assertTrue(t.id, t.elements.size > 8) }
    }
}
