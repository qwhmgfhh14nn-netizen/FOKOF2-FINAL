package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Business cards: 10 decoration styles x 2 text layouts (centered, Arabic right-aligned with logo circle) x 10 palettes = 200. Canvas 360 x 202 (16:9). */
object CardEngine {
    private const val W = 360f
    private const val H = 202f

    val styleNames = listOf("شريط جانبي", "شريط علوي", "ركن مائل", "شريط سفلي", "دوائر", "إطار", "سداسي", "خطوط", "ماسة", "نجمة")

    data class CardData(
        val name: String = "", val job: String = "", val company: String = "",
        val phone: String = "", val email: String = "", val address: String = ""
    )

    fun card(layout: Int, p: Palette, d: CardData = CardData()): List<CanvasElement> {
        val style = layout / 2
        val rtl = layout % 2 == 1
        val b = B(W, H)
        val name = d.name.ifBlank { "اسم صاحب الكارت" }
        val job = d.job.ifBlank { "المسمى الوظيفي" }
        val company = d.company.ifBlank { "اسم الشركة" }
        val phone = d.phone.ifBlank { "0100 000 0000" }
        val email = d.email.ifBlank { "name@email.com" }
        val address = d.address.ifBlank { "القاهرة، مصر" }
        b.rect(0f, 0f, W, H, p.bg)
        when (style) {
            0 -> { b.rect(0f, 0f, 14f, H, p.accent); b.rect(14f, 0f, 17f, H, p.soft) }
            1 -> { b.rect(0f, 0f, W, 16f, p.accent); b.rect(0f, 16f, W, 18f, p.soft) }
            2 -> { b.shape(ShapeType.RECTANGLE, W, 0f, 120f, 120f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, W, 0f, 84f, 84f, p.soft, 0.9f, 45f) }
            3 -> { b.rect(0f, H - 18f, W, H, p.accent); b.rect(0f, H - 21f, W, H - 18f, p.soft) }
            4 -> { b.shape(ShapeType.CIRCLE, W - 20f, 20f, 130f, 130f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, W - 20f, 20f, 86f, 86f, p.soft, 0.8f); b.shape(ShapeType.CIRCLE, 20f, H - 10f, 80f, 80f, p.soft, 0.6f) }
            5 -> { b.rect(10f, 10f, W - 10f, 11.5f, p.accent); b.rect(10f, H - 11.5f, W - 10f, H - 10f, p.accent); b.rect(10f, 10f, 11.5f, H - 10f, p.accent); b.rect(W - 11.5f, 10f, W - 10f, H - 10f, p.accent) }
            6 -> { b.shape(ShapeType.HEXAGON, W - 30f, 30f, 90f, 80f, p.accent, 0.95f); b.shape(ShapeType.HEXAGON, W - 30f, 30f, 62f, 55f, p.soft, 0.9f); b.rect(0f, H - 6f, W, H, p.accent) }
            7 -> { listOf(20f, 26f, 32f).forEachIndexed { i, y -> b.rect(0f, y, W, y + 1.5f, p.accent, 0.8f - i * 0.2f) }; b.rect(0f, H - 8f, W, H, p.accent) }
            8 -> { b.shape(ShapeType.DIAMOND, W - 28f, H - 28f, 96f, 96f, p.accent, 0.95f); b.shape(ShapeType.DIAMOND, W - 28f, H - 28f, 64f, 64f, p.soft, 0.9f) }
            else -> { b.shape(ShapeType.STAR, 36f, 36f, 56f, 56f, p.accent); b.rect(0f, H - 8f, W, H, p.accent) }
        }
        if (!rtl) {
            val cx = if (style == 0) 190f else W / 2
            b.text(name, cx, 78f, 20f, p.ink, true)
            b.text(job, cx, 100f, 11.5f, p.accent, true)
            b.text(company, cx, 116f, 10.5f, p.ink, false, 0.8f)
            b.rect(cx - 30f, 128f, cx + 30f, 129f, p.accent, 0.8f)
            b.text(phone, cx, 144f, 10.5f, p.ink, false, 0.9f)
            b.text(email, cx, 158f, 10.5f, p.ink, false, 0.9f)
            b.text(address, cx, 172f, 10.5f, p.ink, false, 0.9f)
        } else {
            val ini = name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString(".") { it.take(1) }
            val ly = if (style == 1 || style == 7) 112f else 100f
            b.shape(ShapeType.CIRCLE, 70f, ly, 74f, 74f, p.accent)
            b.text(ini, 70f, ly, 22f, p.onAccent, true)
            val cx = 236f
            b.textBox(name, cx, 66f, 190f, 18f, p.ink, 1, true)
            b.textBox(job, cx, 88f, 190f, 11.5f, p.accent, 1, true)
            b.textBox(company, cx, 103f, 190f, 10.5f, p.ink, 1, false, 0.8f)
            b.rect(cx + 95f - 50f, 116f, cx + 95f, 117f, p.accent, 0.8f)
            b.textBox(phone, cx, 133f, 190f, 10.5f, p.ink, 1, false, 0.9f)
            b.textBox(email, cx, 147f, 190f, 10.5f, p.ink, 1, false, 0.9f)
            b.textBox(address, cx, 161f, 190f, 10.5f, p.ink, 1, false, 0.9f)
        }
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            val v = if (i % 2 == 1) "يمين" else "وسط"
            DesignEngine.template("card", i, j, "كروت العمل الشخصية", "16:9", "كارت ${styleNames[i / 2]} $v ${p.nameAr}", "Business card ${i + 1}-${j + 1}", p, card(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: CardData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("card", layout, pal, "كروت العمل الشخصية", "16:9", "كارتي", "My card", p, card(layout, p, d))
    }
}
