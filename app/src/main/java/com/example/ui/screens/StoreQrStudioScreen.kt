package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QrFrameStyle
import com.example.data.model.UserWebsite
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.parseColor

@Composable
fun StoreQrStudioScreen(
    website: UserWebsite,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedFrameStyle by remember { mutableStateOf(QrFrameStyle.TABLE_TENT) }
    var qrColorHex by remember { mutableStateOf(website.primaryColorHex) }
    val qrLink = "https://wa.me/" + website.whatsappNumber.filter { it.isDigit() }

    val presetColors = listOf(
        website.primaryColorHex to "لون البراند",
        "#D4AF37" to "ذهبي ملكي",
        "#10B981" to "زمردي",
        "#0284C7" to "أزرق سماوي",
        "#EF4444" to "أحمر فاخر",
        "#1E293B" to "كحلي داكن",
        "#000000" to "أسود نقي"
    )


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("qr_studio_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "استوديو باركود المحل (QR Studio)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = website.siteTitle,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }

            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Printable QR Card Canvas Preview
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Frame Header Text based on selected style
                    when (selectedFrameStyle) {
                        QrFrameStyle.TABLE_TENT -> {
                            Surface(
                                color = parseColor(qrColorHex).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "🍽️ تصميم بطاقة منيو للمطعم",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = parseColor(qrColorHex),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                        QrFrameStyle.STOREFRONT_STICKER -> {
                            Surface(
                                color = parseColor(qrColorHex).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "✨ تابع أحدث العروض والمنتجات حصرياً",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = parseColor(qrColorHex),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                        QrFrameStyle.BUSINESS_CARD -> {
                            Surface(
                                color = parseColor(qrColorHex).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "💼 بطاقة التواصل والخدمات الرسمية",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = parseColor(qrColorHex),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                        QrFrameStyle.MINIMAL -> { /* Clean */ }
                    }

                    Text(
                        text = website.siteTitle,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )

                    Text(
                        text = website.headline,
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    // Procedural Rendered QR Matrix on Canvas
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val qrBitmap = androidx.compose.runtime.remember(qrLink, qrColorHex) {
                            com.example.ui.util.QrCodeMaker.make(qrLink, parseColor(qrColorHex).toArgb(), 720)
                        }
                        androidx.compose.foundation.Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR",
                            modifier = Modifier.fillMaxSize().testTag("qr_image")
                        )
                    }

                    Text(
                        text = "الرمز يفتح محادثة واتساب مع ${website.siteTitle}: $qrLink",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )

                    if (selectedFrameStyle != QrFrameStyle.MINIMAL) {
                        Text(
                            text = "📍 ${website.address} • 📞 ${website.phoneNumber}",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Customization Options: Frame Style
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "شكل الإطار والغرض من الطباعة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoldPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QrFrameStyle.values()) { style ->
                            val isSelected = style == selectedFrameStyle
                            Surface(
                                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldPrimary else GoldBorder.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.clickable { selectedFrameStyle = style }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = style.titleAr,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = style.subtitleAr,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color(0xFF334155) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customization Options: QR Color
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "لون الباركود المميز",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoldPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(presetColors) { (hex, name) ->
                            val isSelected = qrColorHex.equals(hex, ignoreCase = true)
                            val color = parseColor(hex)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { qrColorHex = hex }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) GoldPrimary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(name, fontFamily = CairoFontFamily, fontSize = 9.sp, color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            val ctx = androidx.compose.ui.platform.LocalContext.current
            androidx.compose.material3.Button(
                onClick = {
                    val bmp = com.example.ui.util.QrCodeMaker.make(qrLink, parseColor(qrColorHex).toArgb(), 1024)
                    val ok = com.example.ui.util.QrCodeMaker.saveToGallery(ctx, bmp, "QR-${website.siteSlug}")
                    android.widget.Toast.makeText(ctx, if (ok) "اتحفظ الباركود في معرض الصور" else "الحفظ في المعرض مش متاح على الأندرويد ده، استخدم زر المشاركة", android.widget.Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag("qr_save_btn"),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) { Text("احفظ الباركود في المعرض", color = Color(0xFF101014), fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) }
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "تواصل مع ${website.siteTitle} على واتساب: $qrLink")
                    }
                    ctx.startActivity(android.content.Intent.createChooser(i, "مشاركة"))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) { Text("شارك رابط واتساب", fontFamily = CairoFontFamily) }
        }
    }
}
