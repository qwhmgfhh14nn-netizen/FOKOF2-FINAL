package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.DesignExporter
import com.example.ui.util.VideoComposer
import com.example.ui.util.VideoEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class VSlide(val bmp: Bitmap, val caption: String)

private fun decodeSlide(ctx: android.content.Context, uri: Uri, maxSide: Int = 1280): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2
    val o = BitmapFactory.Options().apply { inSampleSize = sample }
    ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
} catch (e: Exception) { null }

/** Photo slideshow video maker: pick photos, add captions, choose transition and size, export a real MP4. */
@Composable
fun VideoMakerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var slides by remember { mutableStateOf<List<VSlide>>(emptyList()) }
    var secPer by remember { mutableFloatStateOf(3f) }
    var transition by remember { mutableIntStateOf(VideoComposer.FADE) }
    var aspect by remember { mutableIntStateOf(0) }
    var t by remember { mutableFloatStateOf(0f) }
    var voice by remember { mutableStateOf(false) }
    var voiceLang by remember { mutableStateOf("ar") }
    var progress by remember { mutableFloatStateOf(-1f) }
    val sizes = listOf(Triple("9:16 (ريلز/ستوري)", 720, 1280), Triple("1:1 (مربع)", 720, 720), Triple("16:9 (عريض)", 1280, 720))
    val (_, vw, vh) = sizes[aspect]
    val total = (slides.size * secPer).coerceAtLeast(0.1f)

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        scope.launch {
            val loaded = withContext(Dispatchers.IO) { uris.take(20).mapNotNull { u -> decodeSlide(ctx, u)?.let { VSlide(it, "") } } }
            slides = (slides + loaded).take(20)
            if (loaded.isEmpty() && uris.isNotEmpty()) Toast.makeText(ctx, "تعذر فتح الصور", Toast.LENGTH_SHORT).show()
        }
    }

    val preview = remember(slides, aspect, secPer, transition, t) {
        val pw = vw / 3; val ph = vh / 3
        val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        VideoComposer.draw(Canvas(bmp), pw, ph, slides.map { it.bmp }, slides.map { it.caption }, (t * total).coerceAtMost(total - 0.01f), secPer, transition)
        bmp
    }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("صانع الفيديو", style = MaterialTheme.typography.headlineSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("رجوع", color = GoldPrimary) }
        }
        Text("اختار صور، اكتب تعليق لكل صورة، واختار الانتقال والمقاس. بيطلع ملف MP4 حقيقي بدون علامة مائية (يحتاج أندرويد 10 أو أحدث).", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { pick.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (slides.isEmpty()) "اختار الصور (لحد 20)" else "إضافة صور تانية") }
        if (slides.isNotEmpty()) {
            Image(preview.asImageBitmap(), contentDescription = "معاينة", modifier = Modifier.fillMaxWidth().height(260.dp))
            Slider(value = t, onValueChange = { t = it })
            Text("الوقت: " + String.format("%.1f", t * total) + " من " + String.format("%.1f", total) + " ثانية")
            Text("مدة كل صورة: " + String.format("%.1f", secPer) + " ثانية")
            Slider(value = secPer, onValueChange = { secPer = it }, valueRange = 1.5f..6f)
            Text("الانتقال")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val names = listOf("تلاشي", "سحب", "زوم")
                items(3) { i -> FilterChip(selected = transition == i, onClick = { transition = i }, label = { Text(names[i]) }) }
            }
            FilterChip(selected = voice, onClick = { voice = !voice }, label = { Text("تعليق صوتي: اقرأ التعليقات بصوت الموبايل") })
            if (voice) {
                Text("لغة التعليق الصوتي")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val langs = listOf("العربية" to "ar", "English" to "en", "Français" to "fr", "Deutsch" to "de", "Español" to "es", "Türkçe" to "tr")
                    items(langs.size) { i -> FilterChip(selected = voiceLang == langs[i].second, onClick = { voiceLang = langs[i].second }, label = { Text(langs[i].first) }) }
                }
            }
            Text("المقاس")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(sizes.size) { i -> FilterChip(selected = aspect == i, onClick = { aspect = i }, label = { Text(sizes[i].first) }) }
            }
            slides.forEachIndexed { i, s ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = s.caption,
                        onValueChange = { v -> slides = slides.mapIndexed { j, o -> if (j == i) o.copy(caption = v.take(90)) else o } },
                        label = { Text("تعليق صورة " + (i + 1)) }, modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { slides = slides.filterIndexed { j, _ -> j != i } }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(Modifier.height(4.dp))
            if (progress in 0f..0.999f) Text("بيتجهز الفيديو... " + (progress * 100).toInt() + "%")
            Button(
                enabled = progress < 0f || progress >= 1f,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val snap = slides; val sp = secPer; val tr = transition; val w = vw; val h = vh; val wantVoice = voice && slides.any { it.caption.isNotBlank() }
                    progress = 0f
                    scope.launch {
                        val file = File(ctx.cacheDir, "video_" + System.currentTimeMillis() + ".mp4")
                        val frames = VideoComposer.totalFrames(snap.size, sp, 24)
                        val ok = withContext(Dispatchers.Default) {
                            val bmps = snap.map { it.bmp }; val caps = snap.map { it.caption }
                            VideoEncoder.encode(file, w, h, 24, frames, { c, f -> VideoComposer.draw(c, w, h, bmps, caps, f / 24f, sp, tr) }, { p -> progress = p })
                        }
                        var finalFile = file
                        var voiceNote = ""
                        if (ok && wantVoice) {
                            val wavs = com.example.ui.util.VoiceOver.synth(ctx, snap.map { it.caption }, ctx.cacheDir, voiceLang)
                            val pcm = if (wavs == null) null else com.example.ui.util.VoiceOver.buildPcm(wavs, sp)
                            if (pcm == null) voiceNote = " (من غير صوت: مفيش صوت قراءة باللغة دي على الموبايل)"
                            else withContext(Dispatchers.Default) {
                                val aac = File(ctx.cacheDir, "vo.m4a"); val mixed = File(ctx.cacheDir, "mixed.mp4")
                                if (com.example.ui.util.VoiceOver.encodeAac(pcm.first, pcm.second, aac) && com.example.ui.util.VoiceOver.mux(file, aac, mixed)) finalFile = mixed
                                else voiceNote = " (من غير صوت: تعذر دمج الصوت)"
                                aac.delete()
                            }
                        }
                        val uri = if (ok) withContext(Dispatchers.IO) { VideoEncoder.saveToMovies(ctx, finalFile, "FOKOF2_video_" + System.currentTimeMillis()) } else null
                        file.delete(); finalFile.delete()
                        progress = -1f
                        if (uri != null) {
                            Toast.makeText(ctx, "تم حفظ الفيديو في Movies/FOKOF2" + voiceNote, Toast.LENGTH_LONG).show()
                            DesignExporter.share(ctx, uri, "video/mp4")
                        } else Toast.makeText(ctx, "تعذر إنشاء الفيديو على الجهاز ده", Toast.LENGTH_LONG).show()
                    }
                }
            ) { Text("إنشاء الفيديو وحفظه") }
        }
    }
}
