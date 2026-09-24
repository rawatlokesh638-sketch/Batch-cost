package com.example.ui.screens.whatsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("WhatsApp Orders Link", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Web / Direct Chat Scanner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLoading = true
                            webView?.reload()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload WhatsApp")
                    }

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("whatsapp://send")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("WhatsApp app not installed or could not be opened.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open WhatsApp App")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isImporting = true
                                isLoading = true
                                // Inject JS to grab visible chat text and try to find active contact name
                                webView?.evaluateJavascript(
                                    "(function() { " +
                                    "  try { " +
                                    "    const messages = Array.from(document.querySelectorAll('.message-in, .message-out, div[data-pre-plain-text]'))" +
                                    "      .map(m => m.innerText)" +
                                    "      .join('\\n'); " +
                                    "    const activeContact = document.querySelector('header span[title]')?.getAttribute('title') || " +
                                    "                          document.querySelector('._amig')?.innerText || 'WhatsApp Customer'; " +
                                    "    return JSON.stringify({ messages: messages, contact: activeContact }); " +
                                    "  } catch(e) { return JSON.stringify({ messages: '', contact: '' }); } " +
                                    "})();"
                                ) { result ->
                                    try {
                                        val clean = result.removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"").replace("\\n", "\n")
                                        val json = JSONObject(clean)
                                        val text = json.optString("messages")
                                        val contact = json.optString("contact")
                                        
                                        if (text.isNotBlank()) {
                                            extractedText = "Contact: $contact\n\n$text"
                                        }
                                    } catch (e: Exception) {
                                        if (result.isNotBlank() && result != "\"null\"") {
                                            extractedText = result.trim().removePrefix("\"").removeSuffix("\"")
                                        }
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
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "1. Scan QR code to link device • 2. Open chat • 3. Tap 'Auto-Scan'",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            isImporting = true
                            extractedText = ""
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Paste Text", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        // Cookie configuration for WhatsApp Web session persistence
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                if (newProgress >= 85) {
                                    isLoading = false
                                }
                            }

                            override fun onPermissionRequest(request: PermissionRequest?) {
                                request?.grant(request.resources)
                            }
                        }

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
                            @SuppressLint("SetJavaScriptEnabled")
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
                            setSupportMultipleWindows(false)
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // Standard Desktop Chrome User Agent for reliable WhatsApp Web connection
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
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
                    Text("WhatsApp chat message or order details:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        placeholder = { Text("e.g. Hello, I need 2 Chocolate Truffle Cakes (1kg each) for tomorrow evening. Contact: Rahul") }
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
                            val resultJson = GeminiService.extractBusinessInfo(pasteText, parsingConfig, context)
                            try {
                                val json = JSONObject(resultJson)
                                val orders = json.optJSONArray("orders")
                                if (orders != null && orders.length() > 0) {
                                    for (i in 0 until orders.length()) {
                                        val o = orders.getJSONObject(i)
                                        val customer = o.optString("customer_name", "WhatsApp Customer")
                                        val product = o.optString("product_name", "Uncategorized Item")
                                        val qty = o.optInt("quantity", 1)
                                        val revenue = o.optDouble("total_price", 0.0)
                                        val cost = o.optDouble("estimated_cost", 0.0)
                                        val status = o.optString("status", "Confirmed")
                                        val delivery = o.optString("delivery_date", "Tomorrow")
                                        val notes = o.optString("notes", "")

                                        onImportOrder(customer, product, qty, revenue, cost, status, delivery, notes)
                                    }
                                } else {
                                    val customer = json.optString("customer_name", "WhatsApp Customer")
                                    val product = json.optString("product_name", "Bakery Order")
                                    val qty = json.optInt("quantity", 1)
                                    val revenue = json.optDouble("total_price", 0.0)
                                    val cost = json.optDouble("estimated_cost", 0.0)
                                    val status = json.optString("status", "Confirmed")
                                    val delivery = json.optString("delivery_date", "Tomorrow")
                                    val notes = json.optString("notes", "")

                                    onImportOrder(customer, product, qty, revenue, cost, status, delivery, notes)
                                }
                                isImporting = false
                            } catch (e: Exception) {
                                // Fallback order if JSON is simple
                                onImportOrder("WhatsApp Customer", pasteText.take(20), 1, 0.0, 0.0, "Pending", "Tomorrow", pasteText)
                                isImporting = false
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    enabled = pasteText.isNotBlank() && !isProcessing
                ) {
                    Text("Extract Order", fontWeight = FontWeight.Bold)
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
