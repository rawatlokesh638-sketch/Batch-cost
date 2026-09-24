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
import com.example.BuildConfig
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
    var isLoading by remember { mutableStateOf(true) }

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
                    if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
                        TextButton(onClick = { /* Could show info */ }) {
                            Text("Missing AI Key", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    isImporting = true
                                    isLoading = true
                                    // Inject JS to grab visible chat text and try to find active contact name
                                    webView?.evaluateJavascript(
                                        "(function() { " +
                                        "  const messages = Array.from(document.querySelectorAll('.message-in, .message-out'))" +
                                        "    .map(m => m.innerText)" +
                                        "    .join('\\n'); " +
                                        "  const activeContact = document.querySelector('._amig')?.innerText || 'Unknown'; " +
                                        "  return JSON.stringify({ messages: messages, contact: activeContact }); " +
                                        "})();"
                                    ) { result ->
                                        try {
                                            val json = JSONObject(result.removePrefix("\"").removeSuffix("\"").replace("\\\"", "\""))
                                            val text = json.optString("messages")
                                            val contact = json.optString("contact")
                                            
                                            if (text.isNotBlank()) {
                                                extractedText = "Contact: $contact\n\n$text"
                                            }
                                        } catch (e: Exception) {
                                            extractedText = result.trim().removePrefix("\"").removeSuffix("\"")
                                        }
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Auto-Scan Chat")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    "💡 Login to WhatsApp. Once logged in, open a chat and click 'Auto-Scan Chat' to automatically pull orders into the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                isLoading = false
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = true
                            allowContentAccess = true
                            
                            // Highly compatible Desktop UA to bypass mobile blocking
                            userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
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
                            val resultJson = GeminiService.extractBusinessInfo(pasteText, parsingConfig)
                            try {
                                val root = JSONObject(resultJson)
                                
                                // Handle Orders
                                val ordersArray = root.optJSONArray("orders")
                                if (ordersArray != null) {
                                    for (i in 0 until ordersArray.length()) {
                                        val json = ordersArray.getJSONObject(i)
                                        val customer = json.optString("customerName", "Unknown")
                                        val product = json.optString("productName", "Unknown")
                                        val qty = json.optInt("quantity", 1)
                                        val rev = json.optDouble("totalRevenue", 0.0)
                                        val status = json.optString("status", "Pending")
                                        val deliveryDate = json.optString("deliveryDate", "Tomorrow")
                                        val notes = json.optString("notes", "")
                                        onImportOrder(customer, product, qty, rev, 0.0, status, deliveryDate, notes)
                                    }
                                }

                                // Handle Insights
                                val insights = root.optString("insights")
                                if (insights.isNotBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("AI Insight: $insights", duration = SnackbarDuration.Long)
                                    }
                                }

                                isImporting = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Extraction complete!")
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
