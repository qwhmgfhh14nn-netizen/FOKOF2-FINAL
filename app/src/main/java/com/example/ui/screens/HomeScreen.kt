package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DesignTemplate
import com.example.ui.AppNavDestination
import com.example.ui.components.TemplateVisualCanvas
import com.example.ui.components.TemplateThumb
import com.example.ui.locale.TranslationStrings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.parseColor

fun isArabicUi(strings: TranslationStrings) = strings.navHome == "الرئيسية"

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable { onAction() }.padding(4.dp)
            )
        }
    }
}

@Composable
fun CircleShortcut(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit, tag: String = "") {
    Column(
        modifier = Modifier.width(76.dp).clickable { onClick() }.testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(62.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)).border(1.dp, tint.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            maxLines = 2
        )
    }
}

/** Clean gallery tile: the artwork only, with a small heart and a PRO crown. No text over the picture. */
@Composable
fun GalleryTile(
    template: DesignTemplate,
    isFavorite: Boolean,
    isPurchased: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: androidx.compose.ui.unit.Dp = 520.dp
) {
    val isPro = template.isPremium && template.priceEgp > 0 && !isPurchased
    TemplateThumb(
        template = template,
        maxHeight = maxHeight,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF3A3326),
                modifier = Modifier.size(14.dp)
            )
        }
        if (isPro) {
            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).size(24.dp).clip(CircleShape).background(Color(0xFF2B2417)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "PRO", tint = Color(0xFFF2D67C), modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
fun HomeScreen(
    strings: TranslationStrings,
    searchQuery: String,
    templates: List<DesignTemplate>,
    userProjects: List<DesignTemplate>,
    favoriteIds: Set<String>,
    purchasedIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onOpenTemplates: (String, String) -> Unit,
    onNavigate: (AppNavDestination) -> Unit,
    onStartDesign: (ratio: String, titleAr: String, titleEn: String) -> Unit,
    onOpenProject: (DesignTemplate) -> Unit,
    onPreviewTemplate: (DesignTemplate) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ar = isArabicUi(strings)
    val freeTemplates = templates.filter { !it.isPremium || it.priceEgp == 0 }
    val proTemplates = templates.filter { it.isPremium && it.priceEgp > 0 }
    val featuredList = (templates.filter { it.isFeatured } + freeTemplates).distinctBy { it.id }.take(10)
    val categories = templates.filter { it.category.isNotBlank() }.groupBy { it.category }.entries.toList()
    val viewAll = if (ar) "عرض الكل" else "View all"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (ar) "إيه اللي هتبدعه النهاردة؟" else "What will you create today?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily),
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = if (ar) "دوّر على قالب أو تصميم..." else "Search templates and designs...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearchSubmit(searchQuery) }),
            modifier = Modifier.fillMaxWidth().testTag("home_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GoldBorder,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Continue designing (only real saved projects)
        if (userProjects.isNotEmpty()) {
            SectionHeader(if (ar) "كمّل تصميمك" else "Continue designing", viewAll) { onNavigate(AppNavDestination.PROJECTS) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(userProjects.take(10)) { p ->
                    Column(modifier = Modifier.width(130.dp).clickable { onOpenProject(p) }) {
                        TemplateThumb(
                            template = p,
                            maxHeight = 150.dp,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.titleAr, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Quick start (blank canvas in a chosen size)
        SectionHeader(if (ar) "اختار طريقة البدء" else "Choose how to start")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val starts = listOf(
                Triple(if (ar) "منشور سوشيال" else "Social post", Icons.Default.Image, "1:1"),
                Triple(if (ar) "ستوري / ريلز" else "Story / Reel", Icons.Default.Smartphone, "9:16"),
                Triple(if (ar) "بوستر" else "Poster", Icons.Default.Campaign, "4:5"),
                Triple(if (ar) "عرض تقديمي" else "Presentation", Icons.Default.Slideshow, "16:9"),
                Triple(if (ar) "حجم مخصص" else "Custom size", Icons.Default.AspectRatio, "1:1")
            )
            val tints = listOf(Color(0xFFD4AF37), Color(0xFFB8860B), Color(0xFF9A7416), Color(0xFFC79A2B), Color(0xFF7A5C0A))
            items(starts.size) { i ->
                val (label, icon, ratio) = starts[i]
                CircleShortcut(label, icon, tints[i], tag = "quick_start_$i", onClick = {
                    if (i == 4) onNavigate(AppNavDestination.STUDIO)
                    onStartDesign(ratio, "تصميم جديد", "New Design")
                })
            }
        }

        // Quick tools: only features that really exist in the app
        SectionHeader(if (ar) "أدوات سريعة" else "Quick tools")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val tools = listOf(
                Triple(if (ar) "سيرة ذاتية" else "CV maker", Icons.Default.Description, AppNavDestination.CV_MAKER),
                Triple(if (ar) "توليد صور" else "AI images", Icons.Default.AutoAwesome, AppNavDestination.AI_IMAGE),
                Triple(if (ar) "كتابة إعلانات" else "Ad copy", Icons.Default.Campaign, AppNavDestination.AI_MARKETING),
                Triple(if (ar) "الهوية البصرية" else "Brand kit", Icons.Default.ColorLens, null),
                Triple(if (ar) "منشئ المواقع" else "Websites", Icons.Default.Language, AppNavDestination.WEBSITES),
                Triple(if (ar) "موك آب" else "Mockups", Icons.Default.ShoppingBag, AppNavDestination.MOCKUP_STUDIO)
            )
            items(tools.size) { i ->
                val (label, icon, dest) = tools[i]
                CircleShortcut(label, icon, Color(0xFF8A6B12), tag = "quick_tool_$i", onClick = {
                    // The brand-identity icon opens the logos and identities templates
                    if (dest == null) onOpenTemplates("", "الشعارات والهويات") else onNavigate(dest)
                })
            }
        }

        if (templates.isEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (ar) "مفيش قوالب لسه. ابدأ من الصفر في الاستوديو." else "No templates yet. Start from scratch in the Studio.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            if (featuredList.isNotEmpty()) {
                SectionHeader(if (ar) "قوالب قد تعجبك" else "Templates you may like", viewAll) { onOpenTemplates("", "ALL") }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(featuredList) { tpl ->
                        GalleryTile(
                            template = tpl,
                            isFavorite = favoriteIds.contains(tpl.id),
                            isPurchased = purchasedIds.contains(tpl.id),
                            onClick = { onPreviewTemplate(tpl) },
                            onToggleFavorite = { onToggleFavorite(tpl.id) },
                            modifier = Modifier.width(150.dp),
                            maxHeight = 240.dp
                        )
                    }
                }
            }

            if (categories.isNotEmpty()) {
                SectionHeader(if (ar) "استعرض فئات القوالب" else "Browse template categories")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories) { (cat, list) ->
                        Column(modifier = Modifier.width(110.dp).clickable { onOpenTemplates("", cat) }.testTag("home_category")) {
                            TemplateThumb(
                                template = list.first(),
                                maxHeight = 110.dp,
                                modifier = Modifier.clip(RoundedCornerShape(14.dp)).border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cat, fontSize = 12.sp, maxLines = 2, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (proTemplates.isNotEmpty()) {
                SectionHeader(if (ar) "قوالب PRO" else "PRO templates", viewAll) { onOpenTemplates("", "PRO") }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(proTemplates.take(10)) { tpl ->
                        GalleryTile(
                            template = tpl,
                            isFavorite = favoriteIds.contains(tpl.id),
                            isPurchased = purchasedIds.contains(tpl.id),
                            onClick = { onPreviewTemplate(tpl) },
                            onToggleFavorite = { onToggleFavorite(tpl.id) },
                            modifier = Modifier.width(150.dp),
                            maxHeight = 240.dp
                        )
                    }
                }
            }

            if (freeTemplates.isNotEmpty()) {
                SectionHeader(if (ar) "قوالب مجانية" else "Free templates", viewAll) { onOpenTemplates("", "FREE") }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(freeTemplates.take(14)) { tpl ->
                        GalleryTile(
                            template = tpl,
                            isFavorite = favoriteIds.contains(tpl.id),
                            onClick = { onPreviewTemplate(tpl) },
                            onToggleFavorite = { onToggleFavorite(tpl.id) },
                            modifier = Modifier.width(150.dp),
                            maxHeight = 240.dp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AiToolMiniCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun HomeTemplateCard(
    template: DesignTemplate,
    strings: TranslationStrings,
    isFavorite: Boolean,
    onPreview: () -> Unit,
    onUse: () -> Unit,
    onBuy: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GoldBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Visual Preview Thumbnail Box with Rich Graphic Canvas
            TemplateThumb(
                template = template,
                maxHeight = 220.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .clickable { onPreview() }
            ) {

                // Price Badge (Egyptian Pounds)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (template.priceEgp > 0) GoldPrimary else Color(0xFF10B981))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (template.priceEgp > 0) "${template.priceEgp} ${strings.currencyEgp}" else strings.freeBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF101014),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                // Favorite Heart Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Info & CTAs
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = template.titleAr,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${template.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "${template.salesCount} مبيعة",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = if (template.priceEgp > 0) onBuy else onUse,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (template.priceEgp > 0) "شراء" else "تعديل",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101014)
                        )
                    }

                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
