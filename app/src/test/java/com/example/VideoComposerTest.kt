package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.example.ui.util.VideoComposer
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
class VideoComposerTest {
    private fun solid(c: Int): Bitmap = Bitmap.createBitmap(40, 60, Bitmap.Config.ARGB_8888).also { Canvas(it).drawColor(c) }

    @Test fun frame_count_matches_duration() {
        assertEquals(144, VideoComposer.totalFrames(2, 3f, 24))
        assertEquals(1, VideoComposer.totalFrames(0, 3f, 24))
    }

    @Test fun frames_show_first_then_second_slide_and_blend_between() {
        val slides = listOf(solid(Color.RED), solid(Color.BLUE))
        fun px(t: Float): Int {
            val b = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            VideoComposer.draw(Canvas(b), 32, 32, slides, listOf("", ""), t, 3f, VideoComposer.FADE)
            return b.getPixel(16, 16)
        }
        val start = px(0.1f)
        val mid = px(2.7f)
        val end = px(5.9f)
        assertTrue("start should be red", Color.red(start) > 200 && Color.blue(start) < 50)
        assertTrue("mid should mix red and blue", Color.red(mid) > 40 && Color.blue(mid) > 40)
        assertTrue("end should be blue", Color.blue(end) > 200 && Color.red(end) < 50)
    }

    @Test fun caption_is_drawn_on_a_gradient_not_a_box() {
        val b = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        VideoComposer.draw(Canvas(b), 200, 200, listOf(solid(Color.WHITE)), listOf("مرحبا"), 0.1f, 3f, VideoComposer.FADE)
        val top = b.getPixel(100, 20)
        val bottom = b.getPixel(5, 195)
        assertTrue("top stays bright", Color.red(top) > 230)
        assertTrue("bottom darkened by scrim", Color.red(bottom) < 140)
    }
}
