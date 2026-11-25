package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.UserWebsite
import com.example.data.model.SiteExtras
import com.example.data.model.SiteTestimonial
import com.example.data.model.WebsiteBooking
import com.example.data.model.WebsiteItem
import com.example.ui.locale.TranslationStrings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.ErrorRuby
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessEmerald
import com.example.ui.util.parseColor
import java.util.UUID

@Composable
fun WebsiteEditorScreen(
    strings: TranslationStrings,
    website: UserWebsite,
    onSave: (UserWebsite) -> Unit,
    onBack: () -> Unit,
    onOpenQrStudio: (UserWebsite) -> Unit = {},
    onSubmitBooking: (WebsiteBooking) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // State bindings
    var siteTitle by remember(website.id) { mutableStateOf(website.siteTitle) }
    var siteSlug by remember(website.id) { mutableStateOf(website.siteSlug) }
    var headline by remember(website.id) { mutableStateOf(website.headline) }
    var subheadline by remember(website.id) { mutableStateOf(website.subheadline) }
    var aboutText by remember(website.id) { mutableStateOf(website.aboutText) }
    var whatsappNumber by remember(website.id) { mutableStateOf(website.whatsappNumber) }
    var phoneNumber by remember(website.id) { mutableStateOf(website.phoneNumber) }
    var address by remember(website.id) { mutableStateOf(website.address) }
    var workingHours by remember(website.id) { mutableStateOf(website.workingHours) }
    var primaryColorHex by remember(website.id) { mutableStateOf(website.primaryColorHex) }
    var items by remember(website.id) { mutableStateOf(website.items) }
    var extras by remember(website.id) { mutableStateOf(website.extras) }

    // Navigation tabs inside the editor: 0 = Info, 1 = Catalog/Menu, 2 = Live Preview, 3 = Export HTML
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val buildUpdatedWebsite = {
        website.copy(
            siteTitle = siteTitle,
            siteSlug = siteSlug.ifBlank { "my-site" },
            headline = headline,
            subheadline = subheadline,
            aboutText = aboutText,
            whatsappNumber = whatsappNumber,
            phoneNumber = phoneNumber,
            address = address,
            workingHours = workingHours,
            primaryColorHex = primaryColorHex,
            items = items,
            extras = extras,
            updatedAt = System.currentTimeMillis()
        )
    }

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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("website_editor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "محرر المواقع",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = siteTitle.ifBlank { "موقعي الجديد" },
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = GoldPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedIconButton(
                        onClick = { onOpenQrStudio(buildUpdatedWebsite()) },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.testTag("website_editor_qr_btn")
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = "QR", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    }

                    Button(
                        onClick = {
                            val updated = buildUpdatedWebsite()
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.testTag("website_editor_save_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "احفظ",
                            maxLines = 1,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Tabs Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "البيانات الأساسية",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "المنتجات والأسعار (${items.size})",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "معاينة مباشرة",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Text(
                        "تصدير HTML",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                0 -> GeneralInfoTab(
                    siteTitle = siteTitle,
                    onSiteTitleChange = { siteTitle = it },
                    siteSlug = siteSlug,
                    onSiteSlugChange = { siteSlug = it },
                    headline = headline,
                    onHeadlineChange = { headline = it },
                    subheadline = subheadline,
                    onSubheadlineChange = { subheadline = it },
                    aboutText = aboutText,
                    onAboutTextChange = { aboutText = it },
                    whatsappNumber = whatsappNumber,
                    onWhatsappNumberChange = { whatsappNumber = it },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    address = address,
                    onAddressChange = { address = it },
                    workingHours = workingHours,
                    onWorkingHoursChange = { workingHours = it },
                    primaryColorHex = primaryColorHex,
                    onPrimaryColorHexChange = { primaryColorHex = it },
                    extras = extras,
                    onExtrasChange = { extras = it }
                )
                1 -> CatalogItemsTab(
                    items = items,
                    onAddItem = { newItem -> items = items + newItem },
                    onDeleteItem = { id -> items = items.filter { it.id != id } },
                    primaryColorHex = primaryColorHex
                )
                2 -> LivePreviewTab(
                    website = buildUpdatedWebsite(),
                    onSubmitBooking = onSubmitBooking
                )
                3 -> ExportHtmlTab(
                    website = buildUpdatedWebsite(),
                    context = context
                )
            }
        }
    }
}

// ---------------- TAB 0: GENERAL INFO ----------------
@Composable
private fun GeneralInfoTab(
    siteTitle: String,
    onSiteTitleChange: (String) -> Unit,
    siteSlug: String,
    onSiteSlugChange: (String) -> Unit,
    headline: String,
    onHeadlineChange: (String) -> Unit,
    subheadline: String,
    onSubheadlineChange: (String) -> Unit,
    aboutText: String,
    onAboutTextChange: (String) -> Unit,
    whatsappNumber: String,
    onWhatsappNumberChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    workingHours: String,
    onWorkingHoursChange: (String) -> Unit,
    primaryColorHex: String,
    onPrimaryColorHexChange: (String) -> Unit,
    extras: SiteExtras = SiteExtras(),
    onExtrasChange: (SiteExtras) -> Unit = {}
) {
    val presetColors = listOf(
        "#D4AF37" to "ذهبي ملكي",
        "#10B981" to "أخضر طبي",
        "#EC4899" to "وردي أنيق",
        "#0284C7" to "أزرق سماوي",
        "#F59E0B" to "عنبر دافئ",
        "#DC2626" to "أحمر فاخر",
        "#6366F1" to "بنفسجي رقمي"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "اسم النشاط وهويته",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldPrimary
                    )

                    OutlinedTextField(
                        value = siteTitle,
                        onValueChange = onSiteTitleChange,
                        label = { Text("اسم النشاط / المحل", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = siteSlug,
                        onValueChange = onSiteSlugChange,
                        label = { Text("رابط الموقع الفرعي (Slug)", fontFamily = CairoFontFamily) },
                        supportingText = { Text("الرابط العام هيظهر بعد ما تربط دومين واستضافة فعلية", color = GoldLight) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = headline,
                        onValueChange = onHeadlineChange,
                        label = { Text("العنوان الرئيسي (Hero Headline)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = subheadline,
                        onValueChange = onSubheadlineChange,
                        label = { Text("العنوان الفرعي والوصف", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = aboutText,
                        onValueChange = onAboutTextChange,
                        label = { Text("نبذة عن المحل (من نحن)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Color Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لون الموقع الأساسي",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetColors) { (hex, name) ->
                            val isSelected = primaryColorHex.equals(hex, ignoreCase = true)
                            val color = parseColor(hex)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPrimaryColorHexChange(hex) }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontFamily = CairoFontFamily, fontSize = 10.sp, color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Contact & Hours
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "بيانات الاتصال والعنوان",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldPrimary
                    )

                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = onWhatsappNumberChange,
                        label = { Text("رقم واتساب للطلبات (مع كود الدولة، مثلًا: 2010xxxx)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = { Text("رقم التليفون للمكالمات", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = onAddressChange,
                        label = { Text("العنوان بالتفصيل / الفرع", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = onWorkingHoursChange,
                        label = { Text("مواعيد الشغل (مثلًا: كل يوم من 10 ص لـ 12 م)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        item { SiteExtrasEditor(extras = extras, onChange = onExtrasChange) }
    }
}

private fun saveSiteGalleryPhoto(context: Context, uri: android.net.Uri): String? {
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }
        var sample = 1
        while (opts.outWidth / sample > 1600) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            android.graphics.BitmapFactory.decodeStream(it, null, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = 800f / maxOf(bmp.width, bmp.height).toFloat()
        val out = if (scale < 1f) android.graphics.Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true) else bmp
        val dir = java.io.File(context.filesDir, "site_gallery").apply { mkdirs() }
        val f = java.io.File(dir, java.util.UUID.randomUUID().toString() + ".jpg")
        f.outputStream().use { out.compress(android.graphics.Bitmap.CompressFormat.JPEG, 78, it) }
        f.absolutePath
    } catch (e: Exception) { null }
}

@Composable
private fun SiteExtrasEditor(extras: SiteExtras, onChange: (SiteExtras) -> Unit) {
    val context = LocalContext.current
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = saveSiteGalleryPhoto(context, uri)
            if (path != null) onChange(extras.copy(gallery = (extras.gallery + path).take(8)))
        }
    }
    var tName by remember { mutableStateOf("") }
    var tText by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("آراء العملاء", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            extras.testimonials.forEachIndexed { i, tm ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("★ " + tm.name + ": " + tm.text, modifier = Modifier.weight(1f), fontFamily = CairoFontFamily)
                    IconButton(onClick = { onChange(extras.copy(testimonials = extras.testimonials.filterIndexed { j, _ -> j != i })) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                    }
                }
            }
            OutlinedTextField(value = tName, onValueChange = { tName = it }, label = { Text("اسم العميل", fontFamily = CairoFontFamily) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = tText, onValueChange = { tText = it }, label = { Text("رأي العميل", fontFamily = CairoFontFamily) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Button(onClick = {
                if (tName.isNotBlank() && tText.isNotBlank()) {
                    onChange(extras.copy(testimonials = extras.testimonials + SiteTestimonial(tName.trim(), tText.trim(), 5)))
                    tName = ""; tText = ""
                }
            }) { Text("إضافة رأي", fontFamily = CairoFontFamily) }

            Text("معرض الصور (حتى 8 صور)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            Text("عدد الصور: " + extras.gallery.size, fontFamily = CairoFontFamily)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    picker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("إضافة صورة من المعرض", fontFamily = CairoFontFamily) }
                if (extras.gallery.isNotEmpty()) {
                    OutlinedButton(onClick = { onChange(extras.copy(gallery = extras.gallery.dropLast(1))) }) { Text("حذف آخر صورة", fontFamily = CairoFontFamily) }
                }
            }

            Text("روابط السوشيال ميديا", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = extras.facebook, onValueChange = { onChange(extras.copy(facebook = it)) }, label = { Text("فيسبوك (رابط أو اسم الصفحة)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = extras.instagram, onValueChange = { onChange(extras.copy(instagram = it)) }, label = { Text("إنستجرام (رابط أو @اسم)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = extras.tiktok, onValueChange = { onChange(extras.copy(tiktok = it)) }, label = { Text("تيك توك (رابط أو @اسم)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = extras.youtube, onValueChange = { onChange(extras.copy(youtube = it)) }, label = { Text("يوتيوب (رابط القناة)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = extras.telegram, onValueChange = { onChange(extras.copy(telegram = it)) }, label = { Text("تيليجرام (رابط أو @اسم)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = extras.website, onValueChange = { onChange(extras.copy(website = it)) }, label = { Text("موقع آخر (رابط)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        }
    }
}

// ---------------- TAB 1: CATALOG ITEMS ----------------
@Composable
private fun CatalogItemsTab(
    items: List<WebsiteItem>,
    onAddItem: (WebsiteItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    primaryColorHex: String
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "المنتجات والخدمات",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "إجمالي العناصر المضافة: ${items.size}",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = GoldLight
                )
            }

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ضيف منتج", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GoldPrimary.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("مفيش منتجات أو خدمات لسه", fontFamily = CairoFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ضيف أول منتج دلوقتي", fontFamily = CairoFontFamily, color = GoldPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.nameAr,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!item.badgeAr.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = parseColor(primaryColorHex).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.badgeAr,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                color = parseColor(primaryColorHex),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (item.descriptionAr.isNotBlank()) {
                                    Text(
                                        text = item.descriptionAr,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.priceEgp} ج.م",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GoldPrimary
                                )
                            }

                            IconButton(onClick = { onDeleteItem(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRuby)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showDialog) {
        var nameAr by remember { mutableStateOf("") }
        var priceEgpStr by remember { mutableStateOf("") }
        var descriptionAr by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("عام") }
        var badgeAr by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ضيف منتج / خدمة جديدة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldPrimary
                    )

                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text("اسم المنتج / الخدمة بالعربي", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = priceEgpStr,
                        onValueChange = { priceEgpStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("السعر بالجنيه (EGP)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = descriptionAr,
                        onValueChange = { descriptionAr = it },
                        label = { Text("وصف مختصر", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف (مثلًا: برجر، وجبات، عطور، كشف)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = badgeAr,
                        onValueChange = { badgeAr = it },
                        label = { Text("شارة (مثلًا: الأكتر طلبًا، عرض خاص)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showDialog = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء", fontFamily = CairoFontFamily)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nameAr.isNotBlank()) {
                                    val price = priceEgpStr.toIntOrNull() ?: 50
                                    val newItem = WebsiteItem(
                                        id = UUID.randomUUID().toString(),
                                        nameAr = nameAr,
                                        priceEgp = price,
                                        descriptionAr = descriptionAr,
                                        category = category.ifBlank { "عام" },
                                        badgeAr = badgeAr.ifBlank { null },
                                        isAvailable = true
                                    )
                                    onAddItem(newItem)
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ضيف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 2: LIVE PREVIEW ----------------
@Composable
private fun LivePreviewTab(
    website: UserWebsite,
    onSubmitBooking: (WebsiteBooking) -> Unit
) {
    val context = LocalContext.current
    val brandColor = parseColor(website.primaryColorHex)
    var selectedCategory by remember { mutableStateOf("الكل") }

    // Mini Cart State
    var cart by remember { mutableStateOf<Map<String, CartItem>>(emptyMap()) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }

    val categories = remember(website.items) {
        listOf("الكل") + website.items.map { it.category }.distinct()
    }

    val filteredItems = remember(selectedCategory, website.items) {
        if (selectedCategory == "الكل") website.items
        else website.items.filter { it.category == selectedCategory }
    }

    val cartTotalCount = cart.values.sumOf { it.quantity }
    val cartTotalEgp = cart.values.sumOf { it.item.priceEgp * it.quantity }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            // Mobile Browser Chrome simulation
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = GoldLight, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "معاينة على الجهاز • الموقع لسه متنشرش",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = CinzelFontFamily
                            )
                        }
                    }

                    Surface(
                        color = SuccessEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "معاينة بس",
                            color = SuccessEmerald,
                            fontSize = 10.sp,
                            fontFamily = CairoFontFamily,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Website Canvas Preview
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = if (cartTotalCount > 0) 70.dp else 0.dp)
            ) {
                // Header & Hero Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(brandColor.copy(alpha = 0.25f), Color(0xFF0F172A))
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Brand Badge
                            Surface(
                                color = brandColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "مرحباً بكم في",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = brandColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = website.siteTitle,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = website.headline,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = brandColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = website.subheadline,
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons: WhatsApp & Phone & Booking
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (website.featureToggles.featureWhatsappOrders) {
                                        Button(
                                            onClick = {
                                                val uri = android.net.Uri.parse("https://wa.me/${website.whatsappNumber}?text=${java.net.URLEncoder.encode("مرحباً أود الاستفسار والطلب من متجركم: ${website.siteTitle}", "UTF-8")}")
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "واتساب مباشر",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (website.featureToggles.featureDirectCalling) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${website.phoneNumber}"))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, brandColor),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, tint = brandColor, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "اتصال مباشر",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 12.sp,
                                                color = brandColor
                                            )
                                        }
                                    }
                                }

                                if (website.featureToggles.featureBookingSystem) {
                                    Button(
                                        onClick = { showBookingDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "حجز موعد / كشف أونلاين",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // About Us Section
                if (website.aboutText.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "من نحن",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = brandColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = website.aboutText,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Catalog Section Header
                if (website.featureToggles.featureProductCatalog && website.items.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = "قائمة المنتجات والعروض",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Category filter chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    val isSelected = cat == selectedCategory
                                    Surface(
                                        color = if (isSelected) brandColor else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.clickable { selectedCategory = cat }
                                    ) {
                                        Text(
                                            text = cat,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Catalog Items List
                    items(filteredItems) { item ->
                        val itemInCart = cart[item.id]

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.nameAr,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        if (!item.badgeAr.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = brandColor.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.badgeAr,
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 9.sp,
                                                    color = brandColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (item.descriptionAr.isNotBlank()) {
                                        Text(
                                            text = item.descriptionAr,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.priceEgp} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = brandColor
                                    )
                                }

                                // Cart action controls
                                if (itemInCart != null && itemInCart.quantity > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val current = cart.toMutableMap()
                                                if (itemInCart.quantity > 1) {
                                                    current[item.id] = itemInCart.copy(quantity = itemInCart.quantity - 1)
                                                } else {
                                                    current.remove(item.id)
                                                }
                                                cart = current
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = brandColor, modifier = Modifier.size(14.dp))
                                        }
                                        Text(
                                            text = "${itemInCart.quantity}",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                val current = cart.toMutableMap()
                                                current[item.id] = itemInCart.copy(quantity = itemInCart.quantity + 1)
                                                cart = current
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = brandColor, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val current = cart.toMutableMap()
                                            current[item.id] = CartItem(item = item, quantity = 1)
                                            cart = current
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("أضف للسلة 🛒", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // Info Card: Address & Working Hours
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = brandColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(website.address, fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = brandColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(website.workingHours, fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }

                // Footer
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تصميم وتطوير عبر منصة FOKOF2 الذكية © 2026",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Floating Mini Cart Bottom Sheet Bar
        if (cartTotalCount > 0) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.5f)),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = brandColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "السلة: $cartTotalCount عناصر",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "الإجمالي: $cartTotalEgp ج.م",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = brandColor
                        )
                    }

                    Button(
                        onClick = { showCartDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "إتمام الطلب عبر واتساب",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Mini Cart Order Checkout Dialog
    if (showCartDialog) {
        var customerNotes by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showCartDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فاتورة سلة المشتريات",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GoldPrimary
                        )
                        IconButton(onClick = { showCartDialog = false }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = ErrorRuby)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cart.values.toList()) { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cartItem.item.nameAr, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("${cartItem.quantity} × ${cartItem.item.priceEgp} = ${cartItem.quantity * cartItem.item.priceEgp} ج.م", fontFamily = CairoFontFamily, fontSize = 11.sp, color = brandColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val current = cart.toMutableMap()
                                            if (cartItem.quantity > 1) {
                                                current[cartItem.item.id] = cartItem.copy(quantity = cartItem.quantity - 1)
                                            } else {
                                                current.remove(cartItem.item.id)
                                            }
                                            cart = current
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                    Text("${cartItem.quantity}", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    IconButton(
                                        onClick = {
                                            val current = cart.toMutableMap()
                                            current[cartItem.item.id] = cartItem.copy(quantity = cartItem.quantity + 1)
                                            cart = current
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { customerNotes = it },
                        label = { Text("ملاحظات التوصيل أو تفاصيل العنوان", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الإجمالي الكلي المطلوب:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("$cartTotalEgp ج.م", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = brandColor)
                        }
                    }

                    Button(
                        onClick = {
                            val itemsList = cart.values.toList()
                            val invoiceBuilder = StringBuilder()
                            invoiceBuilder.appendLine("🛍️ *طلب شراء جديد من موقع: ${website.siteTitle}*")
                            invoiceBuilder.appendLine("━━━━━━━━━━━━━━━━━━━")
                            itemsList.forEachIndexed { index, cartItem ->
                                val subtotal = cartItem.item.priceEgp * cartItem.quantity
                                invoiceBuilder.appendLine("${index + 1}. *${cartItem.item.nameAr}* (الكمية: ${cartItem.quantity}) = ${subtotal} ج.م")
                            }
                            invoiceBuilder.appendLine("━━━━━━━━━━━━━━━━━━━")
                            invoiceBuilder.appendLine("💰 *الإجمالي النهائي: ${cartTotalEgp} ج.م*")
                            if (customerNotes.isNotBlank()) {
                                invoiceBuilder.appendLine("📝 ملاحظات: $customerNotes")
                            }
                            invoiceBuilder.appendLine("⏰ التاريخ: ${java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}")

                            val uri = android.net.Uri.parse("https://wa.me/${website.whatsappNumber}?text=${java.net.URLEncoder.encode(invoiceBuilder.toString(), "UTF-8")}")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                            showCartDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال الطلب إلى واتساب المحل", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Appointment Booking Dialog
    if (showBookingDialog) {
        var custName by remember { mutableStateOf("") }
        var custPhone by remember { mutableStateOf("") }
        var selectedService by remember { mutableStateOf(website.items.firstOrNull()?.nameAr ?: "خدمة عامة") }
        var bookDate by remember { mutableStateOf("غداً") }
        var bookTime by remember { mutableStateOf("06:00 م") }
        var bookingNotes by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showBookingDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📅 حجز موعد / استشارة أونلاين",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldPrimary
                    )

                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("الاسم بالكامل", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        label = { Text("رقم الهاتف للتأكيد", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = selectedService,
                        onValueChange = { selectedService = it },
                        label = { Text("الخدمة أو الكشف المطلوب", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bookDate,
                            onValueChange = { bookDate = it },
                            label = { Text("تاريخ الموعد", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = bookTime,
                            onValueChange = { bookTime = it },
                            label = { Text("الوقت المفضل", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = bookingNotes,
                        onValueChange = { bookingNotes = it },
                        label = { Text("ملاحظات إضافية", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showBookingDialog = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء", fontFamily = CairoFontFamily)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (custName.isNotBlank() && custPhone.isNotBlank()) {
                                    val booking = WebsiteBooking(
                                        siteId = website.id,
                                        customerName = custName,
                                        customerPhone = custPhone,
                                        serviceName = selectedService,
                                        bookingDate = bookDate,
                                        bookingTime = bookTime,
                                        notes = bookingNotes,
                                        status = "PENDING"
                                    )
                                    onSubmitBooking(booking)

                                    val bookingMsg = """
                                        📅 *طلب حجز موعد جديد من موقع: ${website.siteTitle}*
                                        ━━━━━━━━━━━━━━━━━━━
                                        👤 *العميل:* $custName
                                        📞 *الهاتف:* $custPhone
                                        🩺 *الخدمة المطلوبة:* $selectedService
                                        🗓️ *التاريخ المقترح:* $bookDate
                                        ⏰ *الوقت:* $bookTime
                                        📝 *الملاحظات:* $bookingNotes
                                        ━━━━━━━━━━━━━━━━━━━
                                    """.trimIndent()

                                    val uri = android.net.Uri.parse("https://wa.me/${website.whatsappNumber}?text=${java.net.URLEncoder.encode(bookingMsg, "UTF-8")}")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                    showBookingDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تأكيد الحجز عبر واتساب", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 3: EXPORT HTML ----------------
@Composable
private fun ExportHtmlTab(
    website: UserWebsite,
    context: Context
) {
    var isCopied by remember { mutableStateOf(false) }
    var publishedUrl by remember(website.id) { mutableStateOf(com.example.data.SitePublisher.savedUrl(context, website.id)) }
    var publishing by remember { mutableStateOf(false) }
    var publishError by remember { mutableStateOf<String?>(null) }
    val publishScope = androidx.compose.runtime.rememberCoroutineScope()

    val generatedHtml = remember(website) {
        generateStandaloneHtml(website, context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "كود الموقع المستقل (HTML5 & CSS3)",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "انسخ كود الموقع وانشره على أي استضافة زي Vercel أو Netlify أو GitHub Pages، أو ارفعه على السيرفر بتاعك.",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Website HTML", generatedHtml)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "تم نسخ الكود!" else "انسخ الكود كله",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = {
                            if (!publishing) {
                                publishing = true; publishError = null
                                val htmlNow = generatedHtml
                                publishScope.launch {
                                    val r = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        com.example.data.SitePublisher.publish(context, website.id, website.siteSlug, website.siteTitle, htmlNow)
                                    }
                                    publishing = false
                                    if (r.ok) publishedUrl = r.url else publishError = "ما قدرناش ننشر دلوقتي، تأكد من النت وحاول تاني"
                                }
                            }
                        },
                        enabled = !publishing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (publishing) "جاري النشر..." else "انشر واحصل على رابط",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                publishError?.let {
                    Text(text = it, fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color(0xFFD32F2F))
                }
                publishedUrl?.let { link ->
                    Text(text = "رابط موقعك:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = link, fontSize = 13.sp, color = GoldPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Site link", link))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("انسخ الرابط", fontFamily = CairoFontFamily) }
                        Button(
                            onClick = {
                                val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, link)
                                }
                                context.startActivity(android.content.Intent.createChooser(i, "شارك رابط الموقع").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("شارك", fontFamily = CairoFontFamily) }
                    }
                }
            }
        }

        // Code preview box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                item {
                    Text(
                        text = generatedHtml,
                        fontFamily = CinzelFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF93C5FD),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

private fun siteEsc(t: String?): String =
    (t ?: "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

private fun siteHeroDataUri(context: Context?, type: String): String {
    if (context == null) return ""
    return try {
        val bytes = context.assets.open("sites/" + type.uppercase() + ".jpg").use { it.readBytes() }
        "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        ""
    }
}


private val SITE_FEATURES: Map<String, List<Triple<String, String, String>>> = mapOf(
    "RESTAURANT" to listOf(Triple("🔥", "مكونات طازجة", "نختار الخامات يومياً ونحضّر كل طلب على الفور"), Triple("🚚", "توصيل سريع", "طلبك يوصلك ساخن في أسرع وقت"), Triple("⭐", "طعم ثابت", "نفس الجودة في كل مرة تطلب فيها")),
    "PHARMACY" to listOf(Triple("💊", "أدوية أصلية", "كل المنتجات من مصادر موثوقة ومعتمدة"), Triple("🕒", "خدمة سريعة", "ابعت الروشتة واستلم طلبك بسرعة"), Triple("👨‍⚕️", "استشارة صيدلي", "فريق جاهز للإجابة عن استفساراتك")),
    "FASHION" to listOf(Triple("👗", "تصميمات عصرية", "تشكيلة تواكب أحدث صيحات الموضة"), Triple("🧵", "خامات مميزة", "جودة تفصيل وخامات تدوم"), Triple("🎁", "عروض موسمية", "خصومات وعروض على أفضل القطع")),
    "CLINIC" to listOf(Triple("🩺", "أطباء متخصصون", "فريق طبي بخبرة وسمعة طيبة"), Triple("📅", "حجز سهل", "احجز موعدك بسهولة عبر واتساب"), Triple("🧼", "تعقيم كامل", "معايير نظافة وتعقيم عالية")),
    "REAL_ESTATE" to listOf(Triple("🏠", "وحدات مميزة", "عقارات مختارة في أفضل المواقع"), Triple("📄", "عقود موثقة", "إجراءات واضحة وقانونية بالكامل"), Triple("🤝", "دعم كامل", "نرافقك من المعاينة حتى الاستلام")),
    "SALON" to listOf(Triple("💇", "خبراء تجميل", "فريق محترف بأحدث التقنيات"), Triple("✨", "منتجات عالمية", "نستخدم خامات آمنة وعالية الجودة"), Triple("📅", "حجز مسبق", "احجز موعدك وتجنّب الانتظار")),
    "MOTORS" to listOf(Triple("🚗", "سيارات مفحوصة", "فحص شامل لكل سيارة قبل العرض"), Triple("🔧", "صيانة وضمان", "خدمة ما بعد البيع وضمان معتمد"), Triple("💳", "تقسيط مرن", "حلول تمويل تناسب ميزانيتك")),
    "GROCERY" to listOf(Triple("🥬", "طازج يومياً", "خضار وفاكهة وبقالة بأعلى جودة"), Triple("🛵", "توصيل للمنزل", "اطلب وهنوصلك في أسرع وقت"), Triple("💰", "أسعار مناسبة", "عروض مستمرة على الأساسيات")),
    "AGENCY" to listOf(Triple("🎯", "استراتيجية واضحة", "خطط تسويق مبنية على أهداف عملك"), Triple("🎨", "إبداع وتصميم", "هوية وإعلانات تلفت الانتباه"), Triple("📈", "نتائج قابلة للقياس", "تقارير واضحة لأداء حملاتك")),
    "ACADEMY" to listOf(Triple("🎓", "مدرّبون محترفون", "خبرة حقيقية في المجال"), Triple("📚", "مناهج حديثة", "محتوى عملي يواكب سوق العمل"), Triple("🏅", "شهادات معتمدة", "شهادة إتمام بعد كل دورة")),
    "GYM" to listOf(Triple("🏋️", "أجهزة حديثة", "معدات متكاملة لكل أنواع التمرين"), Triple("🥇", "مدربون معتمدون", "برامج مخصصة لهدفك"), Triple("🕒", "مواعيد مرنة", "نفتح لك طوال اليوم")),
    "ELECTRONICS" to listOf(Triple("📱", "أجهزة أصلية", "منتجات بضمان رسمي"), Triple("🔌", "إكسسوارات متنوعة", "كل ما يخص جهازك في مكان واحد"), Triple("🛠️", "صيانة وفحص", "دعم فني سريع بعد الشراء")),
    "PHOTO" to listOf(Triple("📸", "تصوير احترافي", "معدات وإضاءة بمستوى استوديوهات"), Triple("🎞️", "تعديل وإخراج", "صور نهائية بجودة عالية"), Triple("💍", "كل المناسبات", "أفراح وبورتريه ومنتجات")),
    "TRAVEL" to listOf(Triple("✈️", "رحلات منظمة", "برامج سياحية داخلية وخارجية"), Triple("🏨", "إقامة مريحة", "فنادق مختارة بأسعار مناسبة"), Triple("🧾", "حجز بدون تعقيد", "نتولى التذاكر والإجراءات عنك")),
    "LAWYER" to listOf(Triple("⚖️", "خبرة قانونية", "استشارات وقضايا بمعرفة عميقة"), Triple("🔒", "سرية تامة", "نحافظ على خصوصية موكلينا"), Triple("📞", "متابعة مستمرة", "نبقيك على اطلاع بكل خطوة")),
    "CAFE" to listOf(Triple("☕", "قهوة مختصة", "حبوب محمصة طازجة ومحضّرة بإتقان"), Triple("💻", "مكان هادئ", "مساحة مريحة للعمل واللمّة"), Triple("🍰", "حلويات طازجة", "تشكيلة يومية تناسب قهوتك")),
    "HOTEL" to listOf(Triple("🛏️", "غرف مريحة", "نظافة وراحة وخدمة على مدار الساعة"), Triple("🍳", "إفطار مشمول", "وجبة إفطار متنوعة كل صباح"), Triple("📍", "موقع مميز", "قريب من أهم الأماكن والخدمات")),
    "BAKERY" to listOf(Triple("🥐", "مخبوزات طازجة", "نخبز يومياً بأجود المكونات"), Triple("🎂", "تورت حسب الطلب", "نصمم لك تورتة مناسبتك"), Triple("🚚", "توصيل للمنزل", "طلبك يوصل طازج"))
)

internal fun generateStandaloneHtml(site: UserWebsite, context: Context? = null): String {
    val primary = if (Regex("^#[0-9A-Fa-f]{6}$").matches(site.primaryColorHex)) site.primaryColorHex else "#D4AF37"
    val hero = siteHeroDataUri(context, site.businessType)
    val heroCss = if (hero.isNotEmpty()) "url('" + hero + "')" else "linear-gradient(135deg, " + primary + ", #1f2937)"
    val t = site.featureToggles
    val waNum = site.whatsappNumber.filter { it.isDigit() }
    val cats = site.items.map { it.category }.distinct()
    val chips = if (t.featureProductCatalog && cats.size > 1) {
        "<button class=\"chip on\" data-cat=\"__all\">الكل</button>" + cats.joinToString("") { "<button class=\"chip\" data-cat=\"" + siteEsc(it) + "\">" + siteEsc(it) + "</button>" }
    } else ""
    val itemsHtml = site.items.joinToString("\n") { item ->
        val msg = java.net.URLEncoder.encode("مرحباً أود طلب: " + item.nameAr, "UTF-8")
        val badge = if (!item.badgeAr.isNullOrBlank()) "<span class=\"badge\">" + siteEsc(item.badgeAr) + "</span>" else ""
        val priceHtml = if (item.priceEgp <= 0) "<div class=\"price\">اتصل للسعر</div>" else {
            val old = if (item.originalPriceEgp > item.priceEgp) "<s class=\"old\">" + item.originalPriceEgp + "</s>" else ""
            val off = if (item.originalPriceEgp > item.priceEgp) "<span class=\"off\">خصم " + ((item.originalPriceEgp - item.priceEgp) * 100 / item.originalPriceEgp) + "%</span>" else ""
            "<div class=\"price\">" + item.priceEgp + " ج.م " + old + off + "</div>"
        }
        """
        <div class="card reveal" data-cat="${siteEsc(item.category)}">
            <div class="card-body">
                <h3>${siteEsc(item.nameAr)} $badge</h3>
                <p>${siteEsc(item.descriptionAr)}</p>
                $priceHtml
            </div>
            <a href="https://wa.me/$waNum?text=$msg" class="order-btn" target="_blank" rel="noopener">طلب الآن</a>
        </div>
        """.trimIndent()
    }
    val features = (SITE_FEATURES[site.businessType.uppercase()] ?: SITE_FEATURES["AGENCY"]!!).joinToString("\n") {
        "<div class=\"feat reveal\"><div class=\"ico\">" + it.first + "</div><h4>" + siteEsc(it.second) + "</h4><p>" + siteEsc(it.third) + "</p></div>"
    }
    val options = site.items.joinToString("") { "<option>" + siteEsc(it.nameAr) + "</option>" }
    val bookingHtml = if (t.featureBookingSystem || t.featureWhatsappOrders) """
  <section id="book" class="panel reveal">
    <h2 class="t">${if (t.featureBookingSystem) "احجز أو اطلب الآن" else "اطلب الآن"}</h2>
    <form id="bf" class="form">
      <input id="f-name" placeholder="الاسم" required>
      <input id="f-phone" type="tel" placeholder="رقم الموبايل" required>
      <select id="f-item"><option value="">اختر الخدمة أو المنتج</option>$options</select>
      <input id="f-when" placeholder="الموعد المناسب (اختياري)">
      <textarea id="f-note" rows="3" placeholder="ملاحظات (اختياري)"></textarea>
      <button type="submit" class="btn btn-wa">إرسال عبر واتساب</button>
    </form>
  </section>""" else ""
    val ex = site.extras
    fun normUrl(raw: String, base: String): String {
        val v = raw.trim().removePrefix("@")
        if (v.isEmpty()) return ""
        if (v.startsWith("http://") || v.startsWith("https://")) return v
        if (v.contains(":")) return ""
        if (v.contains(".")) return "https://" + v
        return base + v
    }
    val socials = listOf(
        "فيسبوك" to normUrl(ex.facebook, "https://facebook.com/"),
        "إنستجرام" to normUrl(ex.instagram, "https://instagram.com/"),
        "تيك توك" to normUrl(ex.tiktok, "https://tiktok.com/@"),
        "يوتيوب" to normUrl(ex.youtube, "https://youtube.com/@"),
        "تيليجرام" to normUrl(ex.telegram, "https://t.me/"),
        "الموقع" to normUrl(ex.website, "https://")
    ).filter { it.second.isNotEmpty() }
    val socialHtml = if (socials.isEmpty()) "" else "<div class=\"social reveal\">" + socials.joinToString("") { "<a href=\"" + siteEsc(it.second) + "\" target=\"_blank\" rel=\"noopener\">" + siteEsc(it.first) + "</a>" } + "</div>"
    val testimonialsHtml = if (ex.testimonials.isEmpty()) "" else "<section id=\"reviews\" class=\"panel reveal\"><h2 class=\"t\">آراء عملائنا</h2><div class=\"revs\">" +
        ex.testimonials.joinToString("") { "<div class=\"rev\"><div class=\"stars\">" + "★".repeat(it.stars.coerceIn(1, 5)) + "</div><p>" + siteEsc(it.text) + "</p><b>" + siteEsc(it.name) + "</b></div>" } + "</div></section>"
    val galleryImgs = ex.gallery.take(8).mapNotNull { g ->
        if (g.startsWith("http://") || g.startsWith("https://")) siteEsc(g)
        else try {
            val f = java.io.File(g)
            if (f.exists() && f.length() < 400_000) "data:image/jpeg;base64," + android.util.Base64.encodeToString(f.readBytes(), android.util.Base64.NO_WRAP) else null
        } catch (e: Exception) { null }
    }
    val galleryHtml = if (galleryImgs.isEmpty()) "" else "<section id=\"gallery\" class=\"panel reveal\"><h2 class=\"t\">معرض الصور</h2><div class=\"gal\">" +
        galleryImgs.joinToString("") { "<img loading=\"lazy\" alt=\"\" src=\"" + it + "\">" } + "</div></section>"
    val extrasHtml = testimonialsHtml + galleryHtml + socialHtml
    val faqHtml = if (t.featureFaqSection) """
  <section id="faq" class="panel reveal">
    <h2 class="t">أسئلة شائعة</h2>
    <details><summary>إزاي أطلب أو أحجز؟</summary><p>املأ النموذج أعلاه أو راسلنا مباشرة على واتساب وهنرد عليك في أسرع وقت.</p></details>
    <details><summary>إيه مواعيد العمل؟</summary><p>${siteEsc(site.workingHours)}</p></details>
    <details><summary>فين مكانكم؟</summary><p>${siteEsc(site.address)}</p></details>
  </section>""" else ""
    val mapHtml = if (t.featureGoogleMaps && site.address.isNotBlank()) {
        val q = java.net.URLEncoder.encode(site.address, "UTF-8")
        "<div class=\"map\"><iframe title=\"map\" loading=\"lazy\" referrerpolicy=\"no-referrer-when-downgrade\" src=\"https://maps.google.com/maps?q=" + q + "&output=embed\"></iframe></div>"
    } else ""
    val catalogHtml = if (t.featureProductCatalog && site.items.isNotEmpty()) """
  <section id="menu">
    <h2 class="t reveal">قائمة المنتجات والخدمات</h2>
    <div class="chips">$chips</div>
    <div class="grid">
$itemsHtml
    </div>
  </section>""" else ""
    val callBtn = if (t.featureDirectCalling) "<a href=\"tel:" + siteEsc(site.phoneNumber) + "\" class=\"btn btn-call\">اتصل بنا: " + siteEsc(site.phoneNumber) + "</a>" else ""
    val desc = siteEsc(site.headline + " - " + site.subheadline)

    return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${siteEsc(site.siteTitle)} | ${siteEsc(site.headline)}</title>
<meta name="description" content="$desc">
<meta name="theme-color" content="$primary">
<meta property="og:title" content="${siteEsc(site.siteTitle)}">
<meta property="og:description" content="$desc">
<meta property="og:type" content="website">
<link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800&display=swap" rel="stylesheet">
<style>
:root { --primary: $primary; --ink: #1c1917; --muted: #6b7280; --paper: #fffdf8; --card: #ffffff; --line: rgba(0,0,0,.08); }
[data-theme="dark"] { --ink: #f5f5f4; --muted: #a8a29e; --paper: #121110; --card: #1c1a18; --line: rgba(255,255,255,.1); }
* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Cairo', sans-serif; }
html { scroll-behavior: smooth; }
body { background: var(--paper); color: var(--ink); line-height: 1.7; overflow-x: hidden; transition: background .3s, color .3s; }
nav { position: sticky; top: 0; z-index: 20; display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 10px 16px; background: var(--card); border-bottom: 1px solid var(--line); backdrop-filter: blur(8px); }
nav b { font-weight: 800; font-size: 1.05rem; }
nav .links { display: flex; gap: 14px; align-items: center; font-size: .9rem; }
nav a { color: var(--ink); text-decoration: none; opacity: .8; }
nav a:hover { color: var(--primary); opacity: 1; }
#theme { background: none; border: 1px solid var(--line); color: var(--ink); width: 36px; height: 36px; border-radius: 50%; cursor: pointer; font-size: 1rem; }
.hero { position: relative; min-height: 78vh; display: flex; align-items: center; justify-content: center; text-align: center; color: #fff; overflow: hidden; }
.hero-bg { position: absolute; inset: 0; background-image: $heroCss; background-size: cover; background-position: center; animation: kenburns 18s ease-in-out infinite alternate; }
.hero::after { content: ''; position: absolute; inset: 0; background: linear-gradient(180deg, rgba(0,0,0,.35), rgba(0,0,0,.68)); }
.hero-inner { position: relative; z-index: 2; padding: 32px 20px; max-width: 760px; }
.hero h1 { font-size: clamp(2rem, 7vw, 3.4rem); font-weight: 800; animation: rise .9s ease both; }
.tagline { font-size: 1.3rem; font-weight: 700; color: var(--primary); margin: 8px 0; animation: rise .9s .15s ease both; filter: brightness(1.35); }
.sub { opacity: .92; margin: 0 auto 26px; animation: rise .9s .3s ease both; }
.cta { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; animation: rise .9s .45s ease both; }
.btn { padding: 13px 28px; border-radius: 999px; font-weight: 700; text-decoration: none; border: 0; cursor: pointer; font-size: 1rem; transition: transform .25s, box-shadow .25s; }
.btn:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(0,0,0,.3); }
.btn-wa { background: #25D366; color: #fff; }
.btn-call { background: rgba(255,255,255,.15); border: 2px solid #fff; color: #fff; backdrop-filter: blur(4px); }
.wrap { max-width: 1040px; margin: 0 auto; padding: 40px 20px 20px; }
h2.t { font-size: 1.7rem; font-weight: 800; margin-bottom: 22px; position: relative; padding-bottom: 10px; }
h2.t::after { content: ''; position: absolute; right: 0; bottom: 0; width: 56px; height: 4px; border-radius: 4px; background: var(--primary); }
.feats { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 16px; margin-bottom: 40px; }
.feat { background: var(--card); border: 1px solid var(--line); border-radius: 18px; padding: 20px; text-align: center; }
.feat .ico { font-size: 2rem; margin-bottom: 6px; }
.feat h4 { font-size: 1.05rem; } .feat p { color: var(--muted); font-size: .88rem; }
.revs { display: grid; gap: 14px; } .rev { background: var(--bg); border: 1px solid var(--line); border-radius: 14px; padding: 16px; }
.rev .stars { color: #f5b301; letter-spacing: 2px; } .rev p { margin: 8px 0; }
.gal { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; } .gal img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 12px; }
.social { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin-bottom: 40px; } .social a { padding: 10px 18px; border-radius: 999px; border: 1px solid var(--line); background: var(--card); color: inherit; text-decoration: none; font-weight: 700; }
.panel { background: var(--card); border-radius: 18px; padding: 26px; box-shadow: 0 8px 30px rgba(0,0,0,.06); margin-bottom: 40px; border: 1px solid var(--line); }
.chips { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 18px; }
.chip { border: 1px solid var(--line); background: var(--card); color: var(--ink); padding: 7px 16px; border-radius: 999px; cursor: pointer; font-weight: 600; }
.chip.on { background: var(--primary); color: #fff; border-color: var(--primary); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(270px, 1fr)); gap: 22px; margin-bottom: 46px; }
.card { background: var(--card); border-radius: 18px; padding: 22px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 6px 22px rgba(0,0,0,.07); border-top: 4px solid var(--primary); transition: transform .3s, box-shadow .3s; }
.card:hover { transform: translateY(-8px); box-shadow: 0 18px 40px rgba(0,0,0,.14); }
.card h3 { font-size: 1.15rem; margin-bottom: 6px; }
.card p { color: var(--muted); font-size: .92rem; margin-bottom: 12px; }
.badge { background: var(--primary); color: #fff; font-size: .7rem; padding: 2px 9px; border-radius: 999px; margin-right: 6px; }
.price { font-size: 1.3rem; font-weight: 800; color: var(--primary); margin-bottom: 14px; }
.old { color: var(--muted); font-size: .9rem; font-weight: 400; margin-right: 6px; }
.off { background: #dc2626; color: #fff; font-size: .72rem; padding: 2px 8px; border-radius: 6px; margin-right: 6px; font-weight: 700; }
.order-btn { background: var(--primary); color: #fff; text-align: center; padding: 11px; border-radius: 12px; font-weight: 700; text-decoration: none; transition: filter .2s; }
.order-btn:hover { filter: brightness(1.12); }
.form { display: grid; gap: 12px; }
.form input, .form select, .form textarea { width: 100%; padding: 13px 14px; border-radius: 12px; border: 1px solid var(--line); background: var(--paper); color: var(--ink); font-size: 1rem; }
.form button { justify-self: start; }
details { border-bottom: 1px solid var(--line); padding: 12px 0; }
summary { cursor: pointer; font-weight: 700; }
details p { color: var(--muted); margin-top: 8px; }
.map { border-radius: 18px; overflow: hidden; margin-bottom: 20px; border: 1px solid var(--line); }
.map iframe { width: 100%; height: 300px; border: 0; display: block; }
.contact { text-align: center; }
.contact h2.t::after { right: 50%; transform: translateX(50%); }
footer { text-align: center; padding: 28px; color: var(--muted); font-size: .85rem; }
.fab { position: fixed; bottom: 20px; left: 20px; z-index: 10; background: #25D366; color: #fff; text-decoration: none; font-weight: 700; padding: 14px 22px; border-radius: 999px; box-shadow: 0 8px 22px rgba(0,0,0,.3); animation: pulse 2.2s infinite; }
#top { position: fixed; bottom: 20px; right: 20px; z-index: 10; width: 44px; height: 44px; border-radius: 50%; border: 0; background: var(--primary); color: #fff; font-size: 1.2rem; cursor: pointer; display: none; box-shadow: 0 6px 16px rgba(0,0,0,.25); }
.reveal { opacity: 0; transform: translateY(28px); transition: opacity .7s ease, transform .7s ease; }
.reveal.in { opacity: 1; transform: none; }
@keyframes kenburns { from { transform: scale(1); } to { transform: scale(1.12) translateY(-1.5%); } }
@keyframes rise { from { opacity: 0; transform: translateY(24px); } to { opacity: 1; transform: none; } }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(37,211,102,.6); } 70% { box-shadow: 0 0 0 16px rgba(37,211,102,0); } 100% { box-shadow: 0 0 0 0 rgba(37,211,102,0); } }
@media (max-width: 560px) { nav .links a { display: none; } }
@media (prefers-reduced-motion: reduce) { .hero-bg, .fab { animation: none; } .reveal { opacity: 1; transform: none; } }
</style>
</head>
<body>
<nav>
  <b>${siteEsc(site.siteTitle)}</b>
  <div class="links">
    <a href="#menu">الخدمات</a><a href="#book">احجز</a><a href="#faq">أسئلة</a><a href="#contact">تواصل</a>
    <button id="theme" title="الوضع الليلي">🌙</button>
  </div>
</nav>
<section class="hero">
  <div class="hero-bg"></div>
  <div class="hero-inner">
    <h1>${siteEsc(site.siteTitle)}</h1>
    <div class="tagline">${siteEsc(site.headline)}</div>
    <p class="sub">${siteEsc(site.subheadline)}</p>
    <div class="cta">
      <a href="https://wa.me/$waNum" class="btn btn-wa" target="_blank" rel="noopener">اطلب عبر واتساب</a>
      $callBtn
    </div>
  </div>
</section>
<div class="wrap">
  <div class="feats">
$features
  </div>
  <section class="panel reveal">
    <h2 class="t">نبذة عنا</h2>
    <p>${siteEsc(site.aboutText)}</p>
  </section>
$catalogHtml
$bookingHtml
$extrasHtml
$faqHtml
  <section id="contact" class="panel contact reveal">
    <h2 class="t">العنوان ومواعيد العمل</h2>
    <p>📍 ${siteEsc(site.address)}</p>
    <p>⏰ ${siteEsc(site.workingHours)}</p>
  </section>
$mapHtml
</div>
<footer>تم إنشاء هذا الموقع بواسطة منصة FOKOF2</footer>
<a class="fab" href="https://wa.me/$waNum" target="_blank" rel="noopener">واتساب</a>
<button id="top" title="لأعلى">↑</button>
<script>
(function () {
  var els = document.querySelectorAll('.reveal');
  if (!('IntersectionObserver' in window)) { els.forEach(function (e) { e.classList.add('in'); }); }
  else {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) { if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); } });
    }, { threshold: 0.12 });
    els.forEach(function (e, i) { e.style.transitionDelay = ((i % 4) * 90) + 'ms'; io.observe(e); });
  }
  var root = document.documentElement, tb = document.getElementById('theme');
  tb.addEventListener('click', function () {
    var dark = root.getAttribute('data-theme') === 'dark';
    if (dark) { root.removeAttribute('data-theme'); tb.textContent = '🌙'; } else { root.setAttribute('data-theme', 'dark'); tb.textContent = '☀️'; }
  });
  var top = document.getElementById('top');
  window.addEventListener('scroll', function () { top.style.display = window.scrollY > 500 ? 'block' : 'none'; });
  top.addEventListener('click', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); });
  document.querySelectorAll('.chip').forEach(function (c) {
    c.addEventListener('click', function () {
      document.querySelectorAll('.chip').forEach(function (x) { x.classList.remove('on'); });
      c.classList.add('on');
      var cat = c.getAttribute('data-cat');
      document.querySelectorAll('.card').forEach(function (card) {
        card.style.display = (cat === '__all' || card.getAttribute('data-cat') === cat) ? '' : 'none';
      });
    });
  });
  var bf = document.getElementById('bf');
  if (bf) bf.addEventListener('submit', function (e) {
    e.preventDefault();
    var lines = ['مرحباً، أود التواصل بخصوص:'];
    var item = document.getElementById('f-item').value;
    if (item) lines.push('الخدمة: ' + item);
    lines.push('الاسم: ' + document.getElementById('f-name').value);
    lines.push('الموبايل: ' + document.getElementById('f-phone').value);
    var w = document.getElementById('f-when').value; if (w) lines.push('الموعد: ' + w);
    var n = document.getElementById('f-note').value; if (n) lines.push('ملاحظات: ' + n);
    window.open('https://wa.me/$waNum?text=' + encodeURIComponent(lines.join('\n')), '_blank');
  });
})();
</script>
</body>
</html>
    """.trimIndent()
}
