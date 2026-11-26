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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.templates.DesignEngine
import com.example.ui.components.TemplateDesignView
import com.example.ui.theme.GoldPrimary
import androidx.compose.foundation.shape.RoundedCornerShape

/** CV maker: the user types their own data, picks a layout and colours, sees the result live, then opens it in the Studio. */
@Composable
fun CvMakerScreen(onOpenInStudio: (DesignTemplate) -> Unit, onBack: () -> Unit) {
    var d by remember { mutableStateOf(DesignEngine.CvData()) }
    var layout by remember { mutableIntStateOf(0) }
    var pal by remember { mutableIntStateOf(0) }
    val tpl = remember(d, layout, pal) { DesignEngine.cvTemplate(layout, pal, d) }

    @Composable
    fun field(label: String, v: String, lines: Int = 1, onChange: (String) -> Unit) {
        OutlinedTextField(
            value = v, onValueChange = onChange, label = { Text(label) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), minLines = lines, maxLines = lines + 1
        )
    }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("اعمل سيرتك الذاتية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = GoldPrimary, modifier = Modifier.weight(1f))
            Text("رجوع", color = GoldPrimary, modifier = Modifier.clickable { onBack() }.padding(8.dp))
        }
        Text("اكتب بياناتك والتصميم بيتغير قدامك. أي خانة تسيبها فاضية بتفضل زي المثال.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
            TemplateDesignView(tpl, Modifier.fillMaxWidth().height(420.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("شكل السيرة", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            itemsIndexed(DesignEngine.cvLayoutNames) { i, n ->
                Chip(n, i == layout) { layout = i }
            }
        }
        Text("الألوان", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            itemsIndexed(DesignEngine.palettes) { i, p ->
                Chip(p.nameAr, i == pal) { pal = i }
            }
        }
        Spacer(Modifier.height(6.dp))
        field("الاسم", d.name) { d = d.copy(name = it) }
        field("المسمى الوظيفي", d.job) { d = d.copy(job = it) }
        field("رقم الموبايل", d.phone) { d = d.copy(phone = it) }
        field("الإيميل", d.email) { d = d.copy(email = it) }
        field("المدينة", d.city) { d = d.copy(city = it) }
        field("نبذة عنك", d.about, 2) { d = d.copy(about = it) }
        field("خبرة 1 (المسمى | الشركة | السنين)", d.job1) { d = d.copy(job1 = it) }
        field("خبرة 2", d.job2) { d = d.copy(job2 = it) }
        field("التعليم (المؤهل | الجامعة | السنة)", d.edu) { d = d.copy(edu = it) }
        field("المهارات (افصل بينها بنقطة)", d.skills) { d = d.copy(skills = it) }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onOpenInStudio(tpl) }, modifier = Modifier.fillMaxWidth()) { Text("افتحها في الاستوديو وعدّل عليها") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Chip(label: String, on: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (on) GoldPrimary.copy(alpha = 0.18f) else Color.Transparent,
        border = BorderStroke(1.dp, if (on) GoldPrimary else MaterialTheme.colorScheme.outline),
        modifier = Modifier.clickable { onClick() }
    ) { Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (on) GoldPrimary else MaterialTheme.colorScheme.onSurface) }
}
