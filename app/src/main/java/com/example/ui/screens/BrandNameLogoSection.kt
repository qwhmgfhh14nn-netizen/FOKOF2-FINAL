package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.GeminiAiService
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.launch

/** Brand name generator + AI logo generator (free Pollinations image URL). */
@Composable
fun BrandNameLogoSection(industry: String, brandName: String, onPickName: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val service = remember { GeminiAiService(context) }
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var logoUrl by remember { mutableStateOf<String?>(null) }
    var logoLoading by remember { mutableStateOf(false) }
    var logoError by remember { mutableStateOf<String?>(null) }
    var logoWait by remember { mutableStateOf(0) }
    var style by remember { mutableStateOf("minimal gold luxury") }
    val styles = listOf("minimal gold luxury" to "فخم ذهبي", "modern geometric" to "عصري هندسي", "playful colorful" to "مرح ملون", "arabic calligraphy" to "خط عربي")

    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("brand_name_logo_section")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("مولّد الأسماء والشعارات", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
            Spacer(Modifier.height(4.dp))
            Text("اقتراحات أسماء بالذكاء الاصطناعي، وشعار جاهز لاسمك.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        names = service.suggestBrandNames(industry.ifBlank { "تجارة" }, brandName)
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("generate_names_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("اقترحلي أسماء", color = androidx.compose.ui.graphics.Color(0xFF101014), fontWeight = FontWeight.Bold)
            }
            names.forEach { n ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(8.dp))
                        .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                        .clickable { onPickName(n) }.padding(12.dp)
                ) { Text(n, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(16.dp))
            Text("ستايل الشعار", fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                styles.forEach { (k, label) ->
                    OutlinedButton(onClick = { style = k }, shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(if (style == k) 2.dp else 1.dp, if (style == k) GoldPrimary else GoldBorder),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)) { Text(label, fontSize = 11.sp) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val name = brandName.ifBlank { "FOKOF2" }
                    val prompt = "vector logo for brand \"$name\", $style, ${industry.ifBlank { "business" }}, flat, centered, white background, professional"
                    val u = "https://image.pollinations.ai/prompt/" + java.net.URLEncoder.encode(prompt, "UTF-8").replace("+", "%20") +
                        "?width=768&height=768&nologo=true&seed=" + (System.currentTimeMillis() % 100000)
                    logoLoading = true; logoError = null; logoWait = 0
                    scope.launch {
                        // Preferred path: the FOKOF2 proxy worker (key server-side, per-device
                        // limits, caching). Falls back to the keyless service on any failure.
                        if (com.example.data.ai.AiImageProxy.isConfigured()) {
                            when (val p = com.example.data.ai.AiImageProxy.generate(context, prompt + com.example.data.ai.StylePrompts.LOGO_CLEANUP)) {
                                is com.example.data.ai.AiImageProxy.Result.Ok -> {
                                    logoUrl = p.fileUri
                                    logoLoading = false; logoWait = 0
                                    return@launch
                                }
                                is com.example.data.ai.AiImageProxy.Result.Fail -> { /* fall back below */ }
                            }
                        }
                        when (val r = com.example.data.ai.FreeImageFetcher.fetch(context, u) { logoWait = it }) {
                            is com.example.data.ai.FreeImageFetcher.Result.Ok -> { logoUrl = r.fileUri }
                            is com.example.data.ai.FreeImageFetcher.Result.Fail -> { logoError = r.reason }
                        }
                        logoLoading = false; logoWait = 0
                    }
                },
                enabled = !logoLoading,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("generate_logo_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                if (logoLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (logoWait > 0) "استنى $logoWait ثانية (الخدمة المجانية)" else "جاري التوليد...", color = androidx.compose.ui.graphics.Color(0xFF101014), fontWeight = FontWeight.Bold)
                } else Text("ولّد شعار", color = androidx.compose.ui.graphics.Color(0xFF101014), fontWeight = FontWeight.Bold)
            }
            logoError?.let { Text("فشل توليد الشعار: $it. جرّب تاني.", color = androidx.compose.ui.graphics.Color(0xFFB3261E), modifier = Modifier.padding(top = 8.dp)) }
            logoUrl?.let { u ->
                AsyncImage(
                    model = u, contentDescription = "logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(260.dp).clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                )
                OutlinedButton(
                    onClick = {
                        val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "شعار ${brandName.ifBlank { "FOKOF2" }} (تم توليده في FOKOF2)") }
                        context.startActivity(Intent.createChooser(i, "مشاركة"))
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("شارك الشعار (واتساب / سوشيال)") }
            }
        }
    }
}
