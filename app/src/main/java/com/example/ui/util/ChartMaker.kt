package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Draws real charts (bars, pie, line) from user numbers on an ivory background with the gold palette. All on-device. */
object ChartMaker {
    const val BARS = 0
    const val PIE = 1
    const val LINE = 2

    private val palette = listOf("#D4AF37", "#8B6914", "#E5C56B", "#6B5310", "#F3E5AB", "#A67C00", "#4A3A08", "#C9A227")
    private const val BG = "#FFFBF2"
    private const val INK = "#1A1A1A"
    private const val FAINT = "#8A8578"

    fun render(kind: Int, title: String, entries: List<Pair<String, Float>>, size: Int = 1080): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.parseColor(BG))
        val data = entries.filter { it.first.isNotBlank() }.take(8)
        val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(INK) }
        val faint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(FAINT) }

        // Title
        ink.textSize = size * 0.05f; ink.textAlign = Paint.Align.CENTER; ink.isFakeBoldText = true
        if (title.isNotBlank()) c.drawText(title, size / 2f, size * 0.09f, ink)
        ink.isFakeBoldText = false

        if (data.isEmpty()) {
            faint.textSize = size * 0.035f; faint.textAlign = Paint.Align.CENTER
            c.drawText("ضيف أرقام عشان يظهر الشارت", size / 2f, size / 2f, faint)
            return bmp
        }

        val top = size * 0.16f
        val bottom = size * 0.82f
        val left = size * 0.08f
        val right = size * 0.94f
        val maxV = max(data.maxOf { it.second }, 1f)

        when (kind) {
            BARS -> {
                val n = data.size
                val slot = (right - left) / n
                val barW = slot * 0.62f
                ink.textSize = size * 0.028f
                faint.textSize = size * 0.026f
                data.forEachIndexed { i, (label, v) ->
                    val cx = left + slot * i + slot / 2
                    val h = (v / maxV) * (bottom - top)
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(palette[i % palette.size]) }
                    c.drawRoundRect(RectF(cx - barW / 2, bottom - h, cx + barW / 2, bottom), barW * 0.18f, barW * 0.18f, p)
                    ink.textAlign = Paint.Align.CENTER
                    val valTxt = if (v == v.toLong().toFloat()) v.toLong().toString() else String.format("%.1f", v)
                    c.drawText(valTxt, cx, bottom - h - size * 0.012f, ink)
                    faint.textAlign = Paint.Align.CENTER
                    c.drawText(label.take(12), cx, bottom + size * 0.04f, faint)
                }
                faint.strokeWidth = size * 0.002f
                c.drawLine(left, bottom, right, bottom, faint)
            }
            PIE -> {
                val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.01f)
                val d = min(right - left, bottom - top) * 0.9f
                val oval = RectF(size / 2f - d / 2, top + (bottom - top - d) / 2, size / 2f + d / 2, top + (bottom - top - d) / 2 + d)
                var angle = -90f
                data.forEachIndexed { i, (_, v) ->
                    val sweep = 360f * v / total
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(palette[i % palette.size]) }
                    c.drawArc(oval, angle, sweep, true, p)
                    angle += sweep
                }
                // legend
                var ly = bottom + size * 0.05f
                ink.textSize = size * 0.028f; ink.textAlign = Paint.Align.LEFT
                data.forEachIndexed { i, (label, v) ->
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(palette[i % palette.size]) }
                    val lx = size * 0.12f + (i % 2) * size * 0.42f
                    if (i % 2 == 0 && i > 0) ly += size * 0.05f
                    c.drawRoundRect(RectF(lx, ly - size * 0.02f, lx + size * 0.03f, ly + size * 0.01f), 6f, 6f, p)
                    val pct = (100f * v / total).toInt()
                    c.drawText("$label ${pct}%", lx + size * 0.045f, ly, ink)
                }
            }
            else -> { // LINE
                val n = data.size
                val stepX = if (n > 1) (right - left) / (n - 1) else 0f
                val pts = data.mapIndexed { i, (_, v) -> Pair(left + stepX * i, bottom - (v / maxV) * (bottom - top)) }
                val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D4AF37"); strokeWidth = size * 0.008f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
                for (i in 0 until pts.size - 1) c.drawLine(pts[i].first, pts[i].second, pts[i + 1].first, pts[i + 1].second, line)
                val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8B6914") }
                ink.textSize = size * 0.026f; faint.textSize = size * 0.026f
                pts.forEachIndexed { i, (px, py) ->
                    c.drawCircle(px, py, size * 0.012f, dot)
                    ink.textAlign = Paint.Align.CENTER
                    val v = data[i].second
                    val valTxt = if (v == v.toLong().toFloat()) v.toLong().toString() else String.format("%.1f", v)
                    c.drawText(valTxt, px, py - size * 0.025f, ink)
                    faint.textAlign = Paint.Align.CENTER
                    c.drawText(data[i].first.take(12), px, bottom + size * 0.04f, faint)
                }
                faint.strokeWidth = size * 0.002f
                c.drawLine(left, bottom, right, bottom, faint)
            }
        }
        return bmp
    }
}
