package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Infographics: 10 header styles x 2 bodies (3 stats / 3 steps) x 10 palettes = 200. Canvas 360 x 450 (4:5). */
object InfoEngine {
    private const val W = 360f
    private const val H = 450f
    val styleNames = listOf("شريط", "دوائر", "ماسات", "هلال", "أمواج", "نجوم", "أعمدة", "سداسي", "إطار", "أقواس")

    data class InfoData(val title: String = "", val subtitle: String = "", val items: List<Pair<String, String>> = emptyList())

    fun header(b: B, style: Int, p: Palette) {
        when (style) {
            0 -> { b.rect(0f, 0f, W, 120f, p.accent); b.rect(0f, 120f, W, 124f, p.soft) }
            1 -> { b.rect(0f, 0f, W, 110f, p.accent); for (i in 0..5) b.shape(ShapeType.CIRCLE, 30f + i * 60f, 110f, 50f, 50f, p.accent) }
            2 -> { b.rect(0f, 0f, W, 110f, p.accent); for (i in 0..8) b.shape(ShapeType.DIAMOND, 20f + i * 40f, 112f, 28f, 28f, p.accent) }
            3 -> { b.rect(0f, 0f, W, 120f, p.accent); b.shape(ShapeType.CIRCLE, 40f, 30f, 120f, 120f, p.soft, 0.5f); b.shape(ShapeType.CIRCLE, 60f, 22f, 100f, 100f, p.accent) }
            4 -> { b.rect(0f, 0f, W, 100f, p.accent); for (i in 0..8) b.shape(ShapeType.CIRCLE, 20f + i * 40f, 104f, 44f, 44f, p.accent) }
            5 -> { b.rect(0f, 0f, W, 120f, p.accent); for (i in 0..4) b.shape(ShapeType.STAR, 40f + i * 70f, 100f, 18f, 18f, p.soft, 0.8f) }
            6 -> { for (i in 0..6) b.rect(i * 52f, 0f, i * 52f + 26f, 120f, if (i % 2 == 0) p.accent else p.soft, 0.9f) }
            7 -> { b.rect(0f, 0f, W, 110f, p.accent); b.shape(ShapeType.HEXAGON, W - 40f, 30f, 90f, 80f, p.soft, 0.4f); b.shape(ShapeType.HEXAGON, 30f, 90f, 70f, 62f, p.soft, 0.4f) }
            8 -> { b.rect(0f, 0f, W, 120f, p.accent); b.rect(10f, 10f, W - 10f, 110f, p.bg, 0.0f); b.rect(10f, 10f, W - 10f, 11.5f, p.soft); b.rect(10f, 108.5f, W - 10f, 110f, p.soft) }
            else -> { b.rect(0f, 0f, W, 110f, p.accent); b.shape(ShapeType.CIRCLE, W / 2, 0f, 300f, 140f, p.soft, 0.35f); b.shape(ShapeType.CIRCLE, W / 2, 0f, 220f, 100f, p.soft, 0.35f) }
    }
    }

    fun infographic(layout: Int, p: Palette, d: InfoData = InfoData()): List<CanvasElement> {
        val style = layout / 2
        val steps = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        header(b, style, p)
        b.text(d.title.ifBlank { "عنوان الإنفوجرافيك" }, W / 2, 48f, 26f, p.bg, true)
        b.text(d.subtitle.ifBlank { "جملة قصيرة تشرح الفكرة" }, W / 2, 82f, 13f, p.bg, false, 0.9f)
        val items = if (d.items.size >= 3) d.items else if (steps)
            listOf("ابدأ" to "اكتب فكرتك", "صمم" to "اختر القالب", "انشر" to "شارك مع الناس")
        else listOf("95%" to "رضا العملاء", "250+" to "مشروع منجز", "12" to "سنة خبرة")
        for (i in 0..2) {
            if (steps) {
                val y = 190f + i * 78f
                b.shape(ShapeType.CIRCLE, 64f, y, 50f, 50f, p.accent)
                b.text((i + 1).toString(), 64f, y, 22f, p.bg, true)
                b.text(items[i].first, 220f, y - 12f, 18f, p.ink, true)
                b.text(items[i].second, 220f, y + 12f, 13f, p.ink, false, 0.8f)
                if (i < 2) b.rect(63.5f, y + 28f, 64.5f, y + 50f, p.soft)
            } else {
                val x = 60f + i * 120f
                b.shape(ShapeType.CIRCLE, x, 220f, 92f, 92f, p.soft, 0.55f)
                b.shape(ShapeType.CIRCLE, x, 220f, 74f, 74f, p.accent)
                b.text(items[i].first, x, 220f, 20f, p.bg, true)
                b.text(items[i].second, x, 284f, 12f, p.ink, true)
            }
        }
        b.rect(40f, H - 46f, W - 40f, H - 45f, p.accent, 0.6f)
        b.text("اسم علامتك  •  رقم التواصل", W / 2, H - 26f, 12f, p.ink, false, 0.8f)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("info", i, j, "إنفوجرافيك", "4:5", "إنفوجرافيك ${styleNames[i / 2]} ${if (i % 2 == 0) "أرقام" else "خطوات"} ${p.nameAr}", "Infographic ${i + 1}-${j + 1}", p, infographic(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: InfoData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("info", layout, pal, "إنفوجرافيك", "4:5", "إنفوجرافيكي", "My infographic", p, infographic(layout, p, d))
    }
}
