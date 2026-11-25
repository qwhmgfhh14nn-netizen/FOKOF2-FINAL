package com.example.ui.screens

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
import com.example.ui.components.TemplateVisualCanvas
import com.example.ui.components.TemplateThumb
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryModel
import com.example.data.model.DesignTemplate
import com.example.data.model.TemplateBundle
import com.example.ui.locale.TranslationStrings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.util.parseColor

@Composable
fun TemplatesScreen(
    strings: TranslationStrings,
    templates: List<DesignTemplate>,
    categories: List<CategoryModel> = emptyList(),
    bundles: List<TemplateBundle>,
    favoriteIds: Set<String>,
    purchasedTemplateIds: Set<String>,
    onPreviewTemplate: (DesignTemplate) -> Unit,
    onUseTemplate: (DesignTemplate) -> Unit,
    onBuyTemplate: (DesignTemplate) -> Unit,
    onBuyBundle: (TemplateBundle) -> Unit,
    onToggleFavorite: (String) -> Unit,
    initialQuery: String = "",
    initialFilter: String = "ALL",
    modifier: Modifier = Modifier
) {
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }

    val dynamicCategories = templates.map { it.category }.filter { it.isNotBlank() }.distinct().map { it to it }

    val filterOptions = listOf(
        "ALL" to strings.filterAll,
        "FREE" to "قوالب مجانية",
        "PRO" to "قوالب مدفوعة PRO",
        "BUNDLES" to "باقات التوفير (Bundles)"
    ) + dynamicCategories

    val filtered = templates.filter { tpl ->
        val matchesSearch = tpl.titleAr.contains(searchQuery, ignoreCase = true) ||
                tpl.titleEn.contains(searchQuery, ignoreCase = true) ||
                tpl.category.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "FREE" -> !tpl.isPremium || tpl.priceEgp == 0
            "PRO" -> tpl.isPremium && tpl.priceEgp > 0
            "BUNDLES" -> false
            "ALL" -> true
            else -> tpl.category.equals(selectedFilter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }

    val headerBlock: @Composable () -> Unit = {
        Column {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(strings.searchTemplates, fontSize = 12.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("templates_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterOptions) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GoldPrimary else GoldBorder.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedFilter = key },
                    color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Content
        if (selectedFilter == "BUNDLES") {
            // Bundles List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { headerBlock() }
                item {
                    Text(
                        text = "باقات قوالب التوفير بأسعار مخفضة بالجنيه المصري (EGP):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                items(bundles) { bundle ->
                    BundleCard(bundle = bundle, strings = strings, onBuy = { onBuyBundle(bundle) })
                }
            }
        } else {
            // Masonry gallery grid
            androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid(
                columns = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Fixed(2),
                modifier = Modifier.weight(1f).testTag("templates_masonry_grid"),
                verticalItemSpacing = 10.dp,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) { headerBlock() }
                if (selectedFilter == "ALL" && searchQuery.isBlank()) {
                    item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { selectedFilter = "BUNDLES" }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = if (isArabicUi(strings)) "باقات التوفير" else "Savings bundles", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = if (isArabicUi(strings)) "من ٥ قوالب بـ ٢٠ ج.م لحد ١٠٠ قالب" else "From 5 templates for 20 EGP up to 100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = if (isArabicUi(strings)) "عرض" else "View", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isArabicUi(strings)) "مفيش قوالب مطابقة. جرّب كلمة تانية." else "No matching templates. Try another word.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                items(filtered.size, key = { filtered[it].id }) { idx ->
                    val tpl = filtered[idx]
                    GalleryTile(
                        template = tpl,
                        isFavorite = favoriteIds.contains(tpl.id),
                        isPurchased = purchasedTemplateIds.contains(tpl.id),
                        onClick = { onPreviewTemplate(tpl) },
                        onToggleFavorite = { onToggleFavorite(tpl.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun BundleCard(
    bundle: TemplateBundle,
    strings: TranslationStrings,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (bundle.isPopular) GoldPrimary else GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bundle.titleAr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily)
                    )
                    if (bundle.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("الأكثر طلباً", fontSize = 10.sp, color = Color(0xFF101014), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${bundle.originalPriceEgp} ج.م",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${bundle.priceEgp} ${strings.currencyEgp}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CinzelFontFamily
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bundle.descriptionAr,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = Color(0xFF101014), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "شراء الباقة الآن (${bundle.priceEgp} ج.م)",
                    color = Color(0xFF101014),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TemplateDetailedCard(
    template: DesignTemplate,
    strings: TranslationStrings,
    isFavorite: Boolean,
    isPurchased: Boolean,
    onPreview: () -> Unit,
    onUse: () -> Unit,
    onBuy: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GoldBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Visual Preview Block with Rich Graphic Canvas
            TemplateThumb(
                template = template,
                maxHeight = 340.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { onPreview() }
            ) {

                // Price Badge (EGP)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isPurchased -> Color(0xFF10B981)
                                template.priceEgp > 0 -> GoldPrimary
                                else -> Color(0xFF10B981)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isPurchased -> "مملوك لك"
                            template.priceEgp > 0 -> "${template.proTier?.titleAr ?: "PRO"} • ${template.priceEgp} ${strings.currencyEgp}"
                            else -> strings.freeBadge
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF101014),
                            fontWeight = FontWeight.Bold,
                            fontFamily = CinzelFontFamily
                        )
                    )
                }

                // Favorite Heart Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Details and CTAs
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.titleAr,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${template.category} • أبعاد ${template.aspectRatio} • ${template.elements.size} طبقات",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${template.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview Button
                    OutlinedButton(
                        onClick = onPreview,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.previewTemplate, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Main Action (Use if Free or Purchased, Buy if PRO)
                    val canDirectlyUse = !template.isPremium || template.priceEgp == 0 || isPurchased
                    Button(
                        onClick = if (canDirectlyUse) onUse else onBuy,
                        modifier = Modifier.weight(1.3f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (canDirectlyUse) Icons.Default.Edit else Icons.Default.Payment,
                            contentDescription = null,
                            tint = Color(0xFF101014),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (canDirectlyUse) strings.useInStudio else "${strings.buyTemplate} (${template.priceEgp} ${strings.currencyEgp})",
                            color = Color(0xFF101014),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
