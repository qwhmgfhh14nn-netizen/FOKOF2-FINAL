package com.example.data.templates

import com.example.data.model.CanvasElement
import com.example.data.model.CanvasElementType
import com.example.data.model.DesignTemplate
import com.example.data.model.ShapeType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Turns the compact seed definition of a default template into a full, ratio-aware design:
 * every template gets its own layout archetype (frame, corner circles, bottom band, side bar,
 * centre card, ribbon), accent shapes and stars, and its original text/shape layers are
 * re-positioned into the template's real aspect-ratio canvas (360 x 360/ratio design units).
 * Output is deterministic per template id, so previews, the Studio and exports match.
 */
object TemplateDesigner {

    private fun ratio(s: String): Float = when (s) {
        "16:9" -> 16f / 9f
        "9:16" -> 9f / 16f
        "4:5" -> 0.8f
        else -> 1f
    }

    private fun rgb(hex: String): Triple<Int, Int, Int> {
        val h = hex.removePrefix("#")
        return try {
            Triple(h.substring(0, 2).toInt(16), h.substring(2, 4).toInt(16), h.substring(4, 6).toInt(16))
        } catch (e: Exception) { Triple(200, 200, 200) }
    }

    private fun lum(hex: String): Float {
        val (r, g, b) = rgb(hex)
        return (0.299f * r + 0.587f * g + 0.114f * b) / 255f
    }

    private fun darken(hex: String, f: Float): String {
        val (r, g, b) = rgb(hex)
        return "#%02X%02X%02X".format((r * f).toInt(), (g * f).toInt(), (b * f).toInt())
    }

    fun enrich(t: DesignTemplate): DesignTemplate {
        if (t.elements.isEmpty()) return t
        val w = 360f
        val h = w / ratio(t.aspectRatio)
        val seed = abs(t.id.hashCode())
        val arch = seed % 6
        val light = (lum(t.bgStartHex) + lum(t.bgEndHex)) / 2f > 0.55f
        val accent = if (light) darken(t.bgEndHex, 0.55f) else if (seed % 2 == 0) "#D4AF37" else "#F5D77A"
        val panel = t.bgStartHex
        val soft = if (light) "#FFFFFF" else "#000000"
        val small = h < 250f
        val fs = if (small) 0.85f else 1f

        val ys = t.elements.map { it.y }
        val ymin = (ys.minOrNull() ?: 0f)
        val ymax = (ys.maxOrNull() ?: 0f)
        val span = max(ymax - ymin, 1f)
        val rh = min(h * 0.40f, 175f).coerceAtLeast(84f)
        val centre = when (arch) {
            2 -> h - 22f - rh / 2f
            3 -> h * 0.58f
            4 -> h * 0.42f
            5 -> h * 0.62f
            else -> h * 0.5f
        }.coerceIn(rh / 2f + 30f, h - rh / 2f - 20f)
        val top = centre - rh / 2f
        val bottom = centre + rh / 2f
        val pTop = (top - 20f).coerceAtLeast(10f)
        val pBottom = if (arch == 2) h else (bottom + 20f).coerceAtMost(h - 10f)
        val pH = pBottom - pTop
        val pMid = (pTop + pBottom) / 2f

        var n = 0
        fun shape(type: ShapeType, x: Float, y: Float, sw: Float, sh: Float, color: String, op: Float, rot: Float = 0f) =
            CanvasElement(
                id = "d_${t.id}_${n++}", type = CanvasElementType.SHAPE, shapeType = type,
                x = x, y = y, width = sw, height = sh, colorHex = color, opacity = op, rotation = rot
            )

        val deco = mutableListOf<CanvasElement>()
        when (arch) {
            0 -> { // gold frame + centre card
                val g = 14f; val th = 2.5f
                deco += shape(ShapeType.RECTANGLE, w / 2, g, w - 2 * g, th, accent, 0.95f)
                deco += shape(ShapeType.RECTANGLE, w / 2, h - g, w - 2 * g, th, accent, 0.95f)
                deco += shape(ShapeType.RECTANGLE, g, h / 2, th, h - 2 * g, accent, 0.95f)
                deco += shape(ShapeType.RECTANGLE, w - g, h / 2, th, h - 2 * g, accent, 0.95f)
                deco += shape(ShapeType.ROUNDED_RECT, w / 2, pMid, 300f, pH, panel, 0.66f)
            }
            1 -> { // corner circles + centre card
                deco += shape(ShapeType.CIRCLE, w - 20f, 10f, 190f, 190f, accent, 0.38f)
                deco += shape(ShapeType.CIRCLE, 20f, h - 10f, 170f, 170f, soft, 0.28f)
                deco += shape(ShapeType.ROUNDED_RECT, w / 2, pMid, 316f, pH, panel, 0.68f)
            }
            2 -> { // bottom band
                deco += shape(ShapeType.RECTANGLE, w / 2, pMid, w, pH, panel, 0.8f)
                deco += shape(ShapeType.RECTANGLE, w / 2, pTop, w, 4f, accent, 1f)
            }
            3 -> { // side bar + card
                deco += shape(ShapeType.ROUNDED_RECT, w / 2, pMid, 304f, pH, panel, 0.68f)
                deco += shape(ShapeType.RECTANGLE, 32f, pMid, 5f, pH - 30f, accent, 1f)
                deco += shape(ShapeType.CIRCLE, 32f, pTop + 12f, 14f, 14f, accent, 1f)
            }
            4 -> { // centre card + diamond
                deco += shape(ShapeType.ROUNDED_RECT, w / 2, pMid, 316f, pH, panel, 0.7f)
                deco += shape(ShapeType.DIAMOND, w / 2, pTop, 20f, 20f, accent, 1f)
                deco += shape(ShapeType.DIAMOND, w / 2, pBottom, 20f, 20f, accent, 1f)
            }
            else -> { // diagonal ribbon
                deco += shape(ShapeType.RECTANGLE, w / 2, pMid, 440f, pH, accent, 0.9f, -8f)
                deco += shape(ShapeType.RECTANGLE, w / 2, pMid, 440f, pH - 12f, panel, 0.9f, -8f)
            }
        }
        // stars above the text area, placed differently per template
        val sx = 40f + (seed % 5) * 62f
        deco += shape(ShapeType.STAR, sx, max(26f, pTop - 6f), 18f, 18f, "#F5D77A", 0.95f)
        deco += shape(ShapeType.STAR, w - sx, max(40f, pTop - 22f), 12f, 12f, "#FFFFFF", 0.85f)

        val mapped = t.elements.map { e ->
            val ny = top + rh * ((e.y - ymin) / span)
            e.copy(
                y = if (ymax == ymin) centre else ny,
                fontSize = e.fontSize * fs,
                height = if (e.type == CanvasElementType.SHAPE) e.height * fs else e.height
            )
        }
        return t.copy(elements = deco + mapped)
    }
}
