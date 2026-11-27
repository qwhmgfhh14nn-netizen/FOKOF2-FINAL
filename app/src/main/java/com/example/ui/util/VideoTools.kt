package com.example.ui.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

/** A piece of a video to keep: [startUs, endUs) of the clip at [uri]. */
data class VideoClip(val uri: Uri, val durationUs: Long, val startUs: Long = 0L, val endUs: Long = durationUs)

/** Lossless trim and join: samples are copied, never re-encoded, so it is fast and keeps quality. Clips must share the same codec and size to join. */
object VideoTools {
    fun durationUs(ctx: Context, uri: Uri): Long = try {
        val r = MediaMetadataRetriever()
        r.setDataSource(ctx, uri)
        val ms = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        r.release()
        ms * 1000L
    } catch (e: Exception) { 0L }

    /** Pure timing rule used for joins: the sample at [sampleUs] inside a clip lands at [offsetUs] + (sampleUs - start). */
    fun placedTimeUs(offsetUs: Long, startUs: Long, sampleUs: Long): Long = offsetUs + (sampleUs - startUs)

    /** Returns true on success. Fails (false) if clips differ in video format. */
    fun trimAndJoin(ctx: Context, clips: List<VideoClip>, out: File): Boolean {
        if (clips.isEmpty()) return false
        var muxer: MediaMuxer? = null
        return try {
            muxer = MediaMuxer(out.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var vTrack = -1
            var aTrack = -1
            var firstV: MediaFormat? = null
            var started = false
            var offset = 0L
            val buf = ByteBuffer.allocate(2 shl 20)
            val info = MediaCodec.BufferInfo()
            for ((ci, clip) in clips.withIndex()) {
                val ex = MediaExtractor()
                ex.setDataSource(ctx, clip.uri, null)
                val vIdx = (0 until ex.trackCount).firstOrNull { ex.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true }
                    ?: run { ex.release(); return false }
                val aIdx = (0 until ex.trackCount).firstOrNull { ex.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
                val vf = ex.getTrackFormat(vIdx)
                if (ci == 0) {
                    firstV = vf
                    vTrack = muxer.addTrack(vf)
                    if (aIdx != null && clips.size == 1) aTrack = muxer.addTrack(ex.getTrackFormat(aIdx))
                    else if (aIdx != null) aTrack = muxer.addTrack(ex.getTrackFormat(aIdx))
                    muxer.start(); started = true
                } else if (vf.getString(MediaFormat.KEY_MIME) != firstV?.getString(MediaFormat.KEY_MIME) ||
                    vf.getInteger(MediaFormat.KEY_WIDTH) != firstV?.getInteger(MediaFormat.KEY_WIDTH) ||
                    vf.getInteger(MediaFormat.KEY_HEIGHT) != firstV?.getInteger(MediaFormat.KEY_HEIGHT)) {
                    ex.release(); return false
                }
                val tracks = listOfNotNull(vIdx to vTrack, if (aIdx != null && aTrack >= 0) aIdx to aTrack else null)
                for ((src, dst) in tracks) {
                    ex.selectTrack(src)
                    ex.seekTo(clip.startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                    while (true) {
                        buf.clear()
                        val n = ex.readSampleData(buf, 0)
                        if (n < 0) break
                        val t = ex.sampleTime
                        if (t >= clip.endUs) break
                        if (t >= clip.startUs || src == vIdx) {
                            info.set(0, n, placedTimeUs(offset, clip.startUs, t).coerceAtLeast(0L), if (ex.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
                            muxer.writeSampleData(dst, buf, info)
                        }
                        ex.advance()
                    }
                    ex.unselectTrack(src)
                }
                ex.release()
                offset += clip.endUs - clip.startUs
            }
            started
        } catch (e: Exception) { false } finally {
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }
}
