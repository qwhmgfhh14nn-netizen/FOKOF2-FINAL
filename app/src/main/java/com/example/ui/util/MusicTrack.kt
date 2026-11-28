package com.example.ui.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlin.math.min

/** Background music for the video maker: decodes any audio the phone can read (mp3/m4a/wav/ogg) to mono PCM and mixes it under the voice-over. */
object MusicTrack {

    /** Decode [uri] to 16-bit mono PCM at [targetRate], trimmed to [maxSeconds] with a short fade-out. Null when the file can't be read. */
    fun decodeToPcm(ctx: Context, uri: Uri, maxSeconds: Float, targetRate: Int = 44100): ShortArray? {
        val ex = MediaExtractor()
        return try {
            ex.setDataSource(ctx, uri, null)
            var format: MediaFormat? = null
            for (i in 0 until ex.trackCount) {
                val f = ex.getTrackFormat(i)
                if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) { format = f; ex.selectTrack(i); break }
            }
            if (format == null) return null
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val srcRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()
            val raw = ArrayList<Short>(44100 * 30)
            val info = MediaCodec.BufferInfo()
            val maxSamples = (maxSeconds * srcRate).toInt()
            var inDone = false
            var done = false
            var spins = 0
            while (!done) {
                if (!inDone) {
                    val ii = codec.dequeueInputBuffer(10000)
                    if (ii >= 0) {
                        val buf = codec.getInputBuffer(ii)!!
                        val n = ex.readSampleData(buf, 0)
                        if (n < 0) { codec.queueInputBuffer(ii, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inDone = true }
                        else { codec.queueInputBuffer(ii, 0, n, ex.sampleTime, 0); ex.advance() }
                    }
                }
                val oi = codec.dequeueOutputBuffer(info, 10000)
                if (oi >= 0) {
                    spins = 0
                    val buf = codec.getOutputBuffer(oi)!!
                    buf.position(info.offset); buf.limit(info.offset + info.size)
                    val shorts = info.size / 2
                    var i = 0
                    while (i < shorts) {
                        var sum = 0L
                        for (ch in 0 until channels) sum += if (i + ch < shorts) buf.getShort(i + ch).toInt() else 0
                        raw.add((sum / channels).toInt().coerceIn(-32768, 32767).toShort())
                        i += channels
                    }
                    codec.releaseOutputBuffer(oi, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) done = true
                    if (raw.size >= maxSamples) done = true
                } else if (oi != MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (++spins > 600) done = true
                }
            }
            codec.stop(); codec.release()
            if (raw.isEmpty()) return null
            val trimmed = if (raw.size > maxSamples) raw.subList(0, maxSamples).toShortArray() else raw.toShortArray()
            val out = resample(trimmed, srcRate, targetRate)
            val fade = min((0.6f * targetRate).toInt(), out.size)
            for (i in 0 until fade) {
                val idx = out.size - fade + i
                out[idx] = (out[idx] * (1f - i.toFloat() / fade)).toInt().toShort()
            }
            out
        } catch (e: Exception) { null } finally { runCatching { ex.release() } }
    }

    /** Linear resample between sample rates (good enough for background music). */
    fun resample(src: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        if (fromRate == toRate || src.isEmpty()) return src
        val out = ShortArray((src.size.toLong() * toRate / fromRate).toInt())
        val step = fromRate.toDouble() / toRate
        for (i in out.indices) {
            val pos = i * step
            val i0 = pos.toInt().coerceAtMost(src.size - 1)
            val i1 = min(i0 + 1, src.size - 1)
            val frac = (pos - i0).toFloat()
            out[i] = (src[i0] * (1 - frac) + src[i1] * frac).toInt().toShort()
        }
        return out
    }

    /** Voice at full level over music at 35%, padded to [length] samples, clamped against clipping. */
    fun mix(voice: ShortArray?, music: ShortArray?, length: Int): ShortArray {
        val out = ShortArray(length)
        for (i in 0 until length) {
            var v = 0
            if (voice != null && i < voice.size) v += voice[i]
            if (music != null && i < music.size) v += (music[i] * 0.35f).toInt()
            out[i] = v.coerceIn(-32768, 32767).toShort()
        }
        return out
    }
}
