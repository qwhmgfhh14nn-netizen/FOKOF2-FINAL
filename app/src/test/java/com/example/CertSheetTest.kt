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
import com.example.data.templates.DesignEngine
import com.example.ui.components.TemplateThumb
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Renders engine CV templates (first palette of each layout, plus the 12 layouts in 2 more palettes) as review sheets. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1000dp-h1400dp-xhdpi")
class CertSheetTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cert_sheets() {
        val out = File("build/screenshots").also { it.mkdirs() }
        val all = com.example.data.templates.CertEngine.templates()
        val pick = (0 until 20).flatMap { l -> listOf(0, 4).map { p -> all[l * 10 + p] } }
        var page by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Column(Modifier.background(Color.White)) {
                pick.drop(page * 12).take(12).chunked(3).forEach { row ->
                    Row { row.forEach { t -> Box(Modifier.width(320.dp).padding(4.dp)) { TemplateThumb(template = t, maxHeight = 240.dp) } } }
                }
            }
        }
        for (p in 0 until 4) {
            rule.runOnUiThread { page = p }
            repeat(6) { rule.mainClock.advanceTimeBy(400); ShadowLooper.idleMainLooper(); Thread.sleep(150) }
            val v = rule.activity.window.decorView
            val bmp = Bitmap.createBitmap(v.width.coerceAtLeast(1), v.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { v.draw(Canvas(bmp)) }
            File(out, "engine_cert_${p + 1}.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) }
        }
    }
}
