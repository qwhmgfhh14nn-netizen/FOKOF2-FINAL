package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Logos: 10 emblem styles x 2 (name under mark / name with tagline) x 10 palettes = 200. Canvas 360 x 360 (1:1). */
object LogoEngine {
    private const val W = 360f
    val styleNames = listOf("دائرة", "سداسي", "ماسة", "نجمة", "إطار مزدوج", "درع", "حلقة", "مربع مائل", "هلال", "شريط")

    data class LogoData(val name: String = "", val tagline: String = "", val letter: String = "")

    fun logo(layout: Int, p: Palette, d: LogoData = LogoData()): List<CanvasElement> {
        val style = layout / 2
        val tag = layout % 2 == 1
        val b = B(W, W)
        b.rect(0f, 0f, W, W, p.bg)
        val cy = if (tag) 120f else 130f
        when (style) {
            0 -> { b.shape(ShapeType.CIRCLE, W / 2, cy, 130f, 130f, p.accent); b.shape(ShapeType.CIRCLE, W / 2, cy, 112f, 112f, p.bg, 1f); b.shape(ShapeType.CIRCLE, W / 2, cy, 96f, 96f, p.accent) }
            1 -> { b.shape(ShapeType.HEXAGON, W / 2, cy, 140f, 122f, p.accent); b.shape(ShapeType.HEXAGON, W / 2, cy, 118f, 102f, p.soft, 0.35f) }
            2 -> { b.shape(ShapeType.DIAMOND, W / 2, cy, 140f, 140f, p.accent); b.shape(ShapeType.DIAMOND, W / 2, cy, 114f, 114f, p.bg); b.shape(ShapeType.DIAMOND, W / 2, cy, 96f, 96f, p.accent) }
            3 -> { b.shape(ShapeType.STAR, W / 2, cy, 150f, 150f, p.accent); b.shape(ShapeType.CIRCLE, W / 2, cy, 80f, 80f, p.bg) }
            4 -> { b.rect(W / 2 - 62f, cy - 62f, W / 2 + 62f, cy + 62f, p.accent); b.rect(W / 2 - 56f, cy - 56f, W / 2 + 56f, cy + 56f, p.bg); b.rect(W / 2 - 50f, cy - 50f, W / 2 + 50f, cy + 50f, p.accent) }
            5 -> { b.shape(ShapeType.RECTANGLE, W / 2, cy - 10f, 112f, 100f, p.accent); b.shape(ShapeType.DIAMOND, W / 2, cy + 44f, 112f, 90f, p.accent) }
            6 -> { b.shape(ShapeType.CIRCLE, W / 2, cy, 136f, 136f, p.soft, 0.7f); b.shape(ShapeType.CIRCLE, W / 2, cy, 136f, 136f, p.accent, 0.0f); b.shape(ShapeType.CIRCLE, W / 2, cy, 104f, 104f, p.accent); b.shape(ShapeType.CIRCLE, W / 2, cy, 84f, 84f, p.bg) }
            7 -> { b.shape(ShapeType.RECTANGLE, W / 2, cy, 100f, 100f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, W / 2, cy, 70f, 70f, p.bg, 1f, 45f) }
            8 -> { b.shape(ShapeType.CIRCLE, W / 2, cy, 130f, 130f, p.accent); b.shape(ShapeType.CIRCLE, W / 2 + 26f, cy - 8f, 112f, 112f, p.bg) }
            else -> { b.rect(40f, cy - 40f, W - 40f, cy + 40f, p.accent); b.rect(40f, cy - 46f, W - 40f, cy - 44f, p.accent); b.rect(40f, cy + 44f, W - 40f, cy + 46f, p.accent) }
        }
        val onMark = when (style) { 0, 1, 2, 4, 5, 9 -> p.bg; else -> p.accent }
        b.text(d.letter.ifBlank { "ف" }, W / 2, if (style == 5) cy - 10f else cy, if (style == 9) 40f else 52f, onMark, true)
        val ny = if (tag) 214f else 232f
        b.text(d.name.ifBlank { "اسم العلامة" }, W / 2, ny, 34f, p.ink, true)
        b.rect(W / 2 - 40f, ny + 28f, W / 2 + 40f, ny + 29.5f, p.accent)
        if (tag) b.text(d.tagline.ifBlank { "شعارك هنا" }, W / 2, ny + 52f, 15f, p.accent, false)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("logo", i, j, "شعارات", "1:1", "شعار ${styleNames[i / 2]} ${p.nameAr}", "Logo ${i + 1}-${j + 1}", p, logo(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: LogoData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("logo", layout, pal, "شعارات", "1:1", "شعاري", "My logo", p, logo(layout, p, d))
    }
}
