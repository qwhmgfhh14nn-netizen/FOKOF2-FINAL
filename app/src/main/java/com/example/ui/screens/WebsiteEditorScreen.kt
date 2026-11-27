package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.data.model.SitePage
import com.example.data.model.PageSection
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
    onAutoSave: (UserWebsite) -> Unit = onSave,
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

    // Undo/redo history + continuous autosave (website builder spec sections 1.6-1.7).
    // Snapshots of the whole site are taken after a short pause in editing.
    val editHistory = remember(website.id) { mutableStateListOf(website) }
    var historyIndex by remember(website.id) { mutableIntStateOf(0) }
    var applyingHistory by remember(website.id) { mutableStateOf(false) }
    var lastAutoSavedAt by remember(website.id) { mutableStateOf<Long?>(null) }

    LaunchedEffect(siteTitle, siteSlug, headline, subheadline, aboutText, whatsappNumber, phoneNumber, address, workingHours, primaryColorHex, items, extras) {
        if (applyingHistory) { applyingHistory = false; return@LaunchedEffect }
        val snap = buildUpdatedWebsite()
        val top = editHistory.getOrNull(historyIndex)
        if (top != null && snap.copy(updatedAt = 0L) == top.copy(updatedAt = 0L)) return@LaunchedEffect
        kotlinx.coroutines.delay(1200)
        while (editHistory.size > historyIndex + 1) editHistory.removeAt(editHistory.lastIndex)
        editHistory.add(snap)
        historyIndex = editHistory.lastIndex
        onAutoSave(snap)
        lastAutoSavedAt = snap.updatedAt
    }

    val applySnapshot: (UserWebsite) -> Unit = { s ->
        applyingHistory = true
        siteTitle = s.siteTitle; siteSlug = s.siteSlug; headline = s.headline; subheadline = s.subheadline
        aboutText = s.aboutText; whatsappNumber = s.whatsappNumber; phoneNumber = s.phoneNumber
        address = s.address; workingHours = s.workingHours; primaryColorHex = s.primaryColorHex
        items = s.items; extras = s.extras
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
                        if (lastAutoSavedAt != null) {
                            Text(
                                text = "بيتحفظ تلقائياً ✓",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedIconButton(
                        onClick = {
                            if (historyIndex > 0) {
                                historyIndex--
                                val s = editHistory[historyIndex]
                                applySnapshot(s)
                                onAutoSave(s)
                                lastAutoSavedAt = s.updatedAt
                            }
                        },
                        enabled = historyIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.testTag("website_editor_undo_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    }

                    androidx.compose.material3.OutlinedIconButton(
                        onClick = {
                            if (historyIndex < editHistory.lastIndex) {
                                historyIndex++
                                val s = editHistory[historyIndex]
                                applySnapshot(s)
                                onAutoSave(s)
                                lastAutoSavedAt = s.updatedAt
                            }
                        },
                        enabled = historyIndex < editHistory.lastIndex,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.testTag("website_editor_redo_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "إعادة", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    }

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
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = {
                    Text(
                        if (extras.pages.size > 1) "الصفحات (${extras.pages.size})" else "الصفحات",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) }
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
                4 -> PagesTab(
                    extras = extras,
                    onExtrasChange = { extras = it }
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

@Composable
private fun SitePhoto(path: String, modifier: Modifier = Modifier) {
    val bmp = remember(path) { try { android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (e: Exception) { null } }
    if (bmp != null) androidx.compose.foundation.Image(bitmap = bmp, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = modifier)
}

/** A picked photo as a data URI for the exported site, or an http(s) link as is; empty when missing or too large. */
private fun siteImageSrc(path: String, maxBytes: Int = 600_000): String {
    if (path.isBlank()) return ""
    if (path.startsWith("http://") || path.startsWith("https://")) return siteEsc(path)
    return try {
        val f = java.io.File(path)
        if (f.exists() && f.length() < maxBytes) "data:image/jpeg;base64," + android.util.Base64.encodeToString(f.readBytes(), android.util.Base64.NO_WRAP) else ""
    } catch (e: Exception) { "" }
}

private fun saveSiteGalleryPhoto(context: Context, uri: android.net.Uri, maxDim: Float = 800f): String? {
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }
        var sample = 1
        while (opts.outWidth / sample > 1600) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            android.graphics.BitmapFactory.decodeStream(it, null, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = maxDim / maxOf(bmp.width, bmp.height).toFloat()
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
    val heroPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = saveSiteGalleryPhoto(context, uri, 1400f)
            if (path != null) onChange(extras.copy(heroImage = path))
        }
    }
    val logoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = saveSiteGalleryPhoto(context, uri, 400f)
            if (path != null) onChange(extras.copy(logoImage = path))
        }
    }
    var newFieldLabel by remember { mutableStateOf("") }
    var newFieldKind by remember { mutableStateOf("text") }
    var tName by remember { mutableStateOf("") }
    var tText by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("الصورة الرئيسية للمتجر (الغلاف)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            if (extras.heroImage.isNotBlank()) {
                SitePhoto(extras.heroImage, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    heroPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text(if (extras.heroImage.isBlank()) "إضافة صورة الغلاف" else "تغيير صورة الغلاف", fontFamily = CairoFontFamily) }
                if (extras.heroImage.isNotBlank()) {
                    OutlinedButton(onClick = { onChange(extras.copy(heroImage = "")) }) { Text("حذف الصورة", fontFamily = CairoFontFamily) }
                }
            }

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

            Text("شكل الموقع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            Text("اللون (التاني: حسب لون موقعك)", fontFamily = CairoFontFamily)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { androidx.compose.material3.FilterChip(selected = extras.theme == 0, onClick = { onChange(extras.copy(theme = 0)) }, label = { Text("حسب لوني") }) }
                items(SITE_THEMES.size) { i ->
                    androidx.compose.material3.FilterChip(selected = extras.theme == i + 1, onClick = { onChange(extras.copy(theme = i + 1)) }, label = { Text(SITE_THEMES[i].first) })
                }
            }
            Text("الخط", fontFamily = CairoFontFamily)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SITE_FONTS.size) { i ->
                    androidx.compose.material3.FilterChip(selected = extras.font == i, onClick = { onChange(extras.copy(font = i)) }, label = { Text(SITE_FONTS[i].first) })
                }
            }
            OutlinedTextField(value = extras.announcement, onValueChange = { onChange(extras.copy(announcement = it.take(120))) }, label = { Text("شريط إعلان أعلى الموقع (اتركه فاضي لو مش عايزه)") }, modifier = Modifier.fillMaxWidth())
            androidx.compose.material3.FilterChip(selected = extras.hideFooter, onClick = { onChange(extras.copy(hideFooter = !extras.hideFooter)) }, label = { Text("إخفاء سطر 'تم إنشاؤه بواسطة FOKOF2' في الأسفل") })
            Text("لوجو الموقع (اختياري)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            if (extras.logoImage.isNotBlank()) {
                SitePhoto(extras.logoImage, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    logoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text(if (extras.logoImage.isBlank()) "إضافة لوجو" else "تغيير اللوجو", fontFamily = CairoFontFamily) }
                if (extras.logoImage.isNotBlank()) {
                    OutlinedButton(onClick = { onChange(extras.copy(logoImage = "")) }) { Text("حذف اللوجو", fontFamily = CairoFontFamily) }
                }
            }

            Text("حقول إضافية لنموذج الحجز/التواصل (حتى 6)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            extras.customFields.forEachIndexed { i, f ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("• " + f.label + " (" + when (f.kind) { "phone" -> "موبايل"; "note" -> "ملاحظة"; else -> "نص" } + ")", modifier = Modifier.weight(1f), fontFamily = CairoFontFamily)
                    IconButton(onClick = { onChange(extras.copy(customFields = extras.customFields.filterIndexed { j, _ -> j != i })) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                    }
                }
            }
            OutlinedTextField(value = newFieldLabel, onValueChange = { newFieldLabel = it.take(40) }, label = { Text("اسم الحقل (مثل: العنوان بالتفصيل)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                androidx.compose.material3.FilterChip(selected = newFieldKind == "text", onClick = { newFieldKind = "text" }, label = { Text("نص") })
                androidx.compose.material3.FilterChip(selected = newFieldKind == "phone", onClick = { newFieldKind = "phone" }, label = { Text("موبايل") })
                androidx.compose.material3.FilterChip(selected = newFieldKind == "note", onClick = { newFieldKind = "note" }, label = { Text("ملاحظة") })
            }
            Button(onClick = {
                val l = newFieldLabel.trim()
                if (l.isNotEmpty() && extras.customFields.size < 6) {
                    onChange(extras.copy(customFields = extras.customFields + com.example.data.model.SiteFormField(l, newFieldKind)))
                    newFieldLabel = ""
                }
            }) { Text("إضافة الحقل", fontFamily = CairoFontFamily) }

            Text("كوبون خصم (اختياري)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            Text("العميل يكتب الكود في السلة فيتخصم من الإجمالي، والطلب يوصلك على واتساب بالسعر بعد الخصم.", fontFamily = CairoFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = extras.couponCode, onValueChange = { onChange(extras.copy(couponCode = it.trim().take(20))) }, label = { Text("كود الخصم (اتركه فاضي لإلغاء الكوبون)", fontFamily = CairoFontFamily) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(
                value = if (extras.couponPercent == 0) "" else extras.couponPercent.toString(),
                onValueChange = { onChange(extras.copy(couponPercent = (it.filter { c -> c.isDigit() }.take(2).toIntOrNull() ?: 0).coerceIn(0, 90))) },
                label = { Text("نسبة الخصم %", fontFamily = CairoFontFamily) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

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
                            if (item.imagePath.isNotBlank()) {
                                SitePhoto(item.imagePath, Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)))
                                Spacer(modifier = Modifier.width(10.dp))
                            }
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
        var imagePath by remember { mutableStateOf("") }
        val dlgContext = LocalContext.current
        val productPicker = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
        ) { uri -> if (uri != null) saveSiteGalleryPhoto(dlgContext, uri)?.let { imagePath = it } }

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

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (imagePath.isNotBlank()) SitePhoto(imagePath, Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)))
                        OutlinedButton(
                            onClick = { productPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text(if (imagePath.isBlank()) "إضافة صورة المنتج" else "تغيير الصورة", fontFamily = CairoFontFamily) }
                        if (imagePath.isNotBlank()) {
                            androidx.compose.material3.TextButton(onClick = { imagePath = "" }) { Text("حذف", fontFamily = CairoFontFamily) }
                        }
                    }

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
                                        isAvailable = true,
                                        imagePath = imagePath
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
                            if (website.extras.heroImage.isNotBlank()) {
                                SitePhoto(website.extras.heroImage, Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(14.dp)))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
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
                                if (item.imagePath.isNotBlank()) {
                                    SitePhoto(item.imagePath, Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)))
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
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
        if (website.extras.pages.size > 1) generateSitePageHtml(website, context, 0, relativePageLinks(website))
        else generateStandaloneHtml(website, context)
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
                                        if (website.extras.pages.size > 1) publishAllPages(context, website)
                                        else com.example.data.SitePublisher.publish(context, website.id, website.siteSlug, website.siteTitle, htmlNow)
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
                            text = if (publishing) "جاري النشر..." else if (website.extras.pages.size > 1) "انشر كل الصفحات (${website.extras.pages.size}) واحصل على رابط" else "انشر واحصل على رابط",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Real ZIP export (website builder spec section 12): full standalone site as files.
                var zipBusy by remember { mutableStateOf(false) }
                var zipInfo by remember { mutableStateOf<String?>(null) }
                Button(
                    onClick = {
                        if (!zipBusy) {
                            zipBusy = true; zipInfo = null
                            publishScope.launch {
                                val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    com.example.data.SiteExporter.buildZip(context, website)
                                }
                                zipBusy = false
                                if (result != null) {
                                    zipInfo = "اتعمل ملف ZIP (${result.file.length() / 1024} ك.ب) فيه: " + result.entryNames.joinToString("، ")
                                    try {
                                        val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", result.file)
                                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "application/zip"
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(send, "شارك أو احفظ ملف الموقع ZIP").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                                    } catch (e: Exception) { /* الملف متاح في التخزين الداخلي حتى بدون مشاركة */ }
                                } else {
                                    zipInfo = "ما قدرناش نعمل ملف ZIP دلوقتي، حاول تاني"
                                }
                            }
                        }
                    },
                    enabled = !zipBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("website_export_zip_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (zipBusy) "جاري تصدير الموقع..." else "صدّر الموقع ZIP (HTML + CSS + JS + الصور)",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                zipInfo?.let {
                    Text(text = it, fontFamily = CairoFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

internal val SITE_THEMES = listOf(
    Triple("ذهبي ملكي", "#D4AF37", "#fffdf8"), Triple("زمردي", "#0F766E", "#f6fbf9"), Triple("ياقوتي", "#B91C1C", "#fff8f7"),
    Triple("أزرق ملكي", "#1D4ED8", "#f7f9ff"), Triple("بنفسجي", "#6D28D9", "#faf7ff"), Triple("وردي", "#DB2777", "#fff7fb"),
    Triple("برتقالي", "#EA580C", "#fffaf5"), Triple("أخضر زيتي", "#4D7C0F", "#fafcf5"), Triple("تركوازي", "#0891B2", "#f5fcfd"),
    Triple("بني قهوة", "#78350F", "#fcf8f4"), Triple("رمادي أنيق", "#374151", "#f9fafb"), Triple("ليلي ذهبي", "#D4AF37", "#121110")
)
internal val SITE_FONTS = listOf("Cairo" to "Cairo", "Tajawal" to "Tajawal", "Almarai" to "Almarai", "Amiri (كلاسيك)" to "Amiri", "Changa" to "Changa", "El Messiri" to "El Messiri")

internal fun generateStandaloneHtml(site: UserWebsite, context: Context? = null): String {
    val themeSel = site.extras.theme
    val primary = if (themeSel in 1..SITE_THEMES.size) SITE_THEMES[themeSel - 1].second
        else if (Regex("^#[0-9A-Fa-f]{6}$").matches(site.primaryColorHex)) site.primaryColorHex else "#D4AF37"
    val paperCss = if (themeSel in 1..SITE_THEMES.size) ":root { --paper: " + SITE_THEMES[themeSel - 1].third + "; }" else ""
    val darkAttr = if (themeSel == SITE_THEMES.size) " data-theme=\"dark\"" else ""
    val fontName = SITE_FONTS[site.extras.font.coerceIn(0, SITE_FONTS.size - 1)].second
    val fontUrl = fontName.replace(" ", "+")
    val annHtml = if (site.extras.announcement.isNotBlank()) "<div class=\"ann\">" + siteEsc(site.extras.announcement) + "</div>" else ""
    val faviconLink = siteFaviconLink(site)
    val logoImg = siteLogoImg(site)
    val couponCode = site.extras.couponCode.trim()
    val couponPct = if (couponCode.isNotEmpty()) site.extras.couponPercent.coerceIn(0, 90) else 0
    val couponJs = couponCode.replace("\\", "\\\\").replace("'", "\\'")
    val customFieldsHtml = site.extras.customFields.joinToString("\n") { f ->
        val lab = siteEsc(f.label)
        when (f.kind) {
            "phone" -> "      <input class=\"cf\" data-l=\"$lab\" type=\"tel\" placeholder=\"$lab\">"
            "note" -> "      <textarea class=\"cf\" data-l=\"$lab\" rows=\"2\" placeholder=\"$lab\"></textarea>"
            else -> "      <input class=\"cf\" data-l=\"$lab\" placeholder=\"$lab\">"
        }
    }
    val couponRow = if (couponPct > 0) "<div class=\"form\" style=\"margin-top:8px\"><input id=\"c-cpn\" placeholder=\"كود الخصم\" data-p=\"كود الخصم|Discount code\"><button id=\"c-cpn-btn\" type=\"button\" class=\"btn\" data-t=\"تطبيق|Apply\">تطبيق</button></div>" else ""
    val footerHtml = if (site.extras.hideFooter) "" else "<footer data-t=\"تم إنشاء هذا الموقع بواسطة منصة FOKOF2|Built with FOKOF2\">تم إنشاء هذا الموقع بواسطة منصة FOKOF2</footer>"
    val hero = siteHeroDataUri(context, site.businessType)
    val storeHero = siteImageSrc(site.extras.heroImage, 900_000)
    val heroCss = if (storeHero.isNotEmpty()) "url('" + storeHero + "')" else if (hero.isNotEmpty()) "url('" + hero + "')" else "linear-gradient(135deg, " + primary + ", #1f2937)"
    val t = site.featureToggles
    val waNum = site.whatsappNumber.filter { it.isDigit() }
    val cats = site.items.map { it.category }.distinct()
    val chips = if (t.featureProductCatalog && cats.size > 1) {
        "<button class=\"chip on\" data-cat=\"__all\">الكل</button>" + cats.joinToString("") { "<button class=\"chip\" data-cat=\"" + siteEsc(it) + "\">" + siteEsc(it) + "</button>" }
    } else ""
    val itemsHtml = site.items.joinToString("\n") { item ->
        val msg = java.net.URLEncoder.encode("مرحباً أود طلب: " + item.nameAr, "UTF-8")
        val pimgSrc = siteImageSrc(item.imagePath)
        val pimg = if (pimgSrc.isNotEmpty()) "<img class=\"pimg\" loading=\"lazy\" alt=\"" + siteEsc(item.nameAr) + "\" src=\"" + pimgSrc + "\">" else ""
        val badge = if (!item.badgeAr.isNullOrBlank()) "<span class=\"badge\">" + siteEsc(item.badgeAr) + "</span>" else ""
        val priceHtml = if (item.priceEgp <= 0) "<div class=\"price\">اتصل للسعر</div>" else {
            val old = if (item.originalPriceEgp > item.priceEgp) "<s class=\"old\">" + item.originalPriceEgp + "</s>" else ""
            val off = if (item.originalPriceEgp > item.priceEgp) "<span class=\"off\">خصم " + ((item.originalPriceEgp - item.priceEgp) * 100 / item.originalPriceEgp) + "%</span>" else ""
            "<div class=\"price\">" + item.priceEgp + " ج.م " + old + off + "</div>"
        }
        """
        <div class="card reveal" data-cat="${siteEsc(item.category)}" data-name="${siteEsc(item.nameAr)}" data-en="${siteEsc(item.nameEn)}" data-price="${item.priceEgp}">
            $pimg
            <div class="card-body">
                <h3>${siteEsc(item.nameAr)} $badge</h3>
                <p>${siteEsc(item.descriptionAr)}</p>
                $priceHtml
            </div>
            <a href="https://wa.me/$waNum?text=$msg" class="order-btn" target="_blank" rel="noopener" data-t="طلب الآن|Order now">طلب الآن</a>
            ${if (item.priceEgp > 0 && t.featureWhatsappOrders) "<button type=\"button\" class=\"add-btn\" data-t=\"+ أضف للسلة|+ Add to cart\">+ أضف للسلة</button>" else ""}
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
$customFieldsHtml
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
    val ldJson = org.json.JSONObject().apply {
        put("@context", "https://schema.org"); put("@type", "LocalBusiness")
        put("name", site.siteTitle); put("description", site.headline + " - " + site.subheadline)
        if (site.phoneNumber.isNotBlank()) put("telephone", site.phoneNumber)
        if (site.address.isNotBlank()) put("address", site.address)
        if (site.workingHours.isNotBlank()) put("openingHours", site.workingHours)
    }.toString().replace("</", "<\\/")

    return """
<!DOCTYPE html>
<html lang="ar" dir="rtl"$darkAttr>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${siteEsc(site.siteTitle)} | ${siteEsc(site.headline)}</title>
$faviconLink
<meta name="description" content="$desc">
<meta name="theme-color" content="$primary">
<meta property="og:title" content="${siteEsc(site.siteTitle)}">
<meta property="og:description" content="$desc">
<meta property="og:type" content="website">
<meta property="og:locale" content="ar_EG">
<meta name="twitter:card" content="summary_large_image">
<script type="application/ld+json">$ldJson</script>
<link href="https://fonts.googleapis.com/css2?family=$fontUrl:wght@400;600;700;800&display=swap" rel="stylesheet">
<style>
:root { --primary: $primary; --ink: #1c1917; --muted: #6b7280; --paper: #fffdf8; --card: #ffffff; --line: rgba(0,0,0,.08); }
[data-theme="dark"] { --ink: #f5f5f4; --muted: #a8a29e; --paper: #121110; --card: #1c1a18; --line: rgba(255,255,255,.1); }
* { box-sizing: border-box; margin: 0; padding: 0; font-family: '$fontName', 'Cairo', sans-serif; }
$paperCss
.ann { background: var(--primary); color: #fff; text-align: center; padding: 8px 14px; font-weight: 700; font-size: .92rem; }
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
.pimg { width: calc(100% + 44px); max-width: none; margin: -22px -22px 14px; height: 180px; object-fit: cover; border-radius: 14px 14px 0 0; display: block; }
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
.add-btn { margin-top: 8px; background: transparent; color: var(--primary); border: 2px solid var(--primary); padding: 9px; border-radius: 12px; font-weight: 700; cursor: pointer; }
.add-btn:hover { background: var(--primary); color: #fff; }
.card { cursor: pointer; }
#lang { background: none; border: 1px solid var(--line); color: var(--ink); height: 36px; padding: 0 12px; border-radius: 999px; cursor: pointer; font-weight: 700; }
.share { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; align-items: center; margin: 0 0 30px; }
.share a, .share button { padding: 9px 16px; border-radius: 999px; border: 1px solid var(--line); background: var(--card); color: var(--ink); text-decoration: none; cursor: pointer; font-weight: 600; font-size: .9rem; }
#cartbtn { position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%); z-index: 12; background: var(--primary); color: #fff; border: 0; padding: 13px 24px; border-radius: 999px; font-weight: 800; box-shadow: 0 8px 22px rgba(0,0,0,.3); cursor: pointer; display: none; }
.sheet { position: fixed; inset: 0; z-index: 30; background: rgba(0,0,0,.55); display: none; align-items: flex-end; justify-content: center; }
.sheet.open { display: flex; }
.sheet-in { background: var(--card); color: var(--ink); width: min(560px, 100%); max-height: 86vh; overflow: auto; border-radius: 22px 22px 0 0; padding: 22px; }
.sheet-in h3 { margin-bottom: 12px; }
.crow { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--line); }
.crow .q button { width: 30px; height: 30px; border-radius: 50%; border: 1px solid var(--line); background: var(--paper); color: var(--ink); cursor: pointer; font-weight: 800; }
.ctotal { font-weight: 800; font-size: 1.15rem; margin: 14px 0; }
.sheet-in .x { float: left; background: none; border: 0; color: var(--ink); font-size: 1.4rem; cursor: pointer; }
.sheet-in .pimg { width: 100%; margin: 0 0 12px; border-radius: 14px; }
@media (max-width: 560px) { nav .links a { display: none; } }
@media (prefers-reduced-motion: reduce) { .hero-bg, .fab { animation: none; } .reveal { opacity: 1; transform: none; } }
</style>
</head>
<body>
$annHtml
<nav>
  $logoImg<b>${siteEsc(site.siteTitle)}</b>
  <div class="links">
    <a href="#menu">الخدمات</a><a href="#book">احجز</a><a href="#faq">أسئلة</a><a href="#contact">تواصل</a>
    <button id="lang" title="English / عربي">EN</button><button id="theme" title="الوضع الليلي">🌙</button>
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
<div class="share">
  <span data-t="شارك الموقع:|Share this site:">شارك الموقع:</span>
  <a id="sh-wa" href="#" target="_blank" rel="noopener">WhatsApp</a>
  <a id="sh-fb" href="#" target="_blank" rel="noopener">Facebook</a>
  <button id="sh-cp" type="button" data-t="نسخ الرابط|Copy link">نسخ الرابط</button>
</div>
$footerHtml
<button id="cartbtn" type="button"></button>
<div id="cartsheet" class="sheet"><div class="sheet-in"><button class="x" type="button" data-close="1">✕</button><h3 data-t="سلتك|Your cart">سلتك</h3><div id="clines"></div><div class="ctotal" id="ctotal"></div>$couponRow<div class="form"><input id="c-name" placeholder="الاسم" data-p="الاسم|Name"><input id="c-phone" type="tel" placeholder="رقم الموبايل" data-p="رقم الموبايل|Mobile number"><button id="c-send" type="button" class="btn btn-wa" data-t="إرسال الطلب عبر واتساب|Send order on WhatsApp">إرسال الطلب عبر واتساب</button></div></div></div>
<div id="detsheet" class="sheet"><div class="sheet-in"><button class="x" type="button" data-close="1">✕</button><div id="detbody"></div></div></div>
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
  if (root.getAttribute('data-theme') === 'dark') tb.textContent = '☀️';
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
  var WA = 'https://wa.me/$waNum';
  var CPN = '$couponJs', CPN_P = $couponPct, cpnOn = false;
  var en = false, cart = {};
  function T(k) { var p = k.split('|'); return en ? p[1] : p[0]; }
  function applyLang() {
    document.documentElement.lang = en ? 'en' : 'ar';
    document.documentElement.dir = en ? 'ltr' : 'rtl';
    document.querySelectorAll('[data-t]').forEach(function (e) { e.textContent = T(e.getAttribute('data-t')); });
    document.querySelectorAll('[data-p]').forEach(function (e) { e.placeholder = T(e.getAttribute('data-p')); });
    document.getElementById('lang').textContent = en ? 'عربي' : 'EN';
    document.querySelectorAll('.card').forEach(function (c) {
      var h = c.querySelector('h3'); var tn = h && h.firstChild;
      if (tn && tn.nodeType === 3) { var n = c.getAttribute('data-en'); tn.textContent = (en && n ? n : c.getAttribute('data-name')) + ' '; }
    });
    renderCart();
  }
  document.getElementById('lang').addEventListener('click', function () { en = !en; applyLang(); });
  function nm(k) { var c = document.querySelector('.card[data-name="' + k.replace(/"/g, '\\"') + '"]'); var e = c && c.getAttribute('data-en'); return en && e ? e : k; }
  function renderCart() {
    var keys = Object.keys(cart), total = 0, count = 0, html = '';
    keys.forEach(function (k) {
      var it = cart[k]; total += it.p * it.q; count += it.q;
      html += '<div class="crow"><span></span><span class="q"><button data-d="-1" data-k="' + encodeURIComponent(k) + '">-</button> ' + it.q + ' <button data-d="1" data-k="' + encodeURIComponent(k) + '">+</button></span><b>' + (it.p * it.q) + '</b></div>';
    });
    var box = document.getElementById('clines'); box.innerHTML = html;
    box.querySelectorAll('.crow').forEach(function (row, i) { row.firstChild.textContent = nm(keys[i]); });
    box.querySelectorAll('button[data-d]').forEach(function (b) {
      b.addEventListener('click', function () {
        var k = decodeURIComponent(b.getAttribute('data-k')); cart[k].q += parseInt(b.getAttribute('data-d'), 10);
        if (cart[k].q <= 0) delete cart[k]; renderCart();
      });
    });
    var ctotalEl = document.getElementById('ctotal');
    var disc = (cpnOn && CPN_P > 0) ? Math.round(total * CPN_P / 100) : 0;
    ctotalEl.textContent = disc > 0
      ? T('الإجمالي|Total') + ': ' + total + ' - ' + T('خصم|Discount') + ' ' + disc + ' = ' + (total - disc) + ' ' + T('ج.م|EGP')
      : T('الإجمالي|Total') + ': ' + total + ' ' + T('ج.م|EGP');
    var cb = document.getElementById('cartbtn');
    cb.style.display = count ? 'block' : 'none';
    cb.textContent = '🛒 ' + T('السلة|Cart') + ' (' + count + ') - ' + (total - disc) + ' ' + T('ج.م|EGP');
    if (!count) document.getElementById('cartsheet').classList.remove('open');
  }
  document.querySelectorAll('.add-btn').forEach(function (b) {
    b.addEventListener('click', function (ev) {
      ev.stopPropagation();
      var c = b.closest('.card'), k = c.getAttribute('data-name');
      if (!cart[k]) cart[k] = { p: parseInt(c.getAttribute('data-price'), 10) || 0, q: 0 };
      cart[k].q += 1; renderCart();
    });
  });
  document.getElementById('cartbtn').addEventListener('click', function () { document.getElementById('cartsheet').classList.add('open'); });
  document.querySelectorAll('.sheet').forEach(function (sh) {
    sh.addEventListener('click', function (ev) { if (ev.target === sh || ev.target.getAttribute('data-close')) sh.classList.remove('open'); });
  });
  document.getElementById('c-send').addEventListener('click', function () {
    var keys = Object.keys(cart); if (!keys.length) return;
    var total = 0, lines = [T('مرحباً، أود طلب:|Hello, I would like to order:')];
    keys.forEach(function (k) { var it = cart[k]; total += it.p * it.q; lines.push('- ' + nm(k) + ' x' + it.q + ' = ' + (it.p * it.q) + ' ' + T('ج.م|EGP')); });
    var disc = (cpnOn && CPN_P > 0) ? Math.round(total * CPN_P / 100) : 0;
    if (disc > 0) { lines.push(T('الإجمالي قبل الخصم|Subtotal') + ': ' + total + ' ' + T('ج.م|EGP')); lines.push(T('خصم|Discount') + ' (' + CPN + '): -' + disc + ' ' + T('ج.م|EGP')); }
    lines.push(T('الإجمالي|Total') + ': ' + (total - disc) + ' ' + T('ج.م|EGP'));
    var n = document.getElementById('c-name').value, p = document.getElementById('c-phone').value;
    if (n) lines.push(T('الاسم|Name') + ': ' + n); if (p) lines.push(T('الموبايل|Mobile') + ': ' + p);
    window.open(WA + '?text=' + encodeURIComponent(lines.join('\n')), '_blank');
  });
  document.querySelectorAll('.card').forEach(function (c) {
    c.addEventListener('click', function (ev) {
      if (ev.target.closest('a,button')) return;
      var copy = c.cloneNode(true);
      copy.querySelectorAll('a,button').forEach(function (x) { x.remove(); });
      var add = document.createElement('button'); add.type = 'button'; add.className = 'btn btn-wa'; add.textContent = T('+ أضف للسلة|+ Add to cart');
      add.addEventListener('click', function () { var b = c.querySelector('.add-btn'); if (b) b.click(); document.getElementById('detsheet').classList.remove('open'); });
      var body = document.getElementById('detbody'); body.innerHTML = '';
      var inner = copy.querySelector('.card-body'); var im = copy.querySelector('.pimg');
      if (im) body.appendChild(im); if (inner) body.appendChild(inner);
      if (c.querySelector('.add-btn')) body.appendChild(add);
      document.getElementById('detsheet').classList.add('open');
    });
  });
  var pageUrl = location.href;
  document.getElementById('sh-wa').href = 'https://wa.me/?text=' + encodeURIComponent(document.title + ' ' + pageUrl);
  document.getElementById('sh-fb').href = 'https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(pageUrl);
  document.getElementById('sh-cp').addEventListener('click', function () {
    if (navigator.share) { navigator.share({ title: document.title, url: pageUrl }).catch(function () {}); }
    else if (navigator.clipboard) { navigator.clipboard.writeText(pageUrl); this.textContent = T('تم النسخ ✓|Copied ✓'); }
  });
  var cpnBtn = document.getElementById('c-cpn-btn');
  if (cpnBtn) cpnBtn.addEventListener('click', function () {
    var v = document.getElementById('c-cpn').value.trim();
    if (v && v === CPN) { cpnOn = true; cpnBtn.textContent = T('تم ✓|Applied ✓'); }
    else { cpnOn = false; cpnBtn.textContent = T('كود غير صحيح|Invalid code'); }
    renderCart();
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
    document.querySelectorAll('#bf .cf').forEach(function (el) { var v = el.value; if (v) lines.push(el.getAttribute('data-l') + ': ' + v); });
    window.open('https://wa.me/$waNum?text=' + encodeURIComponent(lines.join('\n')), '_blank');
  });
})();
</script>
</body>
</html>
    """.trimIndent()
}

// ---------------- Multi-page sites (spec section 4) ----------------

internal val PAGE_SECTION_TYPES: List<Pair<String, String>> = listOf(
    "hero" to "هيدر الصفحة",
    "text" to "نص حر",
    "items" to "المنتجات والخدمات",
    "gallery" to "معرض الصور",
    "reviews" to "آراء العملاء",
    "faq" to "أسئلة شائعة",
    "map" to "الخريطة",
    "contact" to "التواصل والحجز"
)

internal fun pageFileName(index: Int): String = if (index <= 0) "index.html" else "page-$index.html"

internal fun relativePageLinks(site: UserWebsite): List<Pair<String, String>> {
    val pages = site.extras.pages
    if (pages.isEmpty()) return listOf("index.html" to "الرئيسية")
    return pages.mapIndexed { i, p -> pageFileName(i) to p.title }
}

private val PAGE_SCRIPT_RE = Regex("<script>.*?</script>", RegexOption.DOT_MATCHES_ALL)

private fun siteLogoImg(site: UserWebsite): String {
    val src = siteImageSrc(site.extras.logoImage, 60_000)
    return if (src.isNotEmpty()) "<img src=\"" + src + "\" alt=\"\" style=\"height:34px;width:34px;border-radius:8px;object-fit:cover;vertical-align:middle;margin-left:8px\">" else ""
}

private fun siteFaviconLink(site: UserWebsite): String {
    val src = siteImageSrc(site.extras.logoImage, 60_000)
    return if (src.isNotEmpty()) "<link rel=\"icon\" href=\"" + src + "\">" else ""
}

/** Builds one page of the site as a full standalone HTML document.
 *  Page 0 is the classic home page (with extra nav links injected when multi-page).
 *  Pages 1+ render their own sections using the shared design, nav, footer and script.
 *  links holds (href, title) for every page: relative file names for export, absolute urls after publishing. */
internal fun generateSitePageHtml(site: UserWebsite, context: Context?, pageIndex: Int, links: List<Pair<String, String>>): String {
    val home = generateStandaloneHtml(site, context)
    val pages = site.extras.pages
    if (pages.size <= 1 || pageIndex <= 0 || pageIndex >= pages.size) {
        if (links.size <= 1) return home
        val extra = links.drop(1).joinToString("") { "<a href=\"" + siteEsc(it.first) + "\">" + siteEsc(it.second) + "</a>" }
        return home.replace("<div class=\"links\">", "<div class=\"links\">\n    " + extra)
    }
    val page = pages[pageIndex]
    val headEnd = home.indexOf("</head>")
    var head = if (headEnd > 0) home.substring(0, headEnd + 7) else home.substring(0, 0)
    head = head.replace(Regex("<title>.*?</title>"), "<title>" + siteEsc(site.siteTitle) + " | " + siteEsc(page.title) + "</title>")
    val script = PAGE_SCRIPT_RE.findAll(home).lastOrNull()?.value ?: ""
    val waNum = site.whatsappNumber.filter { it.isDigit() }
    val annHtml = if (site.extras.announcement.isNotBlank()) "<div class=\"ann\">" + siteEsc(site.extras.announcement) + "</div>" else ""
    val navLinks = links.mapIndexedNotNull { i, l ->
        if (i == pageIndex) null else "<a href=\"" + siteEsc(l.first) + "\">" + siteEsc(l.second) + "</a>"
    }.joinToString("")
    val subCouponCode = site.extras.couponCode.trim()
    val subCouponPct = if (subCouponCode.isNotEmpty()) site.extras.couponPercent.coerceIn(0, 90) else 0
    val couponRow = if (subCouponPct > 0) "<div class=\"form\" style=\"margin-top:8px\"><input id=\"c-cpn\" placeholder=\"كود الخصم\" data-p=\"كود الخصم|Discount code\"><button id=\"c-cpn-btn\" type=\"button\" class=\"btn\" data-t=\"تطبيق|Apply\">تطبيق</button></div>" else ""
    val footerHtml = if (site.extras.hideFooter) "" else "<footer data-t=\"تم إنشاء هذا الموقع بواسطة منصة FOKOF2|Built with FOKOF2\">تم إنشاء هذا الموقع بواسطة منصة FOKOF2</footer>"
    val bodyContent = StringBuilder()
    page.sections.forEach { s -> bodyContent.append(renderPageSection(site, s, page.title)).append("\n") }
    return """$head
<body>
$annHtml
<nav>
  ${siteLogoImg(site)}<b>${siteEsc(site.siteTitle)}</b>
  <div class="links">
    $navLinks
    <button id="lang" title="English / عربي">EN</button><button id="theme" title="الوضع الليلي">🌙</button>
  </div>
</nav>
<div class="wrap">
$bodyContent</div>
<div class="share">
  <span data-t="شارك الموقع:|Share this site:">شارك الموقع:</span>
  <a id="sh-wa" href="#" target="_blank" rel="noopener">WhatsApp</a>
  <a id="sh-fb" href="#" target="_blank" rel="noopener">Facebook</a>
  <button id="sh-cp" type="button" data-t="نسخ الرابط|Copy link">نسخ الرابط</button>
</div>
$footerHtml
<button id="cartbtn" type="button"></button>
<div id="cartsheet" class="sheet"><div class="sheet-in"><button class="x" type="button" data-close="1">✕</button><h3 data-t="سلتك|Your cart">سلتك</h3><div id="clines"></div><div class="ctotal" id="ctotal"></div>$couponRow<div class="form"><input id="c-name" placeholder="الاسم" data-p="الاسم|Name"><input id="c-phone" type="tel" placeholder="رقم الموبايل" data-p="رقم الموبايل|Mobile number"><button id="c-send" type="button" class="btn btn-wa" data-t="إرسال الطلب عبر واتساب|Send order on WhatsApp">إرسال الطلب عبر واتساب</button></div></div></div>
<div id="detsheet" class="sheet"><div class="sheet-in"><button class="x" type="button" data-close="1">✕</button><div id="detbody"></div></div></div>
<a class="fab" href="https://wa.me/$waNum" target="_blank" rel="noopener">واتساب</a>
<button id="top" title="لأعلى">↑</button>
$script
</body>
</html>
    """.trimIndent()
}

private fun renderPageSection(site: UserWebsite, s: PageSection, pageTitle: String): String {
    val waNum = site.whatsappNumber.filter { it.isDigit() }
    return when (s.type) {
        "hero" -> "<section class=\"hero reveal\" style=\"min-height:42vh\"><div class=\"hero-bg\"></div><div class=\"hero-inner\"><h1>" + siteEsc(pageTitle) + "</h1>" + (if (s.text.isNotBlank()) "<p class=\"sub\">" + siteEsc(s.text) + "</p>" else "") + "</div></section>"
        "text" -> if (s.text.isBlank()) "" else "<section class=\"panel reveal\">" + s.text.split("\n").filter { it.isNotBlank() }.joinToString("") { "<p>" + siteEsc(it) + "</p>" } + "</section>"
        "items" -> pageItemsHtml(site, waNum)
        "gallery" -> pageGalleryHtml(site)
        "reviews" -> pageReviewsHtml(site)
        "faq" -> "<section id=\"faq\" class=\"panel reveal\"><h2 class=\"t\">أسئلة شائعة</h2><details><summary>إزاي أطلب أو أحجز؟</summary><p>املأ نموذج التواصل أو راسلنا مباشرة على واتساب وهنرد عليك في أسرع وقت.</p></details><details><summary>إيه مواعيد العمل؟</summary><p>" + siteEsc(site.workingHours) + "</p></details><details><summary>فين مكانكم؟</summary><p>" + siteEsc(site.address) + "</p></details></section>"
        "map" -> if (site.address.isBlank()) "" else {
            val q = java.net.URLEncoder.encode(site.address, "UTF-8")
            "<div class=\"map\"><iframe title=\"map\" loading=\"lazy\" referrerpolicy=\"no-referrer-when-downgrade\" src=\"https://maps.google.com/maps?q=" + q + "&output=embed\"></iframe></div>"
        }
        "contact" -> pageContactHtml(site)
        else -> ""
    }
}

private fun pageItemsHtml(site: UserWebsite, waNum: String): String {
    if (site.items.isEmpty()) return ""
    val cards = site.items.joinToString("\n") { item ->
        val msg = java.net.URLEncoder.encode("مرحباً أود طلب: " + item.nameAr, "UTF-8")
        val pimgSrc = siteImageSrc(item.imagePath)
        val pimg = if (pimgSrc.isNotEmpty()) "<img class=\"pimg\" loading=\"lazy\" alt=\"" + siteEsc(item.nameAr) + "\" src=\"" + pimgSrc + "\">" else ""
        val priceHtml = if (item.priceEgp <= 0) "<div class=\"price\">اتصل للسعر</div>" else "<div class=\"price\">" + item.priceEgp + " ج.م</div>"
        val addBtn = if (item.priceEgp > 0 && site.featureToggles.featureWhatsappOrders) "<button type=\"button\" class=\"add-btn\" data-t=\"+ أضف للسلة|+ Add to cart\">+ أضف للسلة</button>" else ""
        """<div class="card reveal" data-cat="${siteEsc(item.category)}" data-name="${siteEsc(item.nameAr)}" data-en="${siteEsc(item.nameEn)}" data-price="${item.priceEgp}">
            $pimg
            <div class="card-body">
                <h3>${siteEsc(item.nameAr)}</h3>
                <p>${siteEsc(item.descriptionAr)}</p>
                $priceHtml
            </div>
            <a href="https://wa.me/$waNum?text=$msg" class="order-btn" target="_blank" rel="noopener" data-t="طلب الآن|Order now">طلب الآن</a>
            $addBtn
        </div>"""
    }
    return "<section id=\"menu\"><h2 class=\"t reveal\">المنتجات والخدمات</h2><div class=\"grid\">\n" + cards + "\n</div></section>"
}

private fun pageGalleryHtml(site: UserWebsite): String {
    val imgs = site.extras.gallery.take(8).map { siteImageSrc(it) }.filter { it.isNotEmpty() }
    if (imgs.isEmpty()) return ""
    return "<section id=\"gallery\" class=\"panel reveal\"><h2 class=\"t\">معرض الصور</h2><div class=\"gal\">" + imgs.joinToString("") { "<img loading=\"lazy\" alt=\"\" src=\"" + it + "\">" } + "</div></section>"
}

private fun pageReviewsHtml(site: UserWebsite): String {
    if (site.extras.testimonials.isEmpty()) return ""
    return "<section id=\"reviews\" class=\"panel reveal\"><h2 class=\"t\">آراء عملائنا</h2><div class=\"revs\">" +
        site.extras.testimonials.joinToString("") { "<div class=\"rev\"><div class=\"stars\">" + "★".repeat(it.stars.coerceIn(1, 5)) + "</div><p>" + siteEsc(it.text) + "</p><b>" + siteEsc(it.name) + "</b></div>" } + "</div></section>"
}

private fun pageContactHtml(site: UserWebsite): String {
    val options = site.items.joinToString("") { "<option>" + siteEsc(it.nameAr) + "</option>" }
    val customFieldsHtml = site.extras.customFields.joinToString("\n") { f ->
        val lab = siteEsc(f.label)
        when (f.kind) {
            "phone" -> "      <input class=\"cf\" data-l=\"$lab\" type=\"tel\" placeholder=\"$lab\">"
            "note" -> "      <textarea class=\"cf\" data-l=\"$lab\" rows=\"2\" placeholder=\"$lab\"></textarea>"
            else -> "      <input class=\"cf\" data-l=\"$lab\" placeholder=\"$lab\">"
        }
    }
    return """<section id="book" class="panel reveal">
    <h2 class="t">احجز أو اطلب الآن</h2>
    <form id="bf" class="form">
      <input id="f-name" placeholder="الاسم" required>
      <input id="f-phone" type="tel" placeholder="رقم الموبايل" required>
      <select id="f-item"><option value="">اختر الخدمة أو المنتج</option>$options</select>
      <input id="f-when" placeholder="الموعد المناسب (اختياري)">
      <textarea id="f-note" rows="3" placeholder="ملاحظات (اختياري)"></textarea>
$customFieldsHtml
      <button type="submit" class="btn btn-wa">إرسال عبر واتساب</button>
    </form>
  </section>
  <section id="contact" class="panel contact reveal">
    <h2 class="t">العنوان ومواعيد العمل</h2>
    <p>📍 ${siteEsc(site.address)}</p>
    <p>⏰ ${siteEsc(site.workingHours)}</p>
  </section>"""
}

private fun pubKey(site: UserWebsite, i: Int): String = if (i == 0) site.id else site.id + "::p" + i
private fun pubSlug(site: UserWebsite, i: Int): String = if (i == 0) site.siteSlug else site.siteSlug + "-p" + i

/** Publishes every page of a multi-page site, then republishes with the nav rewritten to the real public urls.
 *  The returned url is the home page (page 0). */
private fun publishAllPages(context: Context, site: UserWebsite): com.example.data.SitePublisher.Result {
    val pages = site.extras.pages
    val rel = relativePageLinks(site)
    val urls = arrayOfNulls<String>(pages.size)
    for (i in pages.indices) {
        val r = com.example.data.SitePublisher.publish(context, pubKey(site, i), pubSlug(site, i), site.siteTitle + " - " + pages[i].title, generateSitePageHtml(site, context, i, rel))
        if (!r.ok) return r
        urls[i] = r.url
    }
    val abs = pages.mapIndexed { i, p -> (urls[i] ?: "") to p.title }
    var last: com.example.data.SitePublisher.Result = com.example.data.SitePublisher.Result(true, urls[0], null)
    for (i in pages.indices) {
        last = com.example.data.SitePublisher.publish(context, pubKey(site, i), pubSlug(site, i), site.siteTitle + " - " + pages[i].title, generateSitePageHtml(site, context, i, abs))
        if (!last.ok) return last
    }
    return com.example.data.SitePublisher.Result(true, urls[0], null)
}

// ---------------- TAB 4: PAGES ----------------

@Composable
private fun PagesTab(extras: SiteExtras, onExtrasChange: (SiteExtras) -> Unit) {
    val pages = extras.pages
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("صفحات الموقع", fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily, fontSize = 16.sp, color = GoldPrimary)
        if (pages.isEmpty()) {
            Text(
                "موقعك دلوقتي صفحة واحدة. فعّل الصفحات المتعددة عشان تضيف صفحات زي: من نحن، خدماتنا، اتصل بنا. كل صفحة بتتصدّر في ملف HTML لوحدها وبتظهر في قائمة الموقع.",
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { onExtrasChange(extras.copy(pages = listOf(SitePage(title = "الرئيسية")))) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) { Text("فعّل الصفحات المتعددة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black) }
        } else {
            var newPageTitle by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newPageTitle,
                    onValueChange = { newPageTitle = it },
                    label = { Text("اسم صفحة جديدة (مثل: من نحن)", fontFamily = CairoFontFamily) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val t = newPageTitle.trim()
                        if (t.isNotEmpty()) {
                            onExtrasChange(extras.copy(pages = pages + SitePage(title = t, sections = listOf(PageSection("hero", ""), PageSection("text", "")))))
                            newPageTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("أضف", fontFamily = CairoFontFamily, color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            pages.forEachIndexed { i, page ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (i == 0) {
                            Text("١. الرئيسية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = GoldPrimary)
                            Text(
                                "محتوى الصفحة الرئيسية بيتحرر من تبويبات البيانات الأساسية والمنتجات والإضافات.",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = page.title,
                                    onValueChange = { v -> onExtrasChange(extras.copy(pages = pages.mapIndexed { j, p -> if (j == i) p.copy(title = v) else p })) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                IconButton(onClick = { if (i > 1) onExtrasChange(extras.copy(pages = pages.toMutableList().apply { add(i - 1, removeAt(i)) })) }, enabled = i > 1) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "فوق", tint = GoldPrimary)
                                }
                                IconButton(onClick = { if (i < pages.lastIndex) onExtrasChange(extras.copy(pages = pages.toMutableList().apply { add(i + 1, removeAt(i)) })) }, enabled = i < pages.lastIndex) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "تحت", tint = GoldPrimary)
                                }
                                IconButton(onClick = { onExtrasChange(extras.copy(pages = pages.filterIndexed { j, _ -> j != i })) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRuby)
                                }
                            }
                            page.sections.forEachIndexed { j, s ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            PAGE_SECTION_TYPES.firstOrNull { it.first == s.type }?.second ?: s.type,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                        if (s.type == "hero" || s.type == "text") {
                                            OutlinedTextField(
                                                value = s.text,
                                                onValueChange = { v ->
                                                    onExtrasChange(extras.copy(pages = pages.mapIndexed { k, p ->
                                                        if (k == i) p.copy(sections = p.sections.mapIndexed { l, x -> if (l == j) x.copy(text = v) else x }) else p
                                                    }))
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = if (s.type == "text") 2 else 1,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        } else {
                                            Text(
                                                "بيترسم تلقائياً من بيانات موقعك",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        if (j > 0) onExtrasChange(extras.copy(pages = pages.mapIndexed { k, p ->
                                            if (k == i) p.copy(sections = p.sections.toMutableList().apply { add(j - 1, removeAt(j)) }) else p
                                        }))
                                    }, enabled = j > 0) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "فوق", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        if (j < page.sections.lastIndex) onExtrasChange(extras.copy(pages = pages.mapIndexed { k, p ->
                                            if (k == i) p.copy(sections = p.sections.toMutableList().apply { add(j + 1, removeAt(j)) }) else p
                                        }))
                                    }, enabled = j < page.sections.lastIndex) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "تحت", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        onExtrasChange(extras.copy(pages = pages.mapIndexed { k, p ->
                                            if (k == i) p.copy(sections = p.sections.filterIndexed { l, _ -> l != j }) else p
                                        }))
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRuby, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(PAGE_SECTION_TYPES.size) { t ->
                                    val type = PAGE_SECTION_TYPES[t].first
                                    val label = PAGE_SECTION_TYPES[t].second
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                        modifier = Modifier.clickable {
                                            onExtrasChange(extras.copy(pages = pages.mapIndexed { k, p ->
                                                if (k == i) p.copy(sections = p.sections + PageSection(type, "")) else p
                                            }))
                                        }
                                    ) {
                                        Text("+ $label", fontFamily = CairoFontFamily, fontSize = 12.sp, color = GoldPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Text(
                "التصدير بيطلع كل صفحة في ملف HTML لوحدها، والنشر بينشر كل الصفحات وبيظهر روابطها في قائمة الموقع.",
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
