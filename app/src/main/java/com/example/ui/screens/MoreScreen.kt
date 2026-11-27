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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.AppNavDestination
import com.example.ui.locale.TranslationStrings
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary

@Composable
fun MenuRow(
    label: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    tag: String = "",
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontFamily = CairoFontFamily, color = tint)
        )
        if (trailing != null) trailing() else Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun MoreScreen(
    strings: TranslationStrings,
    currentUser: UserProfile,
    isDarkTheme: Boolean,
    isArabic: Boolean,
    isOwnerAuthenticated: Boolean,
    onNavigate: (AppNavDestination) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenHelp: () -> Unit,
    onUpgradeVip: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ar = isArabic
    val isStaff = !currentUser.isGuest && (currentUser.role == "OWNER" || currentUser.role == "ADMIN")
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (!currentUser.isVip) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GoldPrimary.copy(alpha = 0.14f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("more_vip_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ar) "حقق أهدافك مع FOKOF2 VIP" else "Reach your goals with FOKOF2 VIP",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (ar) "قوالب PRO وأدوات أكتر بباقة VIP. الترقية بتتم بتحويل يدوي." else "PRO templates and more tools with VIP. Upgrade is by manual transfer.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onUpgradeVip,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (ar) "رقّي باقتك" else "Upgrade your plan", color = Color(0xFF101014), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Profile card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .clickable { onNavigate(AppNavDestination.ACCOUNT) }
                .testTag("more_profile_card"),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name.trim().take(1).ifBlank { "F" },
                        color = Color(0xFF101014), fontWeight = FontWeight.Bold, fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text(
                        text = if (currentUser.isGuest) (if (ar) "زائر. سجّل دخولك عشان مشاريعك تتحفظ" else "Guest. Sign in to save your projects") else currentUser.email,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tools group
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                MenuRow(if (ar) "الهوية البصرية" else "Brand kit", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.BRAND_KIT) }, "more_brand")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "اعمل سيرتك الذاتية" else "CV maker", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.CV_MAKER) }, "more_cv")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "صمم بالنموذج (شهادة، كارت، منيو...)" else "Design by form", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.ENGINE_MAKER) }, "more_engine")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "صانع الفيديو (صور وانتقالات)" else "Video maker", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.VIDEO_MAKER) }, "more_video")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "قص ودمج فيديو" else "Trim and join video", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.VIDEO_TRIM) }, "more_trim")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "أدوات الصور (تعديل، ميم، كولاج، ملصقات)" else "Photo tools", Icons.Default.ColorLens, GoldPrimary, { onNavigate(AppNavDestination.PHOTO_TOOLS) }, "more_photo")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "منشئ المواقع" else "Website builder", Icons.Default.Language, GoldPrimary, { onNavigate(AppNavDestination.WEBSITES) }, "more_websites")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "توليد الصور بالذكاء" else "AI image generator", Icons.Default.AutoAwesome, GoldPrimary, { onNavigate(AppNavDestination.AI_IMAGE) }, "more_ai_image")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "كتابة الإعلانات" else "Ad copywriter", Icons.Default.Campaign, GoldPrimary, { onNavigate(AppNavDestination.AI_MARKETING) }, "more_ai_copy")

MenuRow(if (ar) "الرد الذكي على العملاء" else "Smart customer replies", Icons.Default.AutoAwesome, GoldPrimary, { onNavigate(AppNavDestination.SMART_REPLY) }, "more_smart_reply")
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(if (ar) "استوديو الموك آب" else "Mockup studio", Icons.Default.ViewInAr, GoldPrimary, { onNavigate(AppNavDestination.MOCKUP_STUDIO) }, "more_mockup")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        MenuRow(if (ar) "حسابك" else "Your account", Icons.Default.PersonOutline, onClick = { onNavigate(AppNavDestination.ACCOUNT) }, tag = "more_account")
        MenuRow(if (ar) "الإعدادات" else "Settings", Icons.Default.Settings, onClick = { onNavigate(AppNavDestination.SETTINGS) }, tag = "more_settings")
        MenuRow(if (ar) "مشترياتي" else "My purchases", Icons.Default.ShoppingBag, onClick = { onNavigate(AppNavDestination.PURCHASES) }, tag = "more_purchases")
        MenuRow(
            label = if (ar) "المظهر الداكن" else "Dark mode",
            icon = Icons.Default.DarkMode,
            onClick = onToggleTheme,
            tag = "more_theme",
            trailing = {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldPrimary.copy(alpha = 0.3f))
                )
            }
        )
        MenuRow(if (ar) "المساعدة (اسأل المساعد)" else "Help (ask the assistant)", Icons.Default.HelpOutline, onClick = onOpenHelp, tag = "more_help")
        if (isStaff) {
            MenuRow(
                if (ar) "الأدوات المتقدمة (لوحة الإدارة)" else "Advanced tools (admin)",
                Icons.Default.AdminPanelSettings,
                onClick = { onNavigate(if (isOwnerAuthenticated) AppNavDestination.ADMIN else AppNavDestination.SETTINGS) },
                tag = "more_admin"
            )
        }
        if (!currentUser.isGuest) {
            MenuRow(if (ar) "تسجيل الخروج" else "Log out", Icons.AutoMirrored.Filled.Logout, MaterialTheme.colorScheme.error, onLogout, "more_logout")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
