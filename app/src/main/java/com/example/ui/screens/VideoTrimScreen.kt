package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.DesignExporter
import com.example.ui.util.VideoClip
import com.example.ui.util.VideoEncoder
import com.example.ui.util.VideoTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Cut the start and end off video clips and join several into one (no re-encode). */
@Composable
fun VideoTrimScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var clips by remember { mutableStateOf<List<VideoClip>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        scope.launch {
            val added = withContext(Dispatchers.IO) { uris.map { u -> VideoClip(u, VideoTools.durationUs(ctx, u)) }.filter { it.durationUs > 0 } }
            clips = clips + added
            if (added.size < uris.size) Toast.makeText(ctx, "في ملف مش فيديو أو مش بيتفتح", Toast.LENGTH_SHORT).show()
        }
    }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("قص ودمج الفيديو", style = MaterialTheme.typography.headlineSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("رجوع", color = GoldPrimary) }
        }
        Text("اختار فيديو واحد أو أكتر، حدد البداية والنهاية لكل واحد، وهيتدمجوا بالترتيب من غير ما الجودة تقل. الدمج بيشتغل بس لو الفيديوهات بنفس المقاس والنوع (مثلا كلها من نفس الكاميرا).", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { pick.launch("video/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (clips.isEmpty()) "اختار فيديو" else "إضافة فيديو تاني") }
        clips.forEachIndexed { i, c ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("فيديو " + (i + 1) + ": من " + String.format("%.1f", c.startUs / 1e6) + " إلى " + String.format("%.1f", c.endUs / 1e6) + " ثانية (من " + String.format("%.1f", c.durationUs / 1e6) + ")")
                RangeSlider(
                    value = (c.startUs / 1e6f)..(c.endUs / 1e6f),
                    valueRange = 0f..(c.durationUs / 1e6f),
                    onValueChange = { r -> clips = clips.mapIndexed { j, o -> if (j == i) o.copy(startUs = (r.start * 1e6).toLong(), endUs = (r.endInclusive * 1e6).toLong()) else o } }
                )
                TextButton(onClick = { clips = clips.filterIndexed { j, _ -> j != i } }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            }
        }
        if (clips.isNotEmpty()) {
            Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                busy = true
                scope.launch {
                    val tmp = File(ctx.cacheDir, "trim_" + System.currentTimeMillis() + ".mp4")
                    val ok = withContext(Dispatchers.IO) { VideoTools.trimAndJoin(ctx, clips, tmp) }
                    val uri = if (ok) withContext(Dispatchers.IO) { VideoEncoder.saveToMovies(ctx, tmp, "FOKOF2_cut_" + System.currentTimeMillis()) } else null
                    tmp.delete()
                    busy = false
                    if (uri != null) { Toast.makeText(ctx, "تم حفظ الفيديو في Movies/FOKOF2", Toast.LENGTH_LONG).show(); DesignExporter.share(ctx, uri, "video/mp4") }
                    else Toast.makeText(ctx, "تعذر القص أو الدمج (الفيديوهات لازم تكون بنفس المقاس والنوع)", Toast.LENGTH_LONG).show()
                }
            }) { Text(if (busy) "بيتجهز..." else "حفظ الفيديو") }
        }
    }
}
