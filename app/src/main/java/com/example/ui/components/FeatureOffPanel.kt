package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.FeatureAvailability
import com.example.ui.theme.GoldPrimary

/** Shown instead of a screen whose service the owner switched off. */
@Composable
fun FeatureOffPanel(feature: FeatureAvailability?, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp).testTag("feature_off_panel"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = feature?.titleAr ?: "",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = GoldPrimary),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (feature?.status == "COMING_SOON") "هذه الخدمة قادمة قريباً" else
                feature?.messageAr?.takeIf { it.isNotBlank() } ?: "هذه الخدمة غير متوفرة حالياً",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
            Text("العودة للرئيسية", color = Color(0xFF101014), fontWeight = FontWeight.Bold)
        }
    }
}
