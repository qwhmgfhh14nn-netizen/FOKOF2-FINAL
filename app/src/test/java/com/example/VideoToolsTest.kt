package com.example

import com.example.ui.util.VideoTools
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoToolsTest {
    @Test fun joined_clip_timestamps_continue_after_the_previous_clip() {
        // second clip starts at 10 s in the output; we keep it from its 2 s mark; a sample at 3.5 s lands at 11.5 s
        assertEquals(11_500_000L, VideoTools.placedTimeUs(10_000_000L, 2_000_000L, 3_500_000L))
        assertEquals(0L, VideoTools.placedTimeUs(0L, 0L, 0L))
    }
}
