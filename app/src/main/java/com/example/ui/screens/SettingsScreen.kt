package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.OwnerAuthDialog
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary

/** Plain settings list: language, theme, upgrade, and (owner/admin only) the admin panel. */
@Composable
fun SettingsScreen(
    isArabic: Boolean,
    isDarkTheme: Boolean,
    currentUser: UserProfile,
    isOwnerAuthenticated: Boolean,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onUpgradeVip: () -> Unit,
    onOpenAdmin: () -> Unit,
    onVerifyOwnerPassword: (String, (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val ar = isArabic
    var showAuth by remember { mutableStateOf(false) }
    val isStaff = !currentUser.isGuest && (currentUser.role == "OWNER" || currentUser.role == "ADMIN")
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
            }
            Text(
                text = if (ar) "الإعدادات" else "Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                MenuRow(
                    label = if (ar) "اللغة: ${if (ar) "العربية" else "English"}" else "Language: English",
                    icon = Icons.Default.Translate, tint = GoldPrimary, onClick = onToggleLanguage, tag = "settings_language",
                    trailing = {
                        Button(
                            onClick = onToggleLanguage,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text(if (ar) "English" else "العربية", color = Color(0xFF101014), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                )
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(
                    label = if (ar) "المظهر الداكن" else "Dark mode",
                    icon = Icons.Default.DarkMode, tint = GoldPrimary, onClick = onToggleTheme, tag = "settings_theme",
                    trailing = {
                        Switch(
                            checked = isDarkTheme, onCheckedChange = { onToggleTheme() },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldPrimary.copy(alpha = 0.3f))
                        )
                    }
                )
                HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                MenuRow(
                    label = if (ar) "ترقية الباقة / إشعار تحويل" else "Upgrade plan / transfer notice",
                    icon = Icons.Default.Payment, tint = GoldPrimary, onClick = onUpgradeVip, tag = "settings_upgrade"
                )
                if (isStaff) {
                    HorizontalDivider(color = GoldBorder.copy(alpha = 0.35f))
                    MenuRow(
                        label = if (ar) "لوحة الإدارة (للمالك)" else "Admin panel (owner)",
                        icon = Icons.Default.AdminPanelSettings, tint = GoldPrimary,
                        onClick = { if (isOwnerAuthenticated) onOpenAdmin() else showAuth = true },
                        tag = "admin_portal_card"
                    )
                }
            }
        }
    }
    if (showAuth) {
        OwnerAuthDialog(
            onDismiss = { showAuth = false },
            onVerify = { password, onResult ->
                onVerifyOwnerPassword(password) { success, msg ->
                    if (success) { showAuth = false; onOpenAdmin() }
                    onResult(success, msg)
                }
            }
        )
    }
}
