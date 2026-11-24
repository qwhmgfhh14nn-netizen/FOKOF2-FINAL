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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.example.data.templates.FreeTemplatesData
import com.example.data.templates.ProTemplatesData
import com.example.data.templates.TemplateDesigner
import com.example.ui.components.TemplateThumb
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Renders all 64 default templates (as the app does, enriched designs) into 4 contact sheets and fails if any artwork fails to load. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1000dp-h1400dp-xhdpi")
class TemplateSheetTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun contact_sheets_for_all_64_templates() {
        val out = File("build/screenshots").also { it.mkdirs() }
        val all = (FreeTemplatesData.getTemplates() + ProTemplatesData.getTemplates()).map { TemplateDesigner.enrich(it) }
        // 1) Real Coil loads of all 64 bundled artworks from assets (same request shape the app uses)
        val ctx = rule.activity
        val loader = ctx.imageLoader
        val bad = java.util.Collections.synchronizedList(ArrayList<String>())
        val worker = Thread {
            kotlinx.coroutines.runBlocking {
                all.forEach { t ->
                    val r = loader.execute(
                        ImageRequest.Builder(ctx).data(t.imageUri).size(Size(1024, 1024)).memoryCacheKey(t.imageUri).build()
                    )
                    if (r !is SuccessResult || r.drawable.intrinsicWidth <= 0) bad += t.id
                }
            }
        }
        worker.start()
        var spins = 0
        while (worker.isAlive && spins < 1200) { ShadowLooper.idleMainLooper(); Thread.sleep(50); spins++ }
        File(out, "coil_load_log.txt").writeText("coil_loaded=${all.size - bad.size}/${all.size} failed=$bad\n")
        assertEquals("Coil failed to load art for: $bad", 0, bad.size)
        var page by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val items = all.drop(page * 16).take(16)
            Column(Modifier.background(Color.White)) {
                items.chunked(4).forEach { row ->
                    Row {
                        row.forEach { t ->
                            Box(Modifier.width(240.dp).padding(4.dp)) { TemplateThumb(template = t, maxHeight = 300.dp) }
                        }
                    }
                }
            }
        }
        var failedLabels = 0
        for (p in 0 until 4) {
            rule.runOnUiThread { page = p }
            repeat(30) {
                rule.mainClock.advanceTimeBy(400)
                ShadowLooper.idleMainLooper()
                Thread.sleep(300)
                ShadowLooper.idleMainLooper()
            }
            val dv = rule.activity.window.decorView
            val bmp = Bitmap.createBitmap(dv.width.coerceAtLeast(1), dv.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { dv.draw(Canvas(bmp)) }
            File(out, "sheet_${p + 1}.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            failedLabels += rule.onAllNodesWithText("⚠ تعذر تحميل صورة القالب").fetchSemanticsNodes().size
        }
        File(out, "sheet_log.txt").writeText("templates=${all.size} image_load_failures=$failedLabels\n")
        assertEquals("templates whose art failed to load", 0, failedLabels)
    }
}
