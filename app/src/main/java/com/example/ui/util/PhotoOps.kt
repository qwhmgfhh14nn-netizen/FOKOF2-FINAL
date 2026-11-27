package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlin.math.max

/** Simple photo operations on bitmaps: adjust, rotate/flip, meme text, collage and emoji stickers. */
object PhotoOps {
    /** brightness -1..1, contrast 0.5..1.5, saturation 0..2, rotate in 90 degree steps, optional mirror. */
    fun adjust(src: Bitmap, brightness: Float, contrast: Float, saturation: Float, rotate90: Int, mirror: Boolean): Bitmap {
        val m = Matrix()
        if (mirror) m.postScale(-1f, 1f, src.width / 2f, src.height / 2f)
        m.postRotate(((rotate90 % 4 + 4) % 4) * 90f, src.width / 2f, src.height / 2f)
        val quarter = (rotate90 % 2 != 0)
        val ow = if (quarter) src.height else src.width
        val oh = if (quarter) src.width else src.height
        val out = Bitmap.createBitmap(ow, oh, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.translate((ow - src.width) / 2f, (oh - src.height) / 2f)
        val cm = ColorMatrix().apply { setSaturation(saturation) }
        val t = (-0.5f * contrast + 0.5f + brightness) * 255f
        cm.postConcat(ColorMatrix(floatArrayOf(contrast, 0f, 0f, 0f, t, 0f, contrast, 0f, 0f, t, 0f, 0f, contrast, 0f, t, 0f, 0f, 0f, 1f, 0f)))
        val p = Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(cm) }
        c.drawBitmap(src, m, p)
        return out
    }

    /** Classic meme: bold white text with a black outline, top and bottom. No boxes behind the text. */
    fun meme(src: Bitmap, top: String, bottom: String): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val c = Canvas(out)
        fun draw(text: String, atTop: Boolean) {
            if (text.isBlank()) return
            val size = out.width * 0.1f
            val fill = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = size; isFakeBoldText = true }
            val stroke = TextPaint(fill).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = size * 0.16f; strokeJoin = Paint.Join.ROUND }
            val w = (out.width * 0.92f).toInt()
            fun layout(p: TextPaint) = StaticLayout.Builder.obtain(text, 0, text.length, p, w).setAlignment(Layout.Alignment.ALIGN_CENTER).setMaxLines(3).build()
            val l1 = layout(stroke); val l2 = layout(fill)
            c.save()
            c.translate(out.width * 0.04f, if (atTop) out.height * 0.03f else out.height * 0.97f - l2.height)
            l1.draw(c); l2.draw(c)
            c.restore()
        }
        draw(top, true); draw(bottom, false)
        return out
    }

    /** layout 0: two side by side, 1: two stacked, 2: 2x2 grid, 3: one big on top and two below. Square canvas of [size] px. */
    fun collage(images: List<Bitmap>, layout: Int, size: Int = 1080, gap: Int = 8): Bitmap {
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.drawColor(Color.WHITE)
        if (images.isEmpty()) return out
        val s = size.toFloat(); val g = gap.toFloat(); val half = (s - g) / 2f
        val cells: List<RectF> = when (layout) {
            0 -> listOf(RectF(0f, 0f, half, s), RectF(half + g, 0f, s, s))
            1 -> listOf(RectF(0f, 0f, s, half), RectF(0f, half + g, s, s))
            2 -> listOf(RectF(0f, 0f, half, half), RectF(half + g, 0f, s, half), RectF(0f, half + g, half, s), RectF(half + g, half + g, s, s))
            else -> listOf(RectF(0f, 0f, s, half), RectF(0f, half + g, half, s), RectF(half + g, half + g, s, s))
        }
        val p = Paint(Paint.FILTER_BITMAP_FLAG)
        cells.forEachIndexed { i, r ->
            val b = images[i % images.size]
            val sc = max(r.width() / b.width, r.height() / b.height)
            val sw = r.width() / sc; val sh = r.height() / sc
            val sx = ((b.width - sw) / 2f).toInt(); val sy = ((b.height - sh) / 2f).toInt()
            c.save(); c.clipRect(r)
            c.drawBitmap(b, android.graphics.Rect(sx, sy, (sx + sw).toInt(), (sy + sh).toInt()), r, p)
            c.restore()
        }
        return out
    }

    /** Puts an emoji sticker at a fraction of the photo (x, y = 0..1 centre) with a size as a fraction of the width. */
    fun sticker(src: Bitmap, emoji: String, x: Float, y: Float, sizeFrac: Float): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val c = Canvas(out)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = out.width * sizeFrac; textAlign = Paint.Align.CENTER }
        c.drawText(emoji, out.width * x, out.height * y - (p.ascent() + p.descent()) / 2f, p)
        return out
    }
}
