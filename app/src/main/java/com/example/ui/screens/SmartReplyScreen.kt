package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary

@Composable
fun SmartReplyScreen(
    isArabic: Boolean,
    businessInfo: String,
    customerMessage: String,
    draft: String,
    isDrafting: Boolean,
    onBusinessInfoChange: (String) -> Unit,
    onCustomerMessageChange: (String) -> Unit,
    onDraft: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isArabic) "الرد الذكي على العملاء" else "Smart customer replies",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily),
            color = GoldPrimary
        )
        Text(
            text = if (isArabic) "اكتب معلومات نشاطك مرة واحدة، الصق رسالة العميل، والذكاء الاصطناعي يجهزلك رد تنسخه وتبعته." else "Enter your business info once, paste a customer message, and the AI drafts a reply to copy and send.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = businessInfo,
            onValueChange = onBusinessInfoChange,
            label = { Text(if (isArabic) "معلومات نشاطك (بتتحفظ تلقائيًا)" else "Your business info (saved automatically)") },
            placeholder = { Text(if (isArabic) "مثال: محل حلويات في المنصورة، بنستقبل أوردرات واتساب، التوصيل خلال يومين..." else "e.g. Dessert shop, orders on WhatsApp, delivery in 2 days...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = customerMessage,
            onValueChange = onCustomerMessageChange,
            label = { Text(if (isArabic) "رسالة العميل" else "Customer message") },
            placeholder = { Text(if (isArabic) "الصق رسالة العميل هنا..." else "Paste the customer's message here...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onDraft,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF101014)),
            shape = RoundedCornerShape(10.dp),
            enabled = !isDrafting && customerMessage.isNotBlank()
        ) {
            if (isDrafting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF101014), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isArabic) "بنجهز الرد..." else "Drafting...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isArabic) "اكتب الرد" else "Draft reply", fontWeight = FontWeight.Bold)
            }
        }

        if (draft.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, GoldBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "الرد الجاهز" else "Ready reply",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = draft, style = MaterialTheme.typography.bodyMedium, fontFamily = CairoFontFamily)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("smart_reply", draft))
                            Toast.makeText(context, if (isArabic) "اتنسخ الرد" else "Reply copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "انسخ الرد" else "Copy reply")
                    }
                }
            }
        }
    }
}
