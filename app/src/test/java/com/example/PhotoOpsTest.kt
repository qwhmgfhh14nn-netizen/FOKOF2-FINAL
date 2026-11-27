package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.example.ui.util.PhotoOps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PhotoOpsTest {
    private fun solid(w: Int, h: Int, c: Int) = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { Canvas(it).drawColor(c) }

    @Test fun brightness_and_grayscale_change_pixels() {
        val gray = solid(20, 20, Color.rgb(100, 100, 100))
        val lighter = PhotoOps.adjust(gray, 0.3f, 1f, 1f, 0, false).getPixel(5, 5)
        assertTrue("brighter", Color.red(lighter) > 150)
        val red = solid(20, 20, Color.RED)
        val bw = PhotoOps.adjust(red, 0f, 1f, 0f, 0, false).getPixel(5, 5)
        assertTrue("desaturated", Math.abs(Color.red(bw) - Color.green(bw)) < 12 && Math.abs(Color.green(bw) - Color.blue(bw)) < 12)
    }

    @Test fun rotate_swaps_size_and_mirror_flips() {
        val b = solid(40, 20, Color.BLUE).also { it.setPixel(0, 0, Color.RED) }
        val r = PhotoOps.adjust(b, 0f, 1f, 1f, 1, false)
        assertEquals(20, r.width); assertEquals(40, r.height)
        val m = PhotoOps.adjust(b, 0f, 1f, 1f, 0, true)
        assertEquals(Color.RED, m.getPixel(39, 0))
    }

    @Test fun collage_places_each_photo_in_its_cell() {
        val c = PhotoOps.collage(listOf(solid(50, 50, Color.RED), solid(50, 50, Color.BLUE)), 0, 200, 8)
        assertEquals(200, c.width)
        assertEquals(Color.RED, c.getPixel(20, 100))
        assertEquals(Color.BLUE, c.getPixel(180, 100))
    }

    @Test fun meme_draws_text_near_the_edges_only() {
        val src = solid(300, 300, Color.rgb(90, 90, 90))
        val out = PhotoOps.meme(src, "TOP", "BOTTOM")
        var changedTop = 0; var changedMid = 0
        for (y in 0 until 300) for (x in 0 until 300) if (out.getPixel(x, y) != src.getPixel(x, y)) { if (y < 70) changedTop++ else if (y in 110..190) changedMid++ }
        assertTrue("text on top", changedTop > 50)
        assertEquals(0, changedMid)
    }
}
