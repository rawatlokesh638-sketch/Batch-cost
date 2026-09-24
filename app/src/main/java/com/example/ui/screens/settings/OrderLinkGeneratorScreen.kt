package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.entity.BusinessProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderLinkGeneratorScreen(
    profile: BusinessProfileEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var customTemplate by remember { mutableStateOf("Hi! I'd like to place an order:\n\nName:\nProduct:\nQuantity:\nDelivery Date:\nNotes:") }
    
    val phone = profile.phone.filter { it.isDigit() }
    val encodedText = Uri.encode(customTemplate)
    val whatsappUrl = "https://wa.me/$phone?text=$encodedText"
    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${Uri.encode(whatsappUrl)}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Order Link") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Generate a link and QR code for your customers. They can fill their details and send them directly to your WhatsApp.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Custom QR Code", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = qrCodeUrl,
                        contentDescription = "WhatsApp Order QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Scan to Order", style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = customTemplate,
                onValueChange = { customTemplate = it },
                label = { Text("WhatsApp Message Template") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Place your order here: $whatsappUrl")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Order Link"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share Link")
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(whatsappUrl))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy Link")
                }
            }
            
            Text(
                "Tip: Print this QR code and paste it at your shop counter. Customers can just scan and send their order!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
