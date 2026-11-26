package com.example.data.templates

import com.example.data.model.DesignTemplate
import com.example.data.model.CanvasElement
import com.example.data.model.ShapeType
import com.example.data.templates.DesignEngine.B

/** Certificates: 10 border styles x 2 header styles = 20 layouts, each in the 10 shared palettes. Canvas 360 x 270 (4:3). */
object CertEngine {
    private const val W = 360f
    private const val H = 270f

    val styleNames = listOf("إطار مزدوج", "عمودين", "أركان مربعة", "شريطين", "أختام الأركان",
        "ماسات الأركان", "خطوط جانبية", "ختم سداسي", "أركان مائلة", "إطار نجمة")

    data class CertData(
        val title: String = "شهادة تقدير", val name: String = "", val reason: String = "",
        val date: String = "", val signer: String = ""
    )

    fun cert(layout: Int, p: Palette, d: CertData = CertData()): List<CanvasElement> {
        val style = layout / 2
        val ribbon = layout % 2 == 1
        val b = B(W, H)
        b.rect(0f, 0f, W, H, p.bg)
        b.shape(ShapeType.CIRCLE, W / 2, H / 2 + 10f, 250f, 250f, p.bg2, 0.55f)
        when (style) {
            0 -> { b.rect(10f, 10f, W - 10f, 12f, p.accent); b.rect(10f, H - 12f, W - 10f, H - 10f, p.accent)
                b.rect(10f, 10f, 12f, H - 10f, p.accent); b.rect(W - 12f, 10f, W - 10f, H - 10f, p.accent)
                b.rect(18f, 18f, W - 18f, 19f, p.accent, 0.6f); b.rect(18f, H - 19f, W - 18f, H - 18f, p.accent, 0.6f)
                b.rect(18f, 18f, 19f, H - 18f, p.accent, 0.6f); b.rect(W - 19f, 18f, W - 18f, H - 18f, p.accent, 0.6f) }
            1 -> { b.rect(0f, 0f, 24f, H, p.accent); b.rect(W - 24f, 0f, W, H, p.accent)
                b.rect(24f, 0f, 27f, H, p.soft); b.rect(W - 27f, 0f, W - 24f, H, p.soft) }
            2 -> { listOf(16f to 16f, W - 16f to 16f, 16f to H - 16f, W - 16f to H - 16f).forEach { (x, y) ->
                    b.shape(ShapeType.RECTANGLE, x, y, 22f, 22f, p.accent) }
                b.rect(30f, 15f, W - 30f, 16f, p.accent, 0.7f); b.rect(30f, H - 16f, W - 30f, H - 15f, p.accent, 0.7f)
                b.rect(15f, 30f, 16f, H - 30f, p.accent, 0.7f); b.rect(W - 16f, 30f, W - 15f, H - 30f, p.accent, 0.7f) }
            3 -> { b.rect(0f, 0f, W, 22f, p.accent); b.rect(0f, 22f, W, 25f, p.soft)
                b.rect(0f, H - 22f, W, H, p.accent); b.rect(0f, H - 25f, W, H - 22f, p.soft) }
            4 -> { listOf(8f to 8f, W - 8f to 8f, 8f to H - 8f, W - 8f to H - 8f).forEach { (x, y) ->
                    b.shape(ShapeType.CIRCLE, x, y, 70f, 70f, p.accent, 0.9f); b.shape(ShapeType.CIRCLE, x, y, 48f, 48f, p.soft, 0.7f) }
                b.rect(40f, 14f, W - 40f, 15f, p.accent, 0.6f); b.rect(40f, H - 15f, W - 40f, H - 14f, p.accent, 0.6f) }
            5 -> { b.rect(14f, 14f, W - 14f, 15.5f, p.accent); b.rect(14f, H - 15.5f, W - 14f, H - 14f, p.accent)
                b.rect(14f, 14f, 15.5f, H - 14f, p.accent); b.rect(W - 15.5f, 14f, W - 14f, H - 14f, p.accent)
                listOf(14f to 14f, W - 14f to 14f, 14f to H - 14f, W - 14f to H - 14f).forEach { (x, y) ->
                    b.shape(ShapeType.DIAMOND, x, y, 20f, 20f, p.accent) } }
            6 -> { b.rect(30f, 0f, 33f, H, p.accent); b.rect(W - 33f, 0f, W - 30f, H, p.accent)
                b.rect(38f, 0f, 39f, H, p.soft); b.rect(W - 39f, 0f, W - 38f, H, p.soft) }
            7 -> { b.rect(12f, 12f, W - 12f, 13f, p.accent, 0.8f); b.rect(12f, H - 13f, W - 12f, H - 12f, p.accent, 0.8f)
                b.rect(12f, 12f, 13f, H - 12f, p.accent, 0.8f); b.rect(W - 13f, 12f, W - 12f, H - 12f, p.accent, 0.8f)
}
            8 -> { listOf(0f to 0f, W to 0f, 0f to H, W to H).forEach { (x, y) ->
                    b.shape(ShapeType.RECTANGLE, x, y, 90f, 90f, p.accent, 1f, 45f); b.shape(ShapeType.RECTANGLE, x, y, 62f, 62f, p.soft, 0.9f, 45f) } }
            else -> { b.rect(12f, 12f, W - 12f, 14f, p.accent); b.rect(12f, H - 14f, W - 12f, H - 12f, p.accent)
                b.rect(12f, 12f, 14f, H - 12f, p.accent); b.rect(W - 14f, 12f, W - 12f, H - 12f, p.accent)
                b.shape(ShapeType.STAR, W / 2, 14f, 26f, 26f, p.accent) }
        }
        // header
        if (ribbon) {
            b.rect(60f, 40f, W - 60f, 78f, p.accent)
            b.rect(50f, 78f, 60f, 88f, p.ink, 0.45f); b.rect(W - 60f, 78f, W - 50f, 88f, p.ink, 0.45f)
            b.text(d.title, W / 2, 59f, 24f, p.onAccent, true)
        } else {
            b.text(d.title, W / 2, 56f, 27f, p.accent, true)
            b.rect(W / 2 - 50f, 76f, W / 2 + 50f, 77.5f, p.accent)
            b.shape(ShapeType.DIAMOND, W / 2, 76.7f, 9f, 9f, p.accent)
        }
        b.text("تمنح هذه الشهادة إلى", W / 2, 106f, 12f, p.ink, false, 0.85f)
        b.text(d.name.ifBlank { "اسم المُكرَّم" }, W / 2, 138f, 25f, p.ink, true)
        b.rect(W / 2 - 80f, 156f, W / 2 + 80f, 157f, p.accent, 0.8f)
        b.text(d.reason.ifBlank { "تقديرا لجهوده المتميزة وتفوقه في العمل" }, W / 2, 178f, 11.5f, p.ink, false, 0.9f)
        val y = 226f
        b.text(d.date.ifBlank { "التاريخ: 2026/01/01" }, 100f, y, 10.5f, p.ink, false, 0.9f)
        b.rect(60f, y - 14f, 140f, y - 13f, p.ink, 0.5f)
        b.text(d.signer.ifBlank { "التوقيع" }, 260f, y, 10.5f, p.ink, false, 0.9f)
        b.rect(220f, y - 14f, 300f, y - 13f, p.ink, 0.5f)
        if (style == 7) { b.shape(ShapeType.HEXAGON, W / 2, y - 4f, 38f, 33f, p.accent); b.shape(ShapeType.HEXAGON, W / 2, y - 4f, 28f, 24f, p.bg) }
        else { b.shape(ShapeType.STAR, W / 2, y - 4f, 30f, 30f, p.accent); b.shape(ShapeType.CIRCLE, W / 2, y - 4f, 11f, 11f, p.bg) }
        return b.els
    }

    fun templates(): List<DesignTemplate> = (0 until 20).flatMap { i ->
        DesignEngine.palettes.mapIndexed { j, p ->
            val v = if (i % 2 == 1) "بشريط" else "بخط"
            DesignEngine.template("cert", i, j, "شهادات تقدير", "4:3", "شهادة ${styleNames[i / 2]} $v ${p.nameAr}", "Certificate ${i + 1}-${j + 1}", p, cert(i, p))
        }
    }

    fun template(layout: Int, pal: Int, d: CertData): DesignTemplate {
        val p = DesignEngine.palettes[pal]
        return DesignEngine.template("cert", layout, pal, "شهادات تقدير", "4:3", "شهادتي", "My certificate", p, cert(layout, p, d))
    }
}
