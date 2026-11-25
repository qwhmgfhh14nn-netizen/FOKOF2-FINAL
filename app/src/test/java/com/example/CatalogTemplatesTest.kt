package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.example.data.model.CanvasElementType
import com.example.data.templates.CatalogTemplates
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import com.example.ui.components.TemplateThumb
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Integrity + no-repeat rules for the generated catalog, and real renders of a sample for visual review. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1000dp-h1400dp-xhdpi")
class CatalogTemplatesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val json: String get() = File("src/main/assets/templates/catalog.json").readText()
    private val hexRe = Regex("#[0-9A-Fa-f]{6}")

    @Test
    fun catalog_integrity_and_no_repeats() {
        val list = CatalogTemplates.parse(json)
        val raw = JSONArray(json)
        val problems = ArrayList<String>()
        assertTrue("catalog empty", list.isNotEmpty())
        val ids = HashSet<String>()
        val fps = HashSet<String>()
        val heads = HashSet<String>()
        val perCat = HashMap<String, MutableList<List<Int>>>()
        val reserved = (FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates()).map { it.id }.toSet()
        for (i in 0 until list.size) {
            val t = list[i]
            val o = raw.getJSONObject(i)
            if (!ids.add(t.id)) problems += "dup id ${t.id}"
            if (t.id in reserved) problems += "id clashes with default template ${t.id}"
            val fpArr = o.getJSONArray("fp")
            val fp = (0 until fpArr.length()).map { fpArr.getInt(it) }
            if (!fps.add(fp.joinToString(","))) problems += "dup composition fingerprint ${t.id}"
            if (!heads.add(t.category + "|" + t.titleAr)) problems += "dup headline ${t.id}"
            perCat.getOrPut(t.category) { ArrayList() }.add(fp)
            if (t.aspectRatio !in setOf("1:1", "4:5", "16:9", "9:16")) problems += "bad aspect ${t.id}"
            if (!hexRe.matches(t.bgStartHex) || !hexRe.matches(t.bgEndHex)) problems += "bad bg ${t.id}"
            if (t.elements.size !in 6..120) problems += "element count ${t.id}=${t.elements.size}"
            val h = 360f / when (t.aspectRatio) { "16:9" -> 16f / 9f; "9:16" -> 9f / 16f; "4:5" -> 0.8f; else -> 1f }
            for (e in t.elements) {
                if (!hexRe.matches(e.colorHex)) problems += "bad color ${t.id}/${e.id}"
                if (e.opacity !in 0f..1f) problems += "bad opacity ${t.id}/${e.id}"
                if (e.type == CanvasElementType.TEXT) {
                    if (e.text.isBlank()) problems += "blank text ${t.id}/${e.id}"
                    if (e.x !in 0f..360f || e.y !in 0f..h) problems += "text outside canvas ${t.id}/${e.id}"
                    if (e.fontSize < 11f) problems += "tiny text ${t.id}/${e.id}"
                }
            }
            if (t.elements.none { it.type == CanvasElementType.TEXT && it.fontSize >= 20f }) problems += "no headline ${t.id}"
        }
        // two templates of one category must differ in at least 2 of: layout, art, palette, text arrangement
        for ((cat, l) in perCat) for (a in l.indices) for (b in a + 1 until l.size) {
            val d = l[a].indices.count { l[a][it] != l[b][it] }
            if (d < 2) problems += "too similar in $cat: #$a vs #$b"
        }
        println("CATALOG_OK templates=${list.size} categories=${perCat.size} problems=${problems.size}")
        assertTrue("catalog problems: ${problems.take(20)}", problems.isEmpty())
    }

    @Test
    fun sample_sheets_render() {
        val out = File("build/screenshots").also { it.mkdirs() }
        val all = CatalogTemplates.parse(json)
        val step = (all.size / 48).coerceAtLeast(1)
        val sample = all.filterIndexed { i, _ -> i % step == 0 }.take(48)
        var page by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val items = sample.drop(page * 16).take(16)
            Column(Modifier.background(Color.White)) {
                items.chunked(4).forEach { row ->
                    Row { row.forEach { t -> Box(Modifier.width(240.dp).padding(4.dp)) { TemplateThumb(template = t, maxHeight = 300.dp) } } }
                }
            }
        }
        for (p in 0 until 3) {
            rule.runOnUiThread { page = p }
            repeat(6) { rule.mainClock.advanceTimeBy(400); ShadowLooper.idleMainLooper(); Thread.sleep(100); ShadowLooper.idleMainLooper() }
            val dv = rule.activity.window.decorView
            val bmp = Bitmap.createBitmap(dv.width.coerceAtLeast(1), dv.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { dv.draw(Canvas(bmp)) }
            File(out, "catalog_sheet_${p + 1}.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        File(out, "catalog_log.txt").writeText("catalog=${all.size} sampled=${sample.size}\n")
        assertEquals(48, sample.size)
    }
}
