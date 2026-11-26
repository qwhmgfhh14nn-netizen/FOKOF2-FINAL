package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Presentation slides: 10 styles x 2 (title slide / content slide) x 10 palettes = 200. Canvas 360 x 203 (16:9). */
object PresentEngine {
    private const val W = 360f
    private const val H = 203f
    val styleNames = listOf("شريط جانبي", "دوائر", "ماسات", "هلال", "أمواج", "نجوم", "أعمدة", "سداسي", "إطار", "أقواس")

    data class SlideData(val title: String = "", val subtitle: String = "", val points: List<String> = emptyList())

    fun slide(layout: Int, p: Palette, d: SlideData = SlideData()): List<CanvasElement> {
        val style = layout / 2
        val content = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, if (content) p.bg else p.bg2)
        when (style) {
            0 -> { b.rect(0f, 0f, 22f, H, p.accent); b.rect(22f, 0f, 25f, H, p.soft) }
            1 -> { b.shape(ShapeType.CIRCLE, W - 20f, 20f, 130f, 130f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, W - 20f, 20f, 90f, 90f, p.soft, 0.7f) }
            2 -> { for (i in 0..8) b.shape(ShapeType.DIAMOND, 20f + i * 40f, H - 6f, 26f, 26f, p.accent) }
            3 -> { b.shape(ShapeType.CIRCLE, 30f, 30f, 110f, 110f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, 48f, 22f, 96f, 96f, if (content) p.bg else p.bg2, 1f) }
            4 -> { for (i in 0..8) b.shape(ShapeType.CIRCLE, 20f + i * 40f, H, 56f, 56f, p.accent, 0.9f) }
            5 -> { for (i in 0..4) b.shape(ShapeType.STAR, 40f + i * 70f, 22f, 16f, 16f, p.accent, 0.6f); b.rect(0f, H - 8f, W, H, p.accent) }
            6 -> { for (i in 0..6) b.rect(i * 52f, H - 40f, i * 52f + 26f, H, p.accent, if (i % 2 == 0) 0.95f else 0.4f) }
            7 -> { b.shape(ShapeType.HEXAGON, W - 26f, H - 26f, 84f, 74f, p.accent, 0.9f); b.shape(ShapeType.HEXAGON, W - 26f, H - 26f, 56f, 50f, p.soft, 0.7f) }
            8 -> { b.rect(8f, 8f, W - 8f, 9.5f, p.accent); b.rect(8f, H - 9.5f, W - 8f, H - 8f, p.accent); b.rect(8f, 8f, 9.5f, H - 8f, p.accent); b.rect(W - 9.5f, 8f, W - 8f, H - 8f, p.accent) }
            else -> { b.shape(ShapeType.CIRCLE, W / 2, H + 40f, 340f, 160f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, W / 2, H + 50f, 280f, 130f, p.soft, 0.7f) }
        }
        if (!content) {
            b.text(d.title.ifBlank { "عنوان العرض التقديمي" }, W / 2, 80f, 28f, p.accent, true)
            b.rect(W / 2 - 50f, 106f, W / 2 + 50f, 107.5f, p.accent)
            b.shape(ShapeType.DIAMOND, W / 2, 106.7f, 8f, 8f, p.accent)
            b.text(d.subtitle.ifBlank { "اسم المقدم  •  التاريخ" }, W / 2, 130f, 14f, p.ink, false, 0.9f)
        } else {
            b.text(d.title.ifBlank { "عنوان الشريحة" }, W / 2, 34f, 22f, p.accent, true)
            b.rect(W / 2 - 40f, 52f, W / 2 + 40f, 53.5f, p.accent)
            val pts = if (d.points.size >= 3) d.points else listOf("النقطة الأولى المهمة", "النقطة الثانية", "النقطة الثالثة")
            for (i in 0..2) {
                b.shape(ShapeType.CIRCLE, W / 2 + 90f, 82f + i * 28f, 8f, 8f, p.accent)
                b.text(pts[i], W / 2 + 10f, 82f + i * 28f, 14f, p.ink, false)
            }
        }
        b.rect(60f, H - 22f, W - 60f, H - 21f, p.accent, 0.35f)
        b.shape(ShapeType.DIAMOND, W / 2, H - 21.5f, 6f, 6f, p.accent, 0.6f)
        b.text(if (content) "٢" else "١", W / 2, H - 11f, 10f, p.ink, false, 0.6f)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("slide", i, j, "عروض تقديمية", "16:9", "شريحة ${styleNames[i / 2]} ${if (i % 2 == 0) "عنوان" else "محتوى"} ${p.nameAr}", "Slide ${i + 1}-${j + 1}", p, slide(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: SlideData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("slide", layout, pal, "عروض تقديمية", "16:9", "شريحتي", "My slide", p, slide(layout, p, d))
    }
}
