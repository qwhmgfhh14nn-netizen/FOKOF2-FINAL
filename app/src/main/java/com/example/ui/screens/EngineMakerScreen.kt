package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DesignTemplate
import com.example.data.templates.BookEngine
import com.example.data.templates.CardEngine
import com.example.data.templates.CertEngine
import com.example.data.templates.DesignEngine
import com.example.data.templates.EmailEngine
import com.example.data.templates.InfoEngine
import com.example.data.templates.InviteEngine
import com.example.data.templates.LogoEngine
import com.example.data.templates.MenuEngine
import com.example.data.templates.PresentEngine
import com.example.ui.components.TemplateDesignView
import com.example.ui.theme.GoldPrimary

private class Kind(val name: String, val labels: List<String>, val layouts: Int, val previewDp: Int)

private val kinds = listOf(
    Kind("شهادة تقدير", listOf("عنوان الشهادة", "الاسم", "السبب", "التاريخ", "اسم الموقّع"), 20, 280),
    Kind("كارت شخصي", listOf("الاسم", "المسمى", "الشركة", "الموبايل", "الإيميل", "العنوان"), 20, 220),
    Kind("منيو", listOf("العنوان", "اسم المكان", "رقم الطلب"), 20, 420),
    Kind("دعوة / تهنئة", listOf("السطر الأول", "العنوان", "الأسماء", "الموعد", "المكان", "الختام"), 20, 420),
    Kind("غلاف كتاب", listOf("عنوان الكتاب", "عنوان فرعي", "اسم المؤلف"), 20, 420),
    Kind("لوجو", listOf("اسم العلامة", "الشعار النصي", "حرف الشعار"), 20, 340),
    Kind("إنفوجرافيك", listOf("العنوان", "الجملة التوضيحية"), 20, 420),
    Kind("نشرة إيميل", listOf("العنوان", "الجملة", "نص الزر", "الفوتر"), 10, 280),
    Kind("شريحة عرض", listOf("العنوان", "العنوان الفرعي"), 20, 220)
)

private fun build(k: Int, l: Int, p: Int, v: List<String>): DesignTemplate {
    fun g(i: Int) = v.getOrElse(i) { "" }
    return when (k) {
        0 -> CertEngine.template(l, p, CertEngine.CertData(title = g(0).ifBlank { "شهادة تقدير" }, name = g(1), reason = g(2), date = g(3), signer = g(4)))
        1 -> CardEngine.template(l, p, CardEngine.CardData(name = g(0), job = g(1), company = g(2), phone = g(3), email = g(4), address = g(5)))
        2 -> MenuEngine.template(l, p, MenuEngine.MenuData(title = g(0), shop = g(1), phone = g(2)))
        3 -> InviteEngine.template(l, p, InviteEngine.InviteData(line1 = g(0), title = g(1), names = g(2), whenText = g(3), where = g(4), footer = g(5)))
        4 -> BookEngine.template(l, p, BookEngine.BookData(title = g(0), subtitle = g(1), author = g(2)))
        5 -> LogoEngine.template(l, p, LogoEngine.LogoData(name = g(0), tagline = g(1), letter = g(2)))
        6 -> InfoEngine.template(l, p, InfoEngine.InfoData(title = g(0), subtitle = g(1)))
        7 -> EmailEngine.template(l, p, EmailEngine.EmailData(title = g(0), subtitle = g(1), button = g(2), footer = g(3)))
        else -> PresentEngine.template(l, p, PresentEngine.SlideData(title = g(0), subtitle = g(1)))
    }
}

/** One form for the design categories: pick a kind, type your text, pick a layout and colours, see it live, open it in the Studio. */
@Composable
fun EngineMakerScreen(onOpenInStudio: (DesignTemplate) -> Unit, onBack: () -> Unit) {
    var kind by remember { mutableIntStateOf(0) }
    var layout by remember { mutableIntStateOf(0) }
    var pal by remember { mutableIntStateOf(0) }
    var vals by remember { mutableStateOf(List(6) { "" }) }
    val k = kinds[kind]
    val tpl = remember(kind, layout, pal, vals) { build(kind, layout.coerceAtMost(k.layouts - 1), pal, vals) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("صمم بالنموذج", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = GoldPrimary, modifier = Modifier.weight(1f))
            Text("رجوع", color = GoldPrimary, modifier = Modifier.clickable { onBack() }.padding(8.dp))
        }
        Text("اختار النوع، اكتب كلامك، والتصميم بيتغير قدامك. الخانة الفاضية بتفضل زي المثال.", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            itemsIndexed(kinds) { i, x -> EChip(x.name, i == kind) { kind = i; layout = 0; vals = List(6) { "" } } }
        }
        Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
            TemplateDesignView(tpl, Modifier.fillMaxWidth().height(k.previewDp.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("الشكل", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            itemsIndexed(List(k.layouts) { it }) { i, _ -> EChip("شكل ${i + 1}", i == layout) { layout = i } }
        }
        Text("الألوان", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            itemsIndexed(DesignEngine.palettes) { i, p -> EChip(p.nameAr, i == pal) { pal = i } }
        }
        k.labels.forEachIndexed { i, label ->
            OutlinedTextField(
                value = vals[i], onValueChange = { nv -> vals = vals.toMutableList().also { it[i] = nv } },
                label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), singleLine = true
            )
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onOpenInStudio(tpl) }, modifier = Modifier.fillMaxWidth()) { Text("افتحه في الاستوديو وعدّل عليه") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EChip(label: String, on: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (on) GoldPrimary.copy(alpha = 0.18f) else Color.Transparent,
        border = BorderStroke(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline),
        modifier = Modifier.clickable { onClick() }
    ) { Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurface) }
}
