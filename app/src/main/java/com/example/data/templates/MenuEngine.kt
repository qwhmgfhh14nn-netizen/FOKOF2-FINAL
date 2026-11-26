package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Menus and price lists: 10 decoration styles x 2 layouts (one column, two columns) x 10 palettes = 200. Canvas 360 x 450 (4:5). */
object MenuEngine {
    private const val W = 360f
    private const val H = 450f

    val styleNames = listOf("شريط علوي", "إطار", "أعمدة جانبية", "دائرة علوية", "شريط سفلي", "ماسة", "سداسي", "خطين", "أركان مائلة", "نجمة")

    data class MenuData(
        val title: String = "", val shop: String = "", val phone: String = "",
        val sections: List<Pair<String, List<Pair<String, String>>>> = emptyList()
    )

    private val sample = listOf(
        "المقبلات" to listOf("سلطة خضراء" to "35", "حمص بالطحينة" to "30", "ورق عنب" to "45"),
        "الأطباق الرئيسية" to listOf("كباب مشوي" to "180", "فراخ مشوية" to "120", "كفتة مشوية" to "140"),
        "المشروبات" to listOf("عصير ليمون" to "25", "شاي بالنعناع" to "15", "قهوة تركي" to "30"),
        "الحلويات" to listOf("أم علي" to "40", "كنافة" to "45", "بسبوسة" to "30")
    )

    fun menu(layout: Int, p: Palette, d: MenuData = MenuData()): List<CanvasElement> {
        val style = layout / 2
        val two = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        when (style) {
            0 -> { b.rect(0f, 0f, W, 18f, p.accent); b.rect(0f, 18f, W, 21f, p.soft) }
            1 -> { b.rect(10f, 10f, W - 10f, 12f, p.accent); b.rect(10f, H - 12f, W - 10f, H - 10f, p.accent); b.rect(10f, 10f, 12f, H - 10f, p.accent); b.rect(W - 12f, 10f, W - 10f, H - 10f, p.accent) }
            2 -> { b.rect(0f, 0f, 18f, H, p.accent); b.rect(W - 18f, 0f, W, H, p.accent); b.rect(18f, 0f, 21f, H, p.soft); b.rect(W - 21f, 0f, W - 18f, H, p.soft) }
            3 -> { b.shape(ShapeType.CIRCLE, W / 2, 0f, 230f, 230f, p.accent, 0.95f); b.shape(ShapeType.CIRCLE, W / 2, 0f, 180f, 180f, p.soft, 0.7f) }
            4 -> { b.rect(0f, H - 24f, W, H, p.accent); b.rect(0f, H - 28f, W, H - 24f, p.soft) }
            5 -> { b.rect(24f, 40f, W - 24f, 41f, p.accent); b.shape(ShapeType.DIAMOND, W / 2, 40.5f, 14f, 14f, p.accent); b.rect(24f, H - 41f, W - 24f, H - 40f, p.accent); b.shape(ShapeType.DIAMOND, W / 2, H - 40.5f, 14f, 14f, p.accent) }
            6 -> { b.shape(ShapeType.HEXAGON, W / 2, 46f, 120f, 104f, p.accent); b.shape(ShapeType.HEXAGON, W / 2, 46f, 98f, 85f, p.soft, 0.7f) }
            7 -> { b.rect(24f, 24f, W - 24f, 26f, p.accent); b.rect(24f, 30f, W - 24f, 31f, p.accent, 0.6f); b.rect(24f, H - 31f, W - 24f, H - 30f, p.accent, 0.6f); b.rect(24f, H - 26f, W - 24f, H - 24f, p.accent) }
            8 -> { b.shape(ShapeType.RECTANGLE, 0f, 0f, 120f, 120f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, W, H, 120f, 120f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, 0f, 0f, 84f, 84f, p.soft, 0.9f, 45f); b.shape(ShapeType.RECTANGLE, W, H, 84f, 84f, p.soft, 0.9f, 45f) }
            else -> { b.shape(ShapeType.STAR, W / 2, 34f, 34f, 34f, p.accent); b.rect(0f, H - 10f, W, H, p.accent) }
        }
        val headOnAccent = style == 3 || style == 6
        val title = d.title.ifBlank { "قائمة الطعام" }
        val shop = d.shop.ifBlank { "اسم المطعم" }
        val ty = if (headOnAccent) 40f else 62f
        b.text(title, W / 2, ty, 27f, if (headOnAccent) p.onAccent else p.accent, true)
        b.text(shop, W / 2, ty + 26f, 12f, if (headOnAccent) p.onAccent else p.ink, false, 0.85f)
        val secs = if (d.sections.isEmpty()) sample else d.sections
        fun row(a: Float, wBox: Float, y: Float, name: String, price: String) {
            val nameW = wBox * 0.68f
            b.textBox(name, a + wBox - nameW / 2, y, nameW, 10.5f, p.ink, 1, false, 0.95f)
            b.textBox(price, a + (wBox - nameW) / 2 - 2f, y, wBox - nameW, 10.5f, p.accent, 2, true)
        }
        if (!two) {
            var y = 126f
            secs.take(3).forEach { (sec, items) ->
                b.text(sec, W / 2, y, 14f, p.accent, true)
                b.rect(W / 2 - 22f, y + 11f, W / 2 + 22f, y + 12.5f, p.accent, 0.8f)
                items.take(3).forEachIndexed { i, (n, pr) ->
                    val yy = y + 32f + i * 22f
                    row(50f, 260f, yy, n, pr)
                    b.rect(50f, yy + 10f, W - 50f, yy + 10.8f, p.soft, 0.8f)
                }
                y += 104f
            }
        } else {
            val cols = listOf(196f to listOf(0, 2), 20f to listOf(1, 3))
            cols.forEach { (a, idx) ->
                var y = 134f
                idx.forEach { k ->
                    val (sec, items) = secs[k % secs.size]
                    b.text(sec, a + 72f, y, 13f, p.accent, true)
                    b.rect(a + 50f, y + 11f, a + 94f, y + 12.5f, p.accent, 0.8f)
                    items.take(3).forEachIndexed { i, (n, pr) ->
                        val yy = y + 30f + i * 22f
                        row(a, 144f, yy, n, pr)
                        b.rect(a, yy + 10f, a + 144f, yy + 10.8f, p.soft, 0.8f)
                    }
                    y += 120f
                }
            }
        }
        b.text("للطلب والتوصيل: " + d.phone.ifBlank { "0100 000 0000" }, W / 2, H - 52f, 11f, p.ink, true, 0.9f)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            val v = if (i % 2 == 1) "عمودين" else "عمود"
            DesignEngine.template("menu", i, j, "منيو وقوائم الأسعار", "4:5", "منيو ${styleNames[i / 2]} $v ${p.nameAr}", "Menu ${i + 1}-${j + 1}", p, menu(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: MenuData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("menu", layout, pal, "منيو وقوائم الأسعار", "4:5", "منيو", "My menu", p, menu(layout, p, d))
    }
}
