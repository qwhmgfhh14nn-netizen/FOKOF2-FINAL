package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType

/**
 * Runtime design engine. A template is a layout function applied to a palette, producing the same
 * CanvasElement list the Studio already edits and exports. No artwork files: ids are stable (cv_LL_PP)
 * so the catalog stays append-only. Canvas is 360 wide; elements are positioned by centre.
 */
data class Palette(val nameAr: String, val bg: String, val bg2: String, val ink: String, val accent: String, val soft: String, val onAccent: String)

object DesignEngine {
    const val W = 360f

    val palettes = listOf(
        Palette("عاجي وذهبي", "#FBF6EA", "#F3EAD2", "#2B2418", "#B8860B", "#E9DCB8", "#FFFFFF"),
        Palette("كحلي وذهبي", "#F7F5F0", "#ECE8DD", "#14213D", "#C9A227", "#DCD5BF", "#14213D"),
        Palette("أسود وذهبي", "#16140F", "#221E15", "#F4EAD0", "#D4AF37", "#3A3220", "#16140F"),
        Palette("أخضر زيتي", "#F6F7F1", "#E6EADB", "#1F3B2D", "#3F7D58", "#CFDCC7", "#FFFFFF"),
        Palette("عنابي", "#FBF4F4", "#F2E3E3", "#3B1218", "#8C1C2B", "#E8CACD", "#FFFFFF"),
        Palette("أزرق سماوي", "#F3F8FB", "#E1EEF5", "#12303F", "#1F7A9E", "#C6DEEA", "#FFFFFF"),
        Palette("بنفسجي هادئ", "#F8F5FB", "#EBE4F3", "#2A1B3D", "#6B4A9E", "#D9CCEA", "#FFFFFF"),
        Palette("رمادي عصري", "#F5F5F5", "#E6E6E6", "#1F1F1F", "#4A4A4A", "#D4D4D4", "#FFFFFF"),
        Palette("برتقالي دافئ", "#FFF8F1", "#FBE8D3", "#3A2012", "#D9661F", "#F4D3B5", "#FFFFFF"),
        Palette("أسود وأبيض", "#FFFFFF", "#F0F0F0", "#0D0D0D", "#0D0D0D", "#D9D9D9", "#FFFFFF")
    )

    class B(val w: Float, val h: Float, val rtl: Boolean = false) {
        val els = ArrayList<CanvasElement>()
        fun shape(t: ShapeType, cx: Float, cy: Float, sw: Float, sh: Float, color: String, op: Float = 1f, rot: Float = 0f, tag: String = "") {
            els += CanvasElement(id = "e${els.size}", type = CanvasElementType.SHAPE, shapeType = t, x = cx, y = cy,
                width = sw, height = sh, colorHex = color, opacity = op, rotation = rot, text = tag)
        }
        fun rect(x0: Float, y0: Float, x1: Float, y1: Float, color: String, op: Float = 1f) =
            shape(ShapeType.RECTANGLE, (x0 + x1) / 2, (y0 + y1) / 2, x1 - x0, y1 - y0, color, op)
        fun text(s: String, cx: Float, cy: Float, size: Float, color: String, bold: Boolean = false, op: Float = 1f) {
            els += CanvasElement(id = "e${els.size}", type = CanvasElementType.TEXT, text = s, x = cx, y = cy,
                fontSize = size, isBold = bold, colorHex = color, opacity = op)
        }
        /** Text in a box of [bw] units centred at [cx]; align 1 = right (Arabic), 2 = left. */
        fun textBox(s: String, cx: Float, cy: Float, bw: Float, size: Float, color: String, align: Int, bold: Boolean = false, op: Float = 1f) {
            els += CanvasElement(id = "e${els.size}", type = CanvasElementType.TEXT, text = s, x = cx, y = cy,
                fontSize = size, isBold = bold, colorHex = color, opacity = op, width = bw, align = align)
        }
    }

    val cvLayoutNames = listOf("شريط علوي", "عمود جانبي", "دائرة كبيرة", "مينيمال", "فاصل ماسي", "شريط سفلي",
        "سداسي", "ركن مائل", "خطين", "شريط شريطة", "نصف علوي", "إطار",
        "مينيمال يمين", "ماسي يمين", "خطين يمين", "إطار يمين", "شريط سفلي يمين", "شريط علوي يمين")

    private fun sections(b: B, p: Palette, top: Float, step: Float = 62f, titleColor: String = p.accent, cx: Float = b.w / 2) {
        val data = listOf(
            "نبذة عني" to listOf("متخصص بخبرة تزيد عن 5 سنوات في مجال العمل"),
            "الخبرات" to listOf("مدير مبيعات | شركة النور | 2021 - الآن", "مسؤول مبيعات | شركة الأمل | 2018 - 2021"),
            "التعليم" to listOf("بكالوريوس تجارة | جامعة القاهرة | 2018"),
            "المهارات" to listOf("التواصل  •  التفاوض  •  إكسل  •  اللغة الإنجليزية")
        )
        var y = top
        data.forEach { (t, lines) ->
            if (b.rtl) {
                val rx = b.w - 44f
                b.textBox(t, rx - 130f, y, 260f, 13f, titleColor, 1, true)
                b.rect(rx - 36f, y + 11f, rx, y + 12.5f, p.accent, 0.8f)
                lines.forEachIndexed { i, l -> b.textBox(l, rx - 130f, y + 25f + i * 15f, 260f, 10.5f, p.ink, 1, false, 0.9f) }
            } else {
                b.text(t, cx, y, 13f, titleColor, true)
                b.rect(cx - 18f, y + 11f, cx + 18f, y + 12.5f, p.accent, 0.8f)
                lines.forEachIndexed { i, l -> b.text(l, cx, y + 25f + i * 15f, 10.5f, p.ink, false, 0.9f) }
            }
            y += step + (lines.size - 1) * 15f
        }
    }

    private fun contact(b: B, cx: Float, y: Float, color: String) =
        b.text("0100 000 0000  •  name@email.com  •  القاهرة", cx, y, 10f, color, false, 0.9f)

    private val rtlBases = listOf(3, 4, 8, 11, 5, 0)

    fun cv(layout: Int, p: Palette): List<CanvasElement> {
        val rtl = layout >= 12
        val layout = if (rtl) rtlBases[layout - 12] else layout
        val b = B(W, 480f, rtl)
        val dark = p.bg == "#16140F"
        b.rect(0f, 0f, W, 480f, p.bg)
        when (layout) {
            0 -> { // top band + avatar
                b.rect(0f, 0f, W, 130f, p.accent)
                b.shape(ShapeType.CIRCLE, W / 2, 62f, 62f, 62f, p.onAccent, 0.95f)
                b.text("م.ش", W / 2, 62f, 20f, p.accent, true)
                b.text("اسم صاحب السيرة", W / 2, 108f, 17f, p.onAccent, true)
                b.text("مدير مبيعات", W / 2, 124f, 11f, p.onAccent, false, 0.9f)
                contact(b, W / 2, 150f, p.ink); sections(b, p, 180f, 66f)
            }
            1 -> { // side stripe
                b.rect(0f, 0f, 26f, 480f, p.accent); b.rect(26f, 0f, 30f, 480f, p.soft)
                b.text("اسم صاحب السيرة", 208f, 44f, 20f, p.ink, true)
                b.text("مدير مبيعات", 208f, 66f, 12f, p.accent, true)
                contact(b, 208f, 88f, p.ink); b.rect(80f, 104f, 336f, 105f, p.soft)
                sections(b, p, 134f, 66f, cx = 208f)
            }
            2 -> { // big circle
                b.shape(ShapeType.CIRCLE, 40f, 20f, 230f, 230f, p.accent, 0.92f)
                b.shape(ShapeType.CIRCLE, 330f, 440f, 150f, 150f, p.soft, 0.9f)
                b.shape(ShapeType.CIRCLE, 58f, 66f, 66f, 66f, p.onAccent, 0.95f)
                b.text("م.ش", 58f, 66f, 20f, p.accent, true)
                b.text("اسم صاحب السيرة", 248f, 52f, 19f, p.ink, true)
                b.text("مدير مبيعات", 248f, 76f, 12f, p.accent, true)
                contact(b, W / 2, 148f, p.ink); sections(b, p, 190f, 64f)
            }
            3 -> { // minimal
                b.text("اسم صاحب السيرة", W / 2, 50f, 24f, p.ink, true)
                b.text("مدير مبيعات", W / 2, 78f, 12f, p.accent, true)
                b.shape(ShapeType.CIRCLE, W / 2, 98f, 6f, 6f, p.accent)
                contact(b, W / 2, 120f, p.ink)
                b.rect(40f, 138f, 320f, 139f, p.soft)
                sections(b, p, 170f, 68f)
            }
            4 -> { // diamond divider
                b.rect(0f, 0f, W, 10f, p.accent)
                b.text("اسم صاحب السيرة", W / 2, 54f, 22f, p.ink, true)
                b.text("مدير مبيعات", W / 2, 80f, 12f, p.accent, true)
                b.rect(50f, 105f, 160f, 106f, p.accent); b.rect(200f, 105f, 310f, 106f, p.accent)
                b.shape(ShapeType.DIAMOND, W / 2, 105.5f, 14f, 14f, p.accent)
                contact(b, W / 2, 128f, p.ink); sections(b, p, 165f, 66f)
            }
            5 -> { // footer band
                b.text("اسم صاحب السيرة", W / 2, 46f, 22f, p.accent, true)
                b.text("مدير مبيعات", W / 2, 72f, 12f, p.ink, true)
                b.rect(60f, 92f, 300f, 93f, p.soft)
                sections(b, p, 122f, 66f)
                b.rect(0f, 440f, W, 480f, p.accent)
                b.text("0100 000 0000  •  name@email.com  •  القاهرة", W / 2, 460f, 10.5f, p.onAccent, true)
            }
            6 -> { // hexagon
                b.shape(ShapeType.HEXAGON, W / 2, 62f, 78f, 68f, p.accent)
                b.shape(ShapeType.HEXAGON, W / 2, 62f, 66f, 57f, p.bg)
                b.text("م.ش", W / 2, 62f, 18f, p.accent, true)
                b.text("اسم صاحب السيرة", W / 2, 120f, 20f, p.ink, true)
                b.text("مدير مبيعات", W / 2, 142f, 12f, p.accent, true)
                contact(b, W / 2, 164f, p.ink); sections(b, p, 196f, 64f)
            }
            7 -> { // tilted corner
                b.shape(ShapeType.RECTANGLE, 330f, 0f, 300f, 110f, p.accent, 1f, -22f)
                b.shape(ShapeType.RECTANGLE, 345f, 8f, 300f, 40f, p.soft, 0.8f, -22f)
                b.text("اسم صاحب السيرة", 120f, 50f, 21f, p.ink, true)
                b.text("مدير مبيعات", 120f, 74f, 12f, p.accent, true)
                contact(b, 150f, 100f, p.ink); sections(b, p, 148f, 68f)
            }
            8 -> { // two lines
                b.rect(30f, 30f, 330f, 32f, p.accent)
                b.text("اسم صاحب السيرة", W / 2, 66f, 22f, p.ink, true)
                b.text("مدير مبيعات", W / 2, 92f, 12f, p.accent, true)
                b.rect(30f, 110f, 330f, 112f, p.accent)
                contact(b, W / 2, 132f, p.ink); sections(b, p, 168f, 66f)
            }
            9 -> { // ribbon
                b.rect(0f, 40f, W, 104f, p.accent)
                b.rect(0f, 104f, 22f, 118f, p.ink, 0.5f); b.rect(W - 22f, 104f, W, 118f, p.ink, 0.5f)
                b.text("اسم صاحب السيرة", W / 2, 66f, 21f, p.onAccent, true)
                b.text("مدير مبيعات", W / 2, 88f, 12f, p.onAccent, false, 0.9f)
                contact(b, W / 2, 138f, p.ink); sections(b, p, 172f, 66f)
            }
            10 -> { // top half
                b.rect(0f, 0f, W, 190f, p.accent)
                b.shape(ShapeType.CIRCLE, 300f, 20f, 160f, 160f, p.onAccent, 0.08f)
                b.shape(ShapeType.CIRCLE, W / 2, 62f, 74f, 74f, p.onAccent)
                b.text("م.ش", W / 2, 62f, 22f, p.accent, true)
                b.text("اسم صاحب السيرة", W / 2, 120f, 20f, p.onAccent, true)
                b.text("مدير مبيعات", W / 2, 142f, 12f, p.onAccent, false, 0.9f)
                b.text("0100 000 0000  •  name@email.com", W / 2, 168f, 10f, p.onAccent, false, 0.9f)
                sections(b, p, 220f, 62f)
            }
            else -> { // frame
                b.rect(14f, 14f, W - 14f, 16f, p.accent); b.rect(14f, 464f, W - 14f, 466f, p.accent)
                b.rect(14f, 14f, 16f, 466f, p.accent); b.rect(W - 16f, 14f, W - 14f, 466f, p.accent)
                b.shape(ShapeType.DIAMOND, W / 2, 15f, 12f, 12f, p.accent)
                b.text("اسم صاحب السيرة", W / 2, 58f, 22f, p.ink, true)
                b.text("مدير مبيعات", W / 2, 84f, 12f, p.accent, true)
                contact(b, W / 2, 108f, p.ink); b.rect(60f, 124f, 300f, 125f, p.soft)
                sections(b, p, 154f, 68f)
            }
        }
        return b.els
    }

    fun template(prefix: String, i: Int, j: Int, cat: String, asp: String, titleAr: String, titleEn: String, p: Palette, els: List<CanvasElement>) =
        DesignTemplate(
            id = "%s_%02d_%02d".format(prefix, i + 1, j + 1), titleAr = titleAr, titleEn = titleEn, category = cat,
            aspectRatio = asp, isPremium = false, pricingType = "FREE", priceEgp = 0, salesCount = 0,
            elements = els, bgStartHex = p.bg, bgEndHex = p.bg2
        )

    data class CvData(
        val name: String = "", val job: String = "", val phone: String = "", val email: String = "", val city: String = "",
        val about: String = "", val job1: String = "", val job2: String = "", val edu: String = "", val skills: String = ""
    )

    /** Fills a CV layout's sample text with the user's own data. Blank fields keep the sample so nothing looks broken. */
    fun cvFilled(layout: Int, p: Palette, d: CvData): List<CanvasElement> {
        val contactParts = listOf(d.phone, d.email, d.city).filter { it.isNotBlank() }
        val map = mapOf(
            "اسم صاحب السيرة" to d.name, "مدير مبيعات" to d.job,
            "م.ش" to d.name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString(".") { it.take(1) },
            "0100 000 0000  •  name@email.com  •  القاهرة" to contactParts.joinToString("  •  "),
            "0100 000 0000  •  name@email.com" to contactParts.take(2).joinToString("  •  "),
            "متخصص بخبرة تزيد عن 5 سنوات في مجال العمل" to d.about,
            "مدير مبيعات | شركة النور | 2021 - الآن" to d.job1,
            "مسؤول مبيعات | شركة الأمل | 2018 - 2021" to d.job2,
            "بكالوريوس تجارة | جامعة القاهرة | 2018" to d.edu,
            "التواصل  •  التفاوض  •  إكسل  •  اللغة الإنجليزية" to d.skills
        )
        return cv(layout, p).map { e ->
            if (e.type == CanvasElementType.TEXT) {
                val v = map[e.text]
                if (v != null && v.isNotBlank()) e.copy(text = v) else e
            } else e
        }
    }

    fun cvTemplate(layout: Int, pal: Int, d: CvData): DesignTemplate {
        val p = palettes[pal]
        return template("cv", layout, pal, "سير ذاتية احترافية", "3:4", "سيرتي الذاتية", "My CV", p, cvFilled(layout, p, d))
    }

    fun cvTemplates(): List<DesignTemplate> = cvLayoutNames.indices.flatMap { i ->
        palettes.mapIndexed { j, p ->
            template("cv", i, j, "سير ذاتية احترافية", "3:4", "سيرة ذاتية ${cvLayoutNames[i]} ${p.nameAr}", "CV ${i + 1}-${j + 1}", p, cv(i, p))
        }
    }

    fun all(): List<DesignTemplate> = cvTemplates() + CertEngine.templates() + CardEngine.templates() + MenuEngine.templates()
}
