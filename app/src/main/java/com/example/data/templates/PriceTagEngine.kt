package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Shop price and sale tags: 10 styles x 10 palettes = 100. Canvas 360 x 360 (1:1). */
object PriceTagEngine {
    private const val W = 360f
    val styleNames = listOf("شريط علوي", "دائرة", "ماسة", "نجمة", "إطار", "سداسي", "أمواج", "أعمدة", "هلال", "ورقة")

    data class TagData(val label: String = "", val product: String = "", val price: String = "", val oldPrice: String = "", val note: String = "")

    fun tag(style: Int, p: Palette, d: TagData = TagData()): List<CanvasElement> {
        val b = B(W, W)
        b.rect(0f, 0f, W, W, p.bg)
        when (style) {
            0 -> b.rect(0f, 0f, W, 70f, p.accent)
            1 -> { b.shape(ShapeType.CIRCLE, W - 40f, 40f, 150f, 150f, p.accent); b.shape(ShapeType.CIRCLE, W - 40f, 40f, 110f, 110f, p.soft, 0.6f) }
            2 -> { b.shape(ShapeType.DIAMOND, W - 40f, 36f, 140f, 140f, p.accent); b.shape(ShapeType.DIAMOND, 40f, W - 36f, 110f, 110f, p.soft, 0.7f) }
            3 -> { b.shape(ShapeType.STAR, W - 50f, 50f, 130f, 130f, p.accent); b.shape(ShapeType.STAR, 40f, W - 40f, 70f, 70f, p.soft, 0.8f) }
            4 -> { b.rect(10f, 10f, W - 10f, 13f, p.accent); b.rect(10f, W - 13f, W - 10f, W - 10f, p.accent); b.rect(10f, 10f, 13f, W - 10f, p.accent); b.rect(W - 13f, 10f, W - 10f, W - 10f, p.accent) }
            5 -> { b.shape(ShapeType.HEXAGON, W - 46f, 46f, 130f, 114f, p.accent); b.shape(ShapeType.HEXAGON, W - 46f, 46f, 100f, 88f, p.soft, 0.5f) }
            6 -> { for (i in 0..8) b.shape(ShapeType.CIRCLE, 20f + i * 40f, W, 64f, 64f, p.accent, 0.9f); b.rect(0f, 0f, W, 12f, p.accent) }
            7 -> { for (i in 0..6) b.rect(i * 52f, 0f, i * 52f + 26f, 40f, if (i % 2 == 0) p.accent else p.soft, 0.9f) }
            8 -> { b.shape(ShapeType.CIRCLE, 50f, 50f, 130f, 130f, p.accent); b.shape(ShapeType.CIRCLE, 70f, 42f, 110f, 110f, p.bg) }
            else -> { b.rect(0f, 0f, W, 44f, p.accent); b.rect(0f, W - 22f, W, W, p.accent); b.shape(ShapeType.DIAMOND, W / 2, 44f, 22f, 22f, p.accent) }
        }
        b.text(d.label.ifBlank { "عرض خاص" }, W / 2, 100f, 17f, p.accent, true)
        b.text(d.product.ifBlank { "اسم المنتج" }, W / 2, 140f, 30f, p.ink, true)
        b.rect(W / 2 - 40f, 168f, W / 2 + 40f, 169.5f, p.accent)
        b.text(d.price.ifBlank { "199 ج.م" }, W / 2, 224f, 56f, p.accent, true)
        b.text(d.oldPrice.ifBlank { "299 ج.م" }, W / 2, 276f, 18f, p.ink, false, 0.6f)
        b.rect(W / 2 - 34f, 276f, W / 2 + 34f, 277.2f, p.ink, 0.7f)
        b.text(d.note.ifBlank { "العرض ساري لفترة محدودة" }, W / 2, 316f, 12f, p.ink, false, 0.8f)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 10).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("price", i, j, "بطاقات أسعار وعروض", "1:1", "بطاقة سعر ${styleNames[i]} ${p.nameAr}", "Price tag ${i + 1}-${j + 1}", p, tag(i, p))
        }
    }

    fun template(style: Int, pal: Int, d: TagData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("price", style, pal, "بطاقات أسعار وعروض", "1:1", "بطاقة سعري", "My price tag", p, tag(style, p, d))
    }
}
