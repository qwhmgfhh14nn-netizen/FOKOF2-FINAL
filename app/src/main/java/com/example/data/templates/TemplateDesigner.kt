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
    private val ornamentChars = setOf('✨', '⭐', '🌟', '★', '☆', '✦', '✧', '➜', '➔', '→', '←', '»', '«', '❖', '◆', '✪')
    private fun isOrnament(e: CanvasElement): Boolean {
        val tx = e.text.trim()
        return (e.type == CanvasElementType.TEXT || e.type == CanvasElementType.CALLIGRAPHY) && tx.isNotEmpty() && tx.length <= 4 &&
            tx.all { it in ornamentChars || it == '\uFE0F' || it.code in 0xD800..0xDFFF }.and(tx.any { it in ornamentChars || it.code in 0xD800..0xDFFF })
    }

    fun enrich(t0: DesignTemplate): DesignTemplate {
        val t = t0.copy(elements = t0.elements.filter { !isOrnament(it) })
        if (t.elements.isEmpty()) return t0
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
        // icons (emoji-only lines) get their own slot above the text block so they never sit under a title
        fun isIcon(e: CanvasElement) = (e.type == CanvasElementType.TEXT || e.type == CanvasElementType.CALLIGRAPHY) && e.text.trim().length <= 3 &&
            e.text.trim().isNotEmpty() && e.text.trim().first().code > 0x2000
        val icons = t.elements.filter { isIcon(it) }
        val iconH = if (icons.isEmpty()) 0f else (icons.maxOf { it.fontSize } * fs * 1.75f + 18f)
        // rows pushed down when the row above wraps onto several lines (keeps badges/buttons clear of the text)
        val rows = t.elements.filter { !isIcon(it) }.sortedBy { it.y }
        val yEff = HashMap<String, Float>()
        run {
            var cum = 0f
            var rowY = rows.firstOrNull()?.y ?: 0f
            var rowExtra = 0f
            for (e in rows) {
                if (e.y - rowY > 3f) { cum += rowExtra; rowY = e.y; rowExtra = 0f }
                if (e.type == CanvasElementType.TEXT || e.type == CanvasElementType.CALLIGRAPHY) {
                    val lines = Math.ceil((e.text.length * e.fontSize * fs * 0.5f / 300f).toDouble()).toInt().coerceAtLeast(1)
                    rowExtra = max(rowExtra, (lines - 1) * e.fontSize * fs * 1.25f)
                }
                yEff[e.id] = e.y + cum
            }
        }
        val ys = rows.map { yEff[it.id] ?: it.y }
        val ymin = ys.minOrNull() ?: 0f
        val ymax = ys.maxOrNull() ?: 0f
        val span = max(ymax - ymin, 1f)
        // keep the original vertical rhythm of the text block (no squashing), within sane bounds
        val bh = (span * fs + 44f + iconH).coerceIn(80f, h * 0.62f)

        // text block vertical placement per archetype
        val blockTop = when (arch) {
            0, 5 -> h - bh - 26f
            1 -> 30f
            2 -> (h - bh) / 2f
            3 -> (h - bh) / 2f
            else -> h - bh - 36f
        }.coerceIn(40f, max(40f, h - bh - 40f))
        val blockMid = blockTop + bh / 2f
        val lumZone = when (arch) {
            0, 5, 4 -> zoneLum(2)
            1 -> zoneLum(0)
            else -> zoneLum(1)
        }
        val lightArt = lumZone > 0.5f
        val midArt = lumZone in 0.38f..0.66f
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

        // No ornaments on top of the artwork (no stars, frames, diamonds, tinted bands).
        // The only layer added is one subtle neutral fade behind the text block, so the picture stays visible.
        val deco = mutableListOf<CanvasElement>()
        val above = mutableListOf<CanvasElement>()
        val fadeOp = if (midArt) 0.55f else 0.42f
        when (arch) {
            1 -> deco += shape(ShapeType.RECTANGLE, w / 2, (bh + 90f) / 2f, w, bh + 90f, scrimColor, fadeOp, tag = "grad:t")
            3 -> deco += shape(ShapeType.RECTANGLE, w * 0.36f, h / 2, w * 0.72f, h, scrimColor, fadeOp, tag = "grad:l")
            2 -> { val gh = bh + 120f; deco += shape(ShapeType.RECTANGLE, w / 2, h - gh / 2f, w, gh, scrimColor, fadeOp, tag = "grad:b") }
            else -> { val gh = bh + 90f; deco += shape(ShapeType.RECTANGLE, w / 2, h - gh / 2f, w, gh, scrimColor, fadeOp, tag = "grad:b") }
        }

        val shapes = t.elements.filter { it.type == CanvasElementType.SHAPE && it.shapeType != ShapeType.STAR && it.height <= 60f && it.width <= 300f }
        fun carriesText(sh: CanvasElement) = t.elements.any { x ->
            x.type != CanvasElementType.SHAPE && abs(x.x - sh.x) <= sh.width / 2 && abs(x.y - sh.y) <= sh.height / 2
        }
        val keepEls = t.elements.filter { e ->
            e.type != CanvasElementType.SHAPE ||
                (e.shapeType != ShapeType.STAR && carriesText(e) && e.height <= 60f && e.width <= 300f)
        }
        val mapped = keepEls.map { e ->
            val ny = if (isIcon(e)) blockTop + iconH * 0.38f
                else if (ymax == ymin) blockMid + iconH / 2f
                else blockTop + iconH + (bh - iconH) * (((yEff[e.id] ?: e.y) - ymin) / span)
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
            // a button/badge must be wide enough for the text that sits on it
            val longest = t.elements.filter { x ->
                x.type != CanvasElementType.SHAPE && abs(x.x - e.x) <= e.width / 2 && abs(x.y - e.y) <= e.height / 2
            }.maxOfOrNull { it.text.length * it.fontSize * fs * 0.56f + 28f } ?: 0f
            val isBox = e.type == CanvasElementType.SHAPE && e.shapeType != ShapeType.STAR && longest > e.width
            e.copy(
                x = ex, y = ny, colorHex = color,
                fontSize = if (e.type == CanvasElementType.SHAPE || e.fontSize <= 0f) e.fontSize * fs else max(e.fontSize * fs, if (tall) 12.5f else 0f),
                width = if (isBox) min(longest, 330f) else e.width,
                height = if (e.type == CanvasElementType.SHAPE) e.height * fs else e.height
            )
        }
        return t.copy(elements = deco + above + mapped)
    }
}
