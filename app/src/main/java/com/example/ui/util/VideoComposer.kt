package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlin.math.max
import kotlin.math.min

/** Draws one frame of a photo slideshow video at a given time. Pure Canvas code, so it can be unit-tested and previewed. */
object VideoComposer {
    const val FADE = 0
    const val SLIDE = 1
    const val ZOOM = 2

    fun totalFrames(slideCount: Int, secondsPerSlide: Float, fps: Int): Int = max(1, (slideCount * secondsPerSlide * fps).toInt())

    fun draw(canvas: Canvas, w: Int, h: Int, slides: List<Bitmap>, captions: List<String>, timeSec: Float, secondsPerSlide: Float, transition: Int) {
        canvas.drawColor(Color.BLACK)
        if (slides.isEmpty()) return
        val n = slides.size
        val i = min((timeSec / secondsPerSlide).toInt(), n - 1).coerceAtLeast(0)
        val local = (timeSec - i * secondsPerSlide).coerceIn(0f, secondsPerSlide)
        val trDur = min(0.6f, secondsPerSlide / 3f)
        val hasNext = i + 1 < n
        val p = if (hasNext && local > secondsPerSlide - trDur) ((local - (secondsPerSlide - trDur)) / trDur).coerceIn(0f, 1f) else 0f
        val zoomNow = 1f + 0.08f * (local / secondsPerSlide)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        when {
            p <= 0f || !hasNext -> drawCover(canvas, slides[i], w, h, zoomNow, 0f, 255, paint)
            transition == SLIDE -> {
                drawCover(canvas, slides[i], w, h, 1.08f, -p * w, 255, paint)
                drawCover(canvas, slides[i + 1], w, h, 1f, (1f - p) * w, 255, paint)
            }
            transition == ZOOM -> {
                drawCover(canvas, slides[i], w, h, zoomNow, 0f, 255, paint)
                drawCover(canvas, slides[i + 1], w, h, 1.35f - 0.35f * p, 0f, (p * 255).toInt(), paint)
            }
            else -> {
                drawCover(canvas, slides[i], w, h, zoomNow, 0f, 255, paint)
                drawCover(canvas, slides[i + 1], w, h, 1f, 0f, (p * 255).toInt(), paint)
            }
        }
        val ci = if (p > 0.5f) i + 1 else i
        val cap = captions.getOrElse(ci) { "" }
        if (cap.isNotBlank()) drawCaption(canvas, w, h, cap)
    }

    private fun drawCover(canvas: Canvas, b: Bitmap, w: Int, h: Int, zoom: Float, dx: Float, alpha: Int, paint: Paint) {
        val scale = max(w.toFloat() / b.width, h.toFloat() / b.height) * zoom
        val dw = b.width * scale
        val dh = b.height * scale
        val left = (w - dw) / 2f + dx
        val top = (h - dh) / 2f
        paint.alpha = alpha
        canvas.drawBitmap(b, null, android.graphics.RectF(left, top, left + dw, top + dh), paint)
        paint.alpha = 255
    }

    /** Caption sits on a dark gradient at the bottom, never on an opaque box. */
    private fun drawCaption(canvas: Canvas, w: Int, h: Int, text: String) {
        val scrimTop = h * 0.62f
        val scrim = Paint().apply {
            shader = LinearGradient(0f, scrimTop, 0f, h.toFloat(), Color.argb(0, 0, 0, 0), Color.argb(190, 0, 0, 0), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(Rect(0, scrimTop.toInt(), w, h), scrim)
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = w * 0.062f
            isFakeBoldText = true
        }
        val pad = (w * 0.08f).toInt()
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, w - pad * 2)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setMaxLines(3).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
        canvas.save()
        canvas.translate(pad.toFloat(), h - layout.height - h * 0.07f)
        layout.draw(canvas)
        canvas.restore()
    }
}
