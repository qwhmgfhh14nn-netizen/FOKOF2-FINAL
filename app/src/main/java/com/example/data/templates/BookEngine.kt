package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Book covers: 10 art styles x 2 title layouts x 10 palettes = 200. Canvas 360 x 480 (3:4). */
object BookEngine {
    private const val W = 360f
    private const val H = 480f
    val styleNames = listOf("هلال", "أشرطة", "إطار", "دوائر", "ماسات", "شروق", "أمواج", "نجمة", "أعمدة", "ميدالية")

    data class BookData(val title: String = "", val subtitle: String = "", val author: String = "")

    fun cover(layout: Int, p: Palette, d: BookData = BookData()): List<CanvasElement> {
        val style = layout / 2
        val low = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        when (style) {
            0 -> { b.shape(ShapeType.CIRCLE, W / 2, 110f, 200f, 200f, p.accent, 0.95f); b.shape(ShapeType.CIRCLE, W / 2 + 34f, 94f, 170f, 170f, p.bg, 1f) }
            1 -> { for (i in 0..5) b.rect(0f, 20f + i * 18f, W, 30f + i * 18f, p.accent, 0.95f - i * 0.12f) }
            2 -> { b.rect(24f, 24f, W - 24f, H - 24f, p.soft, 0.5f); b.rect(24f, 24f, W - 24f, 27f, p.accent); b.rect(24f, H - 27f, W - 24f, H - 24f, p.accent); b.rect(24f, 24f, 27f, H - 24f, p.accent); b.rect(W - 27f, 24f, W - 24f, H - 24f, p.accent) }
            3 -> { b.shape(ShapeType.CIRCLE, 40f, 60f, 190f, 190f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, W - 30f, 150f, 130f, 130f, p.soft, 0.9f); b.shape(ShapeType.CIRCLE, 90f, H - 40f, 160f, 160f, p.soft, 0.8f); b.shape(ShapeType.CIRCLE, W - 40f, H - 60f, 110f, 110f, p.accent, 0.9f) }
            4 -> { for (i in 0..3) for (j in 0..1) b.shape(ShapeType.DIAMOND, 50f + i * 85f, 50f + j * 45f, 64f, 64f, if ((i + j) % 2 == 0) p.accent else p.soft, 0.9f) }
            5 -> { b.shape(ShapeType.CIRCLE, W / 2, 190f, 320f, 320f, p.soft, 0.6f); b.shape(ShapeType.CIRCLE, W / 2, 190f, 220f, 220f, p.accent, 0.9f); b.rect(0f, 190f, W, H, p.bg) }
            6 -> { for (i in 0..8) b.shape(ShapeType.CIRCLE, 20f + i * 40f, H - 10f, 70f, 70f, p.accent, 0.9f); for (i in 0..7) b.shape(ShapeType.CIRCLE, 40f + i * 40f, H - 36f, 60f, 60f, p.soft, 0.8f) }
            7 -> { b.shape(ShapeType.STAR, W / 2, 125f, 190f, 190f, p.accent, 0.95f); b.shape(ShapeType.STAR, W / 2, 125f, 120f, 120f, p.soft, 0.9f) }
            8 -> { for (i in 0..6) b.rect(i * 52f, 0f, i * 52f + 26f, 190f, if (i % 2 == 0) p.accent else p.soft, 0.9f) }
            else -> { b.shape(ShapeType.HEXAGON, W / 2, 130f, 200f, 176f, p.accent); b.shape(ShapeType.HEXAGON, W / 2, 130f, 164f, 144f, p.bg); b.shape(ShapeType.STAR, W / 2, 130f, 70f, 70f, p.accent) }
        }
        val ty = if (low) 300f else 262f
        b.text(d.title.ifBlank { "عنوان الكتاب" }, W / 2, ty, 38f, p.accent, true)
        b.rect(W / 2 - 50f, ty + 32f, W / 2 + 50f, ty + 33.5f, p.accent)
        b.text(d.subtitle.ifBlank { "عنوان فرعي يشرح فكرة الكتاب" }, W / 2, ty + 58f, 15f, p.ink, false, 0.9f)
        b.text(d.author.ifBlank { "اسم المؤلف" }, W / 2, H - 52f, 18f, p.ink, true)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("book", i, j, "أغلفة كتب", "3:4", "غلاف ${styleNames[i / 2]} ${p.nameAr}", "Book cover ${i + 1}-${j + 1}", p, cover(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: BookData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("book", layout, pal, "أغلفة كتب", "3:4", "غلافي", "My cover", p, cover(layout, p, d))
    }
}
