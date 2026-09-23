package com.example.ui.screens.whatsapp

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.GeminiService
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppIntegrationScreen(
    onBack: () -> Unit,
    parsingConfig: com.example.ui.AIParsingConfig,
    onImportOrder: (customerName: String, productName: String, qty: Int, revenue: Double, cost: Double, status: String, deliveryDate: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var extractedText by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Smart Link") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                // Inject JS to grab visible chat text
                                webView?.evaluateJavascript(
                                    "(function() { " +
                                    "  const messages = Array.from(document.querySelectorAll('.message-in, .message-out'))" +
                                    "    .map(m => m.innerText)" +
                                    "    .join('\\n'); " +
                                    "  return messages; " +
                                    "})();"
                                ) { result ->
                                    if (!result.isNullOrBlank() && result != "null" && result != "\"\"") {
                                        val cleaned = try {
                                            JSONObject("{ \"text\": $result }").getString("text")
                                        } catch (e: Exception) {
                                            result.trim().removePrefix("\"").removeSuffix("\"")
                                        }
                                        extractedText = cleaned
                                    }
                                    isImporting = true
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Smart Import")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    "💡 Log in to WhatsApp Web below. Copy any order message, then click 'Smart Import' to automatically record it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            
                            // Essential for WhatsApp Web on mobile
                            userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
                        }
                        loadUrl("https://web.whatsapp.com")
                        webView = this
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }

    if (isImporting) {
        var pasteText by remember { mutableStateOf(extractedText ?: "") }
        var isProcessing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { isImporting = false },
            title = { Text("AI Order Extractor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste the WhatsApp message here:")
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("e.g. Hi, I need 2 Chocolate Cakes for tomorrow...") }
                    )
                    if (isProcessing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            val jsonString = GeminiService.parseOrderFromText(pasteText, parsingConfig)
                            try {
                                val json = JSONObject(jsonString)
                                val customer = json.optString("customerName", "Unknown")
                                val product = json.optString("productName", "Unknown")
                                val qty = json.optInt("quantity", 1)
                                val rev = json.optDouble("totalRevenue", 0.0)
                                val status = json.optString("status", "Pending")
                                val deliveryDate = json.optString("deliveryDate", "Tomorrow")
                                val notes = json.optString("notes", "")
                                
                                onImportOrder(customer, product, qty, rev, 0.0, status, deliveryDate, notes)
                                isImporting = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Order recorded for $customer")
                                }
                            } catch (e: Exception) {
                                // Handle error
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    enabled = pasteText.isNotBlank() && !isProcessing
                ) {
                    Text("Extract with AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { isImporting = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
