package com.example.ui.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

/** Reads slide captions aloud with the phone's own text-to-speech and mixes the voice into the video. Needs a real device. */
object VoiceOver {
    /** One WAV per text (null when that text is blank or failed). Returns null when the phone has no voice for langCode (BCP-47, e.g. "ar", "en", "fr"). */
    suspend fun synth(ctx: Context, texts: List<String>, dir: File, langCode: String = "ar"): List<File?>? {
        val ready = CompletableDeferred<Int>()
        val engine = TextToSpeech(ctx.applicationContext) { ready.complete(it) }
        try {
            if (withTimeoutOrNull(8000) { ready.await() } != TextToSpeech.SUCCESS) return null
            val candidates = if (langCode == "ar") listOf(Locale("ar", "EG"), Locale("ar"))
                else listOf(Locale.forLanguageTag(langCode), Locale(langCode))
            val loc = candidates.firstOrNull { engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE } ?: return null
            engine.language = loc
            val pending = HashMap<String, CompletableDeferred<Boolean>>()
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { utteranceId?.let { pending[it] }?.complete(true) }
                @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { utteranceId?.let { pending[it] }?.complete(false) }
            })
            val out = ArrayList<File?>()
            texts.forEachIndexed { i, t ->
                if (t.isBlank()) { out.add(null); return@forEachIndexed }
                val f = File(dir, "vo_$i.wav")
                val id = "u$i"
                val d = CompletableDeferred<Boolean>()
                pending[id] = d
                val r = engine.synthesizeToFile(t, Bundle(), f, id)
                val ok = r == TextToSpeech.SUCCESS && withTimeoutOrNull(20000) { d.await() } == true
                out.add(if (ok && f.length() > 44) f else null)
            }
            return out
        } finally {
            engine.shutdown()
        }
    }

    /** Places each slide's narration at the start of its slide, cut to the slide length. Returns mono 16-bit samples and the sample rate. */
    fun buildPcm(wavs: List<File?>, secondsPerSlide: Float): Pair<ShortArray, Int>? {
        val parsed = wavs.map { f ->
            if (f == null) null else try {
                val b = f.readBytes()
                val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
                val ch = bb.getShort(22).toInt().coerceAtLeast(1)
                val sr = bb.getInt(24)
                val n = (b.size - 44) / 2 / ch
                val s = ShortArray(n) { bb.getShort(44 + it * 2 * ch) }
                s to sr
            } catch (e: Exception) { null }
        }
        val sr = parsed.firstOrNull { it != null }?.second ?: return null
        val per = (secondsPerSlide * sr).toInt()
        val out = ShortArray(per * wavs.size)
        parsed.forEachIndexed { i, p ->
            if (p == null || p.second != sr) return@forEachIndexed
            val len = minOf(p.first.size, per)
            System.arraycopy(p.first, 0, out, i * per, len)
        }
        return out to sr
    }

    fun encodeAac(pcm: ShortArray, sampleRate: Int, out: File): Boolean {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        return try {
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, 64000)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            muxer = MediaMuxer(out.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val info = MediaCodec.BufferInfo()
            var track = -1; var started = false; var pos = 0; var inputDone = false; var done = false; var spins = 0
            while (!done && spins < 20000) {
                if (!inputDone) {
                    val ii = codec.dequeueInputBuffer(10_000)
                    if (ii >= 0) {
                        val buf = codec.getInputBuffer(ii)!!
                        buf.clear()
                        val n = minOf(buf.capacity() / 2, pcm.size - pos)
                        if (n <= 0) {
                            codec.queueInputBuffer(ii, 0, 0, pos * 1_000_000L / sampleRate, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            buf.order(ByteOrder.LITTLE_ENDIAN)
                            for (k in 0 until n) buf.putShort(pcm[pos + k])
                            codec.queueInputBuffer(ii, 0, n * 2, pos * 1_000_000L / sampleRate, 0)
                            pos += n
                        }
                    }
                }
                val oi = codec.dequeueOutputBuffer(info, 10_000)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    track = muxer.addTrack(codec.outputFormat); muxer.start(); started = true
                } else if (oi >= 0) {
                    val ob = codec.getOutputBuffer(oi)
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                    if (info.size > 0 && started && ob != null) {
                        ob.position(info.offset); ob.limit(info.offset + info.size)
                        muxer.writeSampleData(track, ob, info)
                    }
                    codec.releaseOutputBuffer(oi, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) done = true
                } else spins++
            }
            started
        } catch (e: Exception) { false } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }

    /** Copies the video track of [video] and the audio track of [audio] into one MP4. */
    fun mux(video: File, audio: File, out: File): Boolean {
        var muxer: MediaMuxer? = null
        val ex = ArrayList<MediaExtractor>()
        return try {
            muxer = MediaMuxer(out.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val tracks = ArrayList<Triple<MediaExtractor, Int, Int>>()
            for (f in listOf(video, audio)) {
                val e = MediaExtractor(); e.setDataSource(f.path); ex.add(e)
                val want = if (f == video) "video/" else "audio/"
                val idx = (0 until e.trackCount).firstOrNull { e.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith(want) == true } ?: return false
                e.selectTrack(idx)
                tracks.add(Triple(e, idx, muxer.addTrack(e.getTrackFormat(idx))))
            }
            muxer.start()
            val buf = ByteBuffer.allocate(1 shl 20)
            val info = MediaCodec.BufferInfo()
            for ((e, _, mt) in tracks) {
                while (true) {
                    buf.clear()
                    val n = e.readSampleData(buf, 0)
                    if (n < 0) break
                    info.set(0, n, e.sampleTime, if (e.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
                    muxer.writeSampleData(mt, buf, info)
                    e.advance()
                }
            }
            true
        } catch (e: Exception) { false } finally {
            ex.forEach { try { it.release() } catch (_: Exception) {} }
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }
}
