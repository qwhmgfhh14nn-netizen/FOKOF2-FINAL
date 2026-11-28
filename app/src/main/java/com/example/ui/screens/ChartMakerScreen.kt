package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.ChartMaker
import com.example.ui.util.DesignExporter
import java.io.File

/** Charts maker: enter labels and numbers, get a real bar/pie/line chart image you can drop into a design or share. */
@Composable
fun ChartMakerScreen(onBack: () -> Unit, onAddToDesign: (String) -> Unit) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableIntStateOf(ChartMaker.BARS) }
    var rows by remember { mutableStateOf(listOf("المبيعات" to "120", "الطلبات" to "85", "الزيارات" to "240")) }

    val parsed = rows.mapNotNull { (l, v) -> v.replace(',', '.').toFloatOrNull()?.let { l to it } }
    val preview = remember(title, kind, rows) { ChartMaker.render(kind, title, parsed, 540) }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الرسوم البيانية", style = MaterialTheme.typography.headlineSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("رجوع", color = GoldPrimary) }
        }
        Text("اكتب الأسماء والأرقام وطلّع شارت أعمدة أو دائري أو خطي بألوان التطبيق. ممكن تضيفه لتصميمك أو تحفظه كصورة.", style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(value = title, onValueChange = { title = it.take(40) }, label = { Text("عنوان الشارت") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val kinds = listOf(ChartMaker.BARS to "أعمدة", ChartMaker.PIE to "دائري", ChartMaker.LINE to "خطي")
            items(kinds.size) { i -> FilterChip(selected = kind == kinds[i].first, onClick = { kind = kinds[i].first }, label = { Text(kinds[i].second) }) }
        }

        Image(preview.asImageBitmap(), contentDescription = "معاينة الشارت", modifier = Modifier.fillMaxWidth())

        rows.forEachIndexed { i, (label, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { v -> rows = rows.mapIndexed { j, o -> if (j == i) v.take(14) to o.second else o } },
                    label = { Text("الاسم") }, modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { v -> rows = rows.mapIndexed { j, o -> if (j == i) o.first to v.take(10) else o } },
                    label = { Text("الرقم") }, modifier = Modifier.weight(1f), singleLine = true
                )
                TextButton(onClick = { rows = rows.filterIndexed { j, _ -> j != i } }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            }
        }
        if (rows.size < 8) TextButton(onClick = { rows = rows + ("" to "") }) { Text("+ ضيف خانة", color = GoldPrimary) }

        Button(
            onClick = {
                if (parsed.isEmpty()) { Toast.makeText(ctx, "ضيف رقم واحد على الأقل", Toast.LENGTH_SHORT).show(); return@Button }
                val full = ChartMaker.render(kind, title, parsed, 1080)
                val dir = File(ctx.filesDir, "design_img").apply { mkdirs() }
                val f = File(dir, "chart_" + System.currentTimeMillis() + ".png")
                f.outputStream().use { full.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                onAddToDesign(android.net.Uri.fromFile(f).toString())
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ضيف الشارت للتصميم") }

        OutlinedButton(
            onClick = {
                if (parsed.isEmpty()) { Toast.makeText(ctx, "ضيف رقم واحد على الأقل", Toast.LENGTH_SHORT).show(); return@OutlinedButton }
                val full = ChartMaker.render(kind, title, parsed, 1080)
                val u = DesignExporter.savePng(ctx, full, "FOKOF2_chart_" + System.currentTimeMillis())
                if (u == null) Toast.makeText(ctx, "تعذر حفظ الصورة (يحتاج أندرويد 10 أو أحدث)", Toast.LENGTH_LONG).show()
                else DesignExporter.share(ctx, u, "image/png")
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("حفظ ومشاركة كصورة", color = GoldPrimary) }
        Spacer(Modifier.height(8.dp))
    }
}
