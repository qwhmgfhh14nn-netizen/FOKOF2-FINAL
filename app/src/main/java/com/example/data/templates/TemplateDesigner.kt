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

    private fun isLightFill(hex: String) = lum(hex) > 0.75f

    /**
     * Magazine-style layouts: text sits directly on the artwork over a soft gradient that fades into the picture
     * (never a big opaque box). Six compositions, chosen per template, each with its own ornaments.
     * Text colours follow the real brightness of the art behind them (see [TemplateArtMeta]).
     */
    fun enrich(t: DesignTemplate): DesignTemplate {
        if (t.elements.isEmpty()) return t
        val w = 360f
        val h = w / ratio(t.aspectRatio)
        val seed = abs(t.id.hashCode())
        val tall = h > 500f
        val small = h < 250f
        val fs = if (small) 0.85f else if (tall) 1.25f else 1f
        val arch = seed % 6
        val meta = TemplateArtMeta.LUM[t.id]
        // meta = top, mid, bottom, left, right brightness of the artwork (0 dark .. 1 light)
        fun zoneLum(a: Int): Float = meta?.getOrNull(a) ?: 0.3f
        val ys = t.elements.map { it.y }
        val ymin = ys.minOrNull() ?: 0f
        val ymax = ys.maxOrNull() ?: 0f
        val span = max(ymax - ymin, 1f)
        val bh = min(h * 0.40f, if (small) 120f else 200f).coerceAtLeast(80f)

        // text block vertical placement per archetype
        val blockTop = when (arch) {
            0, 5 -> h - bh - 26f
            1 -> 30f
            2 -> (h - bh) / 2f
            3 -> (h - bh) / 2f
            else -> h - bh - 36f
        }.coerceIn(14f, max(14f, h - bh - 14f))
        val blockMid = blockTop + bh / 2f
        val lumZone = when (arch) {
            0, 5, 4 -> zoneLum(2)
            1 -> zoneLum(0)
            else -> zoneLum(1)
        }
        val lightArt = lumZone > 0.58f
        val ink = if (lightArt) "#1B1630" else "#FFFFFF"
        val accent = if (lightArt) "#9A7416" else if (seed % 2 == 0) "#D4AF37" else "#F2D67C"
        val scrimColor = if (lightArt) "#FFFFFF" else "#000000"
        val cx = if (arch == 3) w * 0.56f else w / 2f

        var n = 0
        fun shape(type: ShapeType, x: Float, y: Float, sw: Float, sh: Float, color: String, op: Float, rot: Float = 0f, tag: String = "") =
            CanvasElement(
                id = "d_${t.id}_${n++}", type = CanvasElementType.SHAPE, shapeType = type, text = tag,
                x = x, y = y, width = sw, height = sh, colorHex = color, opacity = op, rotation = rot
            )

        val deco = mutableListOf<CanvasElement>()
        val above = mutableListOf<CanvasElement>()
        when (arch) {
            0 -> { // bottom fade + gold rule + diamond
                val gh = bh + 90f
                deco += shape(ShapeType.RECTANGLE, w / 2, h - gh / 2f, w, gh, scrimColor, 0.72f, tag = "grad:b")
                above += shape(ShapeType.RECTANGLE, w / 2, blockTop - 10f, 70f, 2f, accent, 1f)
                above += shape(ShapeType.DIAMOND, w / 2, blockTop - 10f, 9f, 9f, accent, 1f)
            }
            1 -> { // top fade + underline
                val gh = bh + 90f
                deco += shape(ShapeType.RECTANGLE, w / 2, gh / 2f, w, gh, scrimColor, 0.72f, tag = "grad:t")
                above += shape(ShapeType.RECTANGLE, w / 2, blockTop + bh + 10f, 90f, 2f, accent, 1f)
            }
            2 -> { // double hairline frame, soft overall veil, corner diamonds
                deco += shape(ShapeType.RECTANGLE, w / 2, h / 2, w, h, scrimColor, 0.30f)
                val g = 14f; val th = 1.6f
                above += shape(ShapeType.RECTANGLE, w / 2, g, w - 2 * g, th, accent, 0.95f)
                above += shape(ShapeType.RECTANGLE, w / 2, h - g, w - 2 * g, th, accent, 0.95f)
                above += shape(ShapeType.RECTANGLE, g, h / 2, th, h - 2 * g, accent, 0.95f)
                above += shape(ShapeType.RECTANGLE, w - g, h / 2, th, h - 2 * g, accent, 0.95f)
                val g2 = 21f
                above += shape(ShapeType.RECTANGLE, w / 2, g2, w - 2 * g2, 0.8f, accent, 0.7f)
                above += shape(ShapeType.RECTANGLE, w / 2, h - g2, w - 2 * g2, 0.8f, accent, 0.7f)
                for ((px, py) in listOf(g to g, w - g to g, g to h - g, w - g to h - g))
                    above += shape(ShapeType.DIAMOND, px, py, 11f, 11f, accent, 1f)
            }
            3 -> { // left-side fade + vertical gold bar
                deco += shape(ShapeType.RECTANGLE, w * 0.36f, h / 2, w * 0.72f, h, scrimColor, 0.70f, tag = "grad:l")
                above += shape(ShapeType.RECTANGLE, 24f, blockMid, 3f, bh * 0.9f, accent, 1f)
                above += shape(ShapeType.CIRCLE, 24f, blockMid - bh * 0.45f - 7f, 8f, 8f, accent, 1f)
            }
            4 -> { // bottom fade + corner brackets + badge star
                val gh = bh + 80f
                deco += shape(ShapeType.RECTANGLE, w / 2, h - gh / 2f, w, gh, scrimColor, 0.68f, tag = "grad:b")
                val g = 16f; val l = 34f; val th = 2.4f
                above += shape(ShapeType.RECTANGLE, g + l / 2, g, l, th, accent, 1f)
                above += shape(ShapeType.RECTANGLE, g, g + l / 2, th, l, accent, 1f)
                above += shape(ShapeType.RECTANGLE, w - g - l / 2, g, l, th, accent, 1f)
                above += shape(ShapeType.RECTANGLE, w - g, g + l / 2, th, l, accent, 1f)
                above += shape(ShapeType.STAR, w / 2, blockTop - 14f, 16f, 16f, accent, 1f)
            }
            else -> { // bottom fade + slim gold edge line across the page + small side stars
                val gh = bh + 100f
                deco += shape(ShapeType.RECTANGLE, w / 2, h - gh / 2f, w, gh, scrimColor, 0.74f, tag = "grad:b")
                above += shape(ShapeType.RECTANGLE, w / 2, h - 8f, w - 40f, 2f, accent, 1f)
                above += shape(ShapeType.STAR, 40f, blockTop - 8f, 12f, 12f, accent, 1f)
                above += shape(ShapeType.STAR, w - 40f, blockTop - 8f, 12f, 12f, accent, 1f)
            }
        }

        val shapes = t.elements.filter { it.type == CanvasElementType.SHAPE }
        val mapped = t.elements.map { e ->
            val ny = if (ymax == ymin) blockMid else blockTop + bh * ((e.y - ymin) / span)
            val ex = if (arch == 3) cx else w / 2f
            var color = e.colorHex
            if (e.type == CanvasElementType.TEXT || e.type == CanvasElementType.CALLIGRAPHY) {
                // keep original colour only when the text sits on one of the template's own light buttons/badges
                val onOwnShape = shapes.any { s ->
                    abs(e.x - s.x) <= s.width / 2 && abs(e.y - s.y) <= s.height / 2 && isLightFill(s.colorHex)
                }
                color = if (onOwnShape) e.colorHex else ink
                // the first small kicker line becomes the accent colour for rhythm
                if (!onOwnShape && e.fontSize <= 14f * 1.01f && e.y == ymin && t.elements.size > 2) color = accent
            }
            e.copy(
                x = ex, y = ny, colorHex = color,
                fontSize = e.fontSize * fs,
                height = if (e.type == CanvasElementType.SHAPE) e.height * fs else e.height
            )
        }
        return t.copy(elements = deco + above + mapped)
    }
}
