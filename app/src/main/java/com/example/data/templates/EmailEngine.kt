package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Email / newsletter headers: 10 styles x 10 palettes = 100. Canvas 360 x 270 (4:3). */
object EmailEngine {
    private const val W = 360f
    private const val H = 270f
    val styleNames = listOf("شريط", "دوائر", "ماسات", "هلال", "أمواج", "نجوم", "أعمدة", "سداسي", "إطار", "أقواس")

    data class EmailData(val title: String = "", val subtitle: String = "", val button: String = "", val footer: String = "")

    fun email(style: Int, p: Palette, d: EmailData = EmailData()): List<CanvasElement> {
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        b.rect(0f, 0f, W, 78f, p.accent)
        when (style) {
            0 -> b.rect(0f, 78f, W, 82f, p.soft)
            1 -> for (i in 0..5) b.shape(ShapeType.CIRCLE, 30f + i * 60f, 78f, 44f, 44f, p.accent)
            2 -> for (i in 0..8) b.shape(ShapeType.DIAMOND, 20f + i * 40f, 80f, 24f, 24f, p.accent)
            3 -> { b.shape(ShapeType.CIRCLE, 40f, 20f, 100f, 100f, p.soft, 0.5f); b.shape(ShapeType.CIRCLE, 58f, 14f, 84f, 84f, p.accent) }
            4 -> for (i in 0..8) b.shape(ShapeType.CIRCLE, 20f + i * 40f, 80f, 40f, 40f, p.accent)
            5 -> for (i in 0..4) b.shape(ShapeType.STAR, 40f + i * 70f, 66f, 14f, 14f, p.soft, 0.8f)
            6 -> for (i in 0..6) b.rect(i * 52f, 0f, i * 52f + 14f, 78f, p.soft, 0.35f)
            7 -> { b.shape(ShapeType.HEXAGON, W - 36f, 24f, 70f, 62f, p.soft, 0.4f); b.shape(ShapeType.HEXAGON, 26f, 66f, 54f, 48f, p.soft, 0.4f) }
            8 -> { b.rect(8f, 8f, W - 8f, 9.5f, p.soft); b.rect(8f, 68.5f, W - 8f, 70f, p.soft) }
            else -> { b.shape(ShapeType.CIRCLE, W / 2, 0f, 260f, 120f, p.soft, 0.35f); b.shape(ShapeType.CIRCLE, W / 2, 0f, 190f, 90f, p.soft, 0.35f) }
        }
        b.text(d.title.ifBlank { "نشرتنا الأسبوعية" }, W / 2, 40f, 24f, p.bg, true)
        b.text(d.subtitle.ifBlank { "أهم الأخبار والعروض في مكان واحد" }, W / 2, 128f, 14f, p.ink, false, 0.9f)
        b.rect(W / 2 - 40f, 146f, W / 2 + 40f, 147.5f, p.accent)
        b.rect(W / 2 - 62f, 170f, W / 2 + 62f, 206f, p.accent)
        b.text(d.button.ifBlank { "اقرأ المزيد" }, W / 2, 188f, 14f, p.bg, true)
        b.rect(30f, H - 34f, W - 30f, H - 33f, p.accent, 0.5f)
        b.text(d.footer.ifBlank { "اسم علامتك  •  إلغاء الاشتراك" }, W / 2, H - 16f, 11f, p.ink, false, 0.7f)
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 10).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            DesignEngine.template("mail", i, j, "نشرات وإيميلات", "4:3", "هيدر ${styleNames[i]} ${p.nameAr}", "Email ${i + 1}-${j + 1}", p, email(i, p))
        }
    }

    fun template(style: Int, pal: Int, d: EmailData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("mail", style, pal, "نشرات وإيميلات", "4:3", "نشرتي", "My newsletter", p, email(style, p, d))
    }
}
