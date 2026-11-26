package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Invitations and greeting cards: 10 decoration styles x 2 kinds (wedding invitation, greeting) x 10 palettes = 200. Canvas 360 x 450 (4:5). */
object InviteEngine {
    private const val W = 360f
    private const val H = 450f

    val styleNames = listOf("إطار مزدوج", "أقواس", "دوائر الأركان", "أعمدة", "هالة", "ماسات", "موجة", "ميدالية", "أركان مائلة", "نجوم")

    data class InviteData(
        val line1: String = "", val title: String = "", val names: String = "",
        val whenText: String = "", val where: String = "", val footer: String = ""
    )

    fun invite(layout: Int, p: Palette, d: InviteData = InviteData()): List<CanvasElement> {
        val style = layout / 2
        val greeting = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        when (style) {
            0 -> { b.rect(12f, 12f, W - 12f, 14f, p.accent); b.rect(12f, H - 14f, W - 12f, H - 12f, p.accent); b.rect(12f, 12f, 14f, H - 12f, p.accent); b.rect(W - 14f, 12f, W - 12f, H - 12f, p.accent)
                b.rect(22f, 22f, W - 22f, 23f, p.accent, 0.6f); b.rect(22f, H - 23f, W - 22f, H - 22f, p.accent, 0.6f); b.rect(22f, 22f, 23f, H - 22f, p.accent, 0.6f); b.rect(W - 23f, 22f, W - 22f, H - 22f, p.accent, 0.6f) }
            1 -> { b.shape(ShapeType.CIRCLE, W / 2, -50f, 360f, 200f, p.accent, 0.95f); b.shape(ShapeType.CIRCLE, W / 2, H + 50f, 360f, 200f, p.accent, 0.95f); b.shape(ShapeType.CIRCLE, W / 2, -60f, 320f, 180f, p.soft, 0.7f); b.shape(ShapeType.CIRCLE, W / 2, H + 60f, 320f, 180f, p.soft, 0.7f) }
            2 -> { listOf(0f to 0f, W to 0f, 0f to H, W to H).forEach { (x, y) -> b.shape(ShapeType.CIRCLE, x, y, 150f, 150f, p.accent, 0.95f); b.shape(ShapeType.CIRCLE, x, y, 104f, 104f, p.soft, 0.8f) } }
            3 -> { b.rect(0f, 0f, 20f, H, p.accent); b.rect(W - 20f, 0f, W, H, p.accent); b.rect(20f, 0f, 23f, H, p.soft); b.rect(W - 23f, 0f, W - 20f, H, p.soft) }
            4 -> { b.shape(ShapeType.CIRCLE, W / 2, H / 2, 330f, 330f, p.soft, 0.6f); b.shape(ShapeType.CIRCLE, W / 2, H / 2, 290f, 290f, p.bg2, 0.8f); b.rect(16f, 16f, W - 16f, 17.5f, p.accent); b.rect(16f, H - 17.5f, W - 16f, H - 16f, p.accent) }
            5 -> { b.rect(16f, 16f, W - 16f, 17.5f, p.accent); b.rect(16f, H - 17.5f, W - 16f, H - 16f, p.accent); b.rect(16f, 16f, 17.5f, H - 16f, p.accent); b.rect(W - 17.5f, 16f, W - 16f, H - 16f, p.accent)
                listOf(16f to 16f, W - 16f to 16f, 16f to H - 16f, W - 16f to H - 16f).forEach { (x, y) -> b.shape(ShapeType.DIAMOND, x, y, 26f, 26f, p.accent) } }
            6 -> { for (i in 0..8) { b.shape(ShapeType.CIRCLE, 20f + i * 40f, H - 6f, 56f, 56f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, 20f + i * 40f, 6f, 56f, 56f, p.accent, 0.9f) } }
            7 -> { b.shape(ShapeType.HEXAGON, W / 2, 62f, 96f, 84f, p.accent); b.shape(ShapeType.HEXAGON, W / 2, 62f, 76f, 66f, p.bg); b.shape(ShapeType.STAR, W / 2, 62f, 34f, 34f, p.accent); b.rect(14f, H - 16f, W - 14f, H - 14f, p.accent) }
            8 -> { listOf(0f to 0f, W to H).forEach { (x, y) -> b.shape(ShapeType.RECTANGLE, x, y, 150f, 150f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, x, y, 104f, 104f, p.soft, 0.9f, 45f) } }
            else -> { for (i in 0..4) b.shape(ShapeType.STAR, 80f + i * 50f, 34f, 20f, 20f, p.accent, 0.5f + 0.1f * (4 - kotlin.math.abs(i - 2) * 2)); b.rect(0f, H - 12f, W, H, p.accent) }
        }
        val l1 = d.line1.ifBlank { if (greeting) "تهنئة خاصة" else "يتشرف بدعوتكم لحضور" }
        val title = d.title.ifBlank { if (greeting) "كل عام وأنتم بخير" else "حفل زفاف" }
        val names = d.names.ifBlank { if (greeting) "نتمنى لكم عيداً سعيداً مليئاً بالفرح" else "أحمد  &  سارة" }
        val whenT = d.whenText.ifBlank { if (greeting) "" else "يوم الجمعة 1 يناير 2027  •  الساعة 8 مساءً" }
        val where = d.where.ifBlank { if (greeting) "" else "قاعة الأفراح، القاهرة" }
        val foot = d.footer.ifBlank { if (greeting) "مع تحيات: اسم المرسل" else "بحضوركم تكتمل فرحتنا" }
        b.text(l1, W / 2, 128f, 13f, p.ink, false, 0.85f)
        b.text(title, W / 2, 178f, 32f, p.accent, true)
        b.rect(W / 2 - 55f, 204f, W / 2 + 55f, 205.5f, p.accent)
        b.shape(ShapeType.DIAMOND, W / 2, 204.7f, 10f, 10f, p.accent)
        b.text(names, W / 2, 244f, if (greeting) 14f else 26f, p.ink, true)
        if (whenT.isNotBlank()) b.text(whenT, W / 2, 292f, 12f, p.ink, false, 0.9f)
        if (where.isNotBlank()) b.text(where, W / 2, 314f, 12f, p.ink, false, 0.9f)
        b.text(foot, W / 2, 372f, 12.5f, p.accent, true)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            val v = if (i % 2 == 1) "تهنئة" else "دعوة"
            DesignEngine.template("inv", i, j, "دعوات وتهاني", "4:5", "$v ${styleNames[i / 2]} ${p.nameAr}", "Invitation ${i + 1}-${j + 1}", p, invite(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: InviteData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("inv", layout, pal, "دعوات وتهاني", "4:5", "دعوتي", "My invitation", p, invite(layout, p, d))
    }
}
