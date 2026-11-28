package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

/** Encodes drawn frames to an H.264 MP4 with the phone's hardware encoder (no extra libraries). Needs a real device. */
object VideoEncoder {
    fun encode(out: File, w: Int, h: Int, fps: Int, frames: Int, drawFrame: (Canvas, Int) -> Unit, onProgress: (Float) -> Unit): Boolean {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var surface: android.view.Surface? = null
        return try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            surface = codec.createInputSurface()
            codec.start()
            muxer = MediaMuxer(out.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var track = -1
            var started = false
            var written = 0L
            val info = MediaCodec.BufferInfo()
            fun drain(end: Boolean) {
                if (end) codec.signalEndOfInputStream()
                var idleSpins = 0
                while (true) {
                    val idx = codec.dequeueOutputBuffer(info, 10_000)
                    if (idx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!end) return
                        if (++idleSpins > 500) return
                    } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        track = muxer.addTrack(codec.outputFormat); muxer.start(); started = true
                    } else if (idx >= 0) {
                        val buf = codec.getOutputBuffer(idx)
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                        if (info.size > 0 && started && buf != null) {
                            buf.position(info.offset); buf.limit(info.offset + info.size)
                            info.presentationTimeUs = written * 1_000_000L / fps
                            written++
                            muxer.writeSampleData(track, buf, info)
                        }
                        codec.releaseOutputBuffer(idx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                }
            }
            for (f in 0 until frames) {
                val c = surface.lockCanvas(null)
                try { drawFrame(c, f) } finally { surface.unlockCanvasAndPost(c) }
                drain(false)
                onProgress((f + 1f) / frames)
            }
            drain(true)
            started && written > 0
        } catch (e: Exception) {
            false
        } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { surface?.release() } catch (_: Exception) {}
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }

    /** Copies the finished MP4 into Movies/FOKOF2 (Android 10+). */
    fun saveToMovies(ctx: Context, file: File, name: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return try {
            val f = LegacyExport.newFile(ctx, "Movies", "$name.mp4")
            file.inputStream().use { i -> f.outputStream().use { i.copyTo(it) } }
            LegacyExport.uriFor(ctx, f)
        } catch (e: Exception) { null }
        return try {
            val v = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "$name.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/FOKOF2")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, v) ?: return null
            ctx.contentResolver.openOutputStream(uri)?.use { o -> file.inputStream().use { it.copyTo(o) } } ?: return null
            uri
        } catch (e: Exception) { null }
    }
}
