package com.example

import com.example.data.templates.DesignEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignEngineTest {
    @Test
    fun cv_templates_unique_and_in_bounds() {
        val all = DesignEngine.cvTemplates()
        assertEquals(180, all.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertEquals(all.size, all.map { it.category + "|" + it.titleAr }.toSet().size)
        all.forEach { t -> assertTrue(t.id, t.elements.size > 10) }
    }
}
