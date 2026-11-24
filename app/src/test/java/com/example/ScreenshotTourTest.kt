package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppNavDestination
import com.example.ui.BarayaApp
import com.example.ui.BarayaViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Review tour: saves a PNG of each screen (light and dark). It never fails the build. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTourTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val out = File("build/screenshots").also { it.mkdirs() }
    private val log = File(out, "log.txt")

    private fun note(s: String) { log.appendText(s + "\n") }

    private fun settle() {
        repeat(6) {
            rule.mainClock.advanceTimeBy(400)
            ShadowLooper.idleMainLooper()
            Thread.sleep(300)
            ShadowLooper.idleMainLooper()
        }
    }

    private fun shot(name: String) {
        try {
            settle()
            val dv = rule.activity.window.decorView
            val bmp = Bitmap.createBitmap(dv.width.coerceAtLeast(1), dv.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { dv.draw(Canvas(bmp)) }
            File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            note("OK $name")
        } catch (t: Throwable) {
            note("FAIL $name: ${t.javaClass.simpleName} ${t.message?.take(300)}")
        }
    }

    @Test
    fun tour() {
        try { runTour() } catch (t: Throwable) { note("FATAL ${t.javaClass.simpleName}: ${t.message?.take(500)}") }
    }

    private fun runTour() {
        rule.mainClock.autoAdvance = false
        var vm: BarayaViewModel? = null
        rule.setContent {
            val v: BarayaViewModel = viewModel()
            vm = v
            BarayaApp(v)
        }
        settle()
        val v = vm!!
        var waited = 0
        while (v.publishedTemplates.value.isEmpty() && waited < 40) { Thread.sleep(500); ShadowLooper.idleMainLooper(); rule.mainClock.advanceTimeBy(500); waited++ }
        note("templates after wait: ${v.publishedTemplates.value.size} (waited ${waited * 500}ms)")
        for (dark in listOf(false, true)) {
            val tag = if (dark) "dark" else "light"
            if (v.isDarkTheme.value != dark) { rule.runOnUiThread { v.toggleTheme() } }
            for (d in AppNavDestination.values()) {
                if (d == AppNavDestination.WEBSITE_EDITOR) continue
                try {
                    rule.runOnUiThread { v.setDestination(d) }
                    shot("${tag}_${d.name}")
                } catch (t: Throwable) {
                    note("FAIL nav $d: ${t.message?.take(200)}")
                }
            }
            try {
                rule.runOnUiThread { v.setDestination(AppNavDestination.TEMPLATES) }
                settle()
                val first = v.publishedTemplates.value.firstOrNull()
                note("published templates: ${v.publishedTemplates.value.size}")
                if (first != null) {
                    rule.runOnUiThread { v.setPreviewTemplate(first) }
                    shot("${tag}_template_preview_dialog")
                    rule.runOnUiThread { v.setPreviewTemplate(null) }
                }
            } catch (t: Throwable) {
                note("FAIL preview: ${t.message?.take(200)}")
            }
        }
    }
}
