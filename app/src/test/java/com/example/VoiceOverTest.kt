package com.example

import com.example.ui.util.VoiceOver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VoiceOverTest {
    private fun wav(sr: Int, seconds: Float, v: Short): File {
        val n = (sr * seconds).toInt()
        val bb = ByteBuffer.allocate(44 + n * 2).order(ByteOrder.LITTLE_ENDIAN)
        bb.putShort(22, 1); bb.putInt(24, sr)
        for (i in 0 until n) bb.putShort(44 + i * 2, v)
        return File.createTempFile("voice", ".wav").also { it.writeBytes(bb.array()); it.deleteOnExit() }
    }

    @Test fun narration_is_placed_at_slide_starts_and_cut_to_slide_length() {
        val r = VoiceOver.buildPcm(listOf(wav(8000, 5f, 1000), null, wav(8000, 1f, 500)), 3f)
        assertNotNull(r)
        val (pcm, sr) = r!!
        assertEquals(8000, sr)
        assertEquals(3 * 3 * 8000, pcm.size)
        assertEquals(1000.toShort(), pcm[0])
        assertEquals(1000.toShort(), pcm[3 * 8000 - 1])   // 5 s narration cut at 3 s
        assertEquals(0.toShort(), pcm[3 * 8000 + 10])     // slide 2 silent
        assertEquals(500.toShort(), pcm[6 * 8000 + 10])   // slide 3 starts with its own voice
        assertEquals(0.toShort(), pcm[7 * 8000 + 100])    // after its 1 s of speech
    }

    @Test fun no_audio_when_nothing_was_spoken() {
        assertEquals(null, VoiceOver.buildPcm(listOf(null, null), 3f))
    }
}
