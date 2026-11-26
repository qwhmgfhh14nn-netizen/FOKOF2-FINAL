package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.util.PhotoOps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun loadPhoto(ctx: android.content.Context, uri: Uri, maxSide: Int = 1600): Bitmap? = try {
    val b = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, b) }
    var s = 1
    while (maxOf(b.outWidth, b.outHeight) / s > maxSide * 2) s *= 2
    ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = s }) }
} catch (e: Exception) { null }

/** Photo tools: adjust and rotate, meme text, collage and emoji stickers. Saves a real PNG to Pictures/FOKOF2. */
@Composable
fun PhotoToolsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var photos by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var mode by remember { mutableIntStateOf(0) }
    var bright by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var sat by remember { mutableFloatStateOf(1f) }
    var rot by remember { mutableIntStateOf(0) }
    var mirror by remember { mutableStateOf(false) }
    var top by remember { mutableStateOf("") }
    var bottom by remember { mutableStateOf("") }
    var layout by remember { mutableIntStateOf(0) }
    var emoji by remember { mutableStateOf("⭐") }
    var sx by remember { mutableFloatStateOf(0.5f) }
    var sy by remember { mutableFloatStateOf(0.5f) }
    var ssize by remember { mutableFloatStateOf(0.25f) }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        scope.launch {
            val l = withContext(Dispatchers.IO) { uris.take(4).mapNotNull { loadPhoto(ctx, it) } }
            if (l.isNotEmpty()) photos = l
        }
    }

    fun render(maxSide: Int): Bitmap? {
        val first = photos.firstOrNull() ?: return null
        fun fit(b: Bitmap): Bitmap {
            val k = maxSide.toFloat() / maxOf(b.width, b.height)
            return if (k >= 1f) b else Bitmap.createScaledBitmap(b, (b.width * k).toInt(), (b.height * k).toInt(), true)
        }
        return when (mode) {
            0 -> PhotoOps.adjust(fit(first), bright, contrast, sat, rot, mirror)
            1 -> PhotoOps.meme(fit(first), top, bottom)
            2 -> PhotoOps.collage(photos.map { fit(it) }, layout, minOf(maxSide, 1080))
            else -> PhotoOps.sticker(fit(first), emoji.ifBlank { "⭐" }, sx, sy, ssize)
        }
    }
    val preview = remember(photos, mode, bright, contrast, sat, rot, mirror, top, bottom, layout, emoji, sx, sy, ssize) { render(600) }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("أدوات الصور", style = MaterialTheme.typography.headlineSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("رجوع", color = GoldPrimary) }
        }
        Button(onClick = { pick.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (photos.isEmpty()) "اختار صورة (أو لحد 4 للكولاج)" else "غيّر الصور") }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val names = listOf("تعديل وتدوير", "ميم", "كولاج", "ملصقات")
            items(4) { i -> FilterChip(selected = mode == i, onClick = { mode = i }, label = { Text(names[i]) }) }
        }
        if (preview != null) Image(preview.asImageBitmap(), contentDescription = "معاينة", modifier = Modifier.fillMaxWidth().height(320.dp))
        when (mode) {
            0 -> {
                Text("السطوع"); Slider(value = bright, onValueChange = { bright = it }, valueRange = -0.5f..0.5f)
                Text("التباين"); Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.5f..1.5f)
                Text("التشبع"); Slider(value = sat, onValueChange = { sat = it }, valueRange = 0f..2f)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { rot = (rot + 1) % 4 }) { Text("تدوير 90°") }
                    OutlinedButton(onClick = { mirror = !mirror }) { Text("قلب أفقي") }
                    OutlinedButton(onClick = { bright = 0f; contrast = 1f; sat = 1f; rot = 0; mirror = false }) { Text("إعادة") }
                }
            }
            1 -> {
                OutlinedTextField(value = top, onValueChange = { top = it.take(60) }, label = { Text("الكلام اللي فوق") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bottom, onValueChange = { bottom = it.take(60) }, label = { Text("الكلام اللي تحت") }, modifier = Modifier.fillMaxWidth())
            }
            2 -> LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val names = listOf("جنب بعض", "فوق بعض", "4 مربعات", "كبيرة + 2")
                items(4) { i -> FilterChip(selected = layout == i, onClick = { layout = i }, label = { Text(names[i]) }) }
            }
            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val set = listOf("⭐", "❤️", "🔥", "😂", "👑", "🎉", "✅", "💯", "🌙", "🎁")
                    items(set.size) { i -> FilterChip(selected = emoji == set[i], onClick = { emoji = set[i] }, label = { Text(set[i]) }) }
                }
                Text("المكان أفقي"); Slider(value = sx, onValueChange = { sx = it })
                Text("المكان رأسي"); Slider(value = sy, onValueChange = { sy = it })
                Text("الحجم"); Slider(value = ssize, onValueChange = { ssize = it }, valueRange = 0.08f..0.6f)
            }
        }
        if (photos.isNotEmpty()) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                scope.launch {
                    val bmp = withContext(Dispatchers.Default) { render(1600) }
                    val u = bmp?.let { withContext(Dispatchers.IO) { DesignExporter.savePng(ctx, it, "FOKOF2_photo_" + System.currentTimeMillis()) } }
                    if (u != null) { Toast.makeText(ctx, "تم حفظ الصورة في معرض الصور (مجلد FOKOF2)", Toast.LENGTH_LONG).show(); DesignExporter.share(ctx, u, "image/png") }
                    else Toast.makeText(ctx, "تعذر حفظ الصورة (يحتاج أندرويد 10 أو أحدث)", Toast.LENGTH_LONG).show()
                }
            }) { Text("حفظ ومشاركة") }
        }
    }
}
