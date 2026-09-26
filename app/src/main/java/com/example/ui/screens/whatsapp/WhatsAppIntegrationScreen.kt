package com.example.ui.screens.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.CapturedWhatsAppOrder
import com.example.service.WhatsAppOrderCaptureHub
import com.example.ui.AIParsingConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppIntegrationScreen(
    onBack: () -> Unit,
    parsingConfig: AIParsingConfig,
    initialSharedText: String? = null,
    onImportOrder: (customerName: String, productName: String, qty: Int, revenue: Double, cost: Double, status: String, deliveryDate: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val capturedOrders by WhatsAppOrderCaptureHub.capturedOrders.collectAsStateWithLifecycle()
    val agentStatus by WhatsAppOrderCaptureHub.agentActiveStatus.collectAsStateWithLifecycle()
    val isAutoPilotRunning by WhatsAppOrderCaptureHub.isAutoPilotRunning.collectAsStateWithLifecycle()
    val crawlerLogs by WhatsAppOrderCaptureHub.crawlerLogs.collectAsStateWithLifecycle()
    val skippedNonWorkCount by WhatsAppOrderCaptureHub.skippedNonWorkCount.collectAsStateWithLifecycle()

    var manualChatText by remember { mutableStateOf("") }
    var selectedOrderForDetails by remember { mutableStateOf<CapturedWhatsAppOrder?>(null) }

    LaunchedEffect(parsingConfig) {
        WhatsAppOrderCaptureHub.parsingConfig = parsingConfig
    }

    LaunchedEffect(initialSharedText) {
        if (!initialSharedText.isNullOrBlank()) {
            WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage(
                senderName = "Shared Contact",
                messageText = initialSharedText,
                source = "Direct Share Sheet",
                context = context
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Autonomous WhatsApp Agent", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Agent 3.8 Flash • Auto-Pilot Chat Scanner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            WhatsAppOrderCaptureHub.runFullAutonomousCrawlerSimulation(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("🤖 Autonomous Crawler started!")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Quick Test", tint = Color(0xFFF59E0B))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (isAutoPilotRunning) Color(0xFF10B981) else MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAutoPilotRunning) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Agent 3.8 Flash", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = if (isAutoPilotRunning) Color(0xFF10B981) else Color(0xFF6B7280),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (isAutoPilotRunning) "SCANNING CHATS" else "STANDBY",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(agentStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Master Auto-Pilot Launcher Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F5132)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Autonomous Auto-Pilot Scanner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Koi screen copy karne ki jaroorat nahi", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Agent active hokar apne aap jaldi-jaldi WhatsApp ke sabhi customer chats open karega, cake & bakery orders extract karega aur Order Book me Firebase par save kar dega!",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    if (!isAutoPilotRunning) {
                        Button(
                            onClick = {
                                WhatsAppOrderCaptureHub.startAutoPilot(context)
                                scope.launch {
                                    snackbarHostState.showSnackbar("🚀 Agent 3.8 Flash launching WhatsApp crawler!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_autopilot_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("🚀 START WHATSAPP AUTO-PILOT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                WhatsAppOrderCaptureHub.stopAutoPilot()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("⏹️ STOP AUTO-PILOT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            WhatsAppOrderCaptureHub.runFullAutonomousCrawlerSimulation(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("🤖 Simulating multi-chat auto-scan...")
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("⚡ Run Multi-Chat Auto-Scan (Direct Live Simulation)", fontSize = 13.sp)
                    }
                }
            }

            // Real-Time Crawler Terminal Logs
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Agent 3.8 Flash Activity Terminal", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        if (isAutoPilotRunning) {
                            Text("LIVE", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        if (crawlerLogs.isEmpty()) {
                            Text(
                                "No active crawling session. Click 'Start WhatsApp Auto-Pilot' or 'Run Multi-Chat Auto-Scan' above.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                crawlerLogs.take(6).forEach { logLine ->
                                    Text(
                                        logLine,
                                        color = if (logLine.contains("SAVED")) Color(0xFF34D399) else Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3-Way Zero Permission Integration Suite
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("3 Direct Zero-Permission Channels", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // 1. Direct Share
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("1️⃣ 1-Tap Direct WhatsApp Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(Modifier.weight(1f))
                                Surface(color = Color(0xFF10B981).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                    Text("Official & Safe", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("WhatsApp me customer ka message ya screenshot select karein ➔ Share (➦) ➔ 'BatchCost' choose karein. Gemini 3.8 Flash turant extract kar lega!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val pm = context.packageManager
                                    val intent = pm.getLaunchIntentForPackage("com.whatsapp")
                                        ?: pm.getLaunchIntentForPackage("com.whatsapp.w4b")
                                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar("WhatsApp is not installed on this device.") }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open WhatsApp to Share Message", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // 2. Clipboard Smart Bar
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("2️⃣ Auto-Clipboard Smart Detector", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(Modifier.weight(1f))
                                Text(if (clipText.isNotBlank()) "Copied Text Ready" else "Empty", fontSize = 10.sp, color = if (clipText.isNotBlank()) Color(0xFF10B981) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            if (clipText.isNotBlank()) {
                                Text("\"${clipText.take(90)}${if (clipText.length > 90) "..." else ""}\"", fontSize = 11.sp, color = Color(0xFF334155), maxLines = 2)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage(
                                            senderName = "Clipboard Customer",
                                            messageText = clipText,
                                            source = "Clipboard Auto-Bar",
                                            context = context
                                        )
                                        scope.launch { snackbarHostState.showSnackbar("⚡ Scanned with Gemini 3.8 Flash & synced to Firebase!") }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                ) {
                                    Icon(Icons.Default.ContentPasteGo, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("⚡ Scan Clipboard Text Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("WhatsApp me koi bhi order message Copy karein aur yahan aakar 1-Tap me scan karein.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 3. WhatsApp Full Chat Export File Importer (.txt)
                    var isImportingChat by remember { mutableStateOf(false) }
                    var importStatusText by remember { mutableStateOf("") }
                    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            scope.launch {
                                try {
                                    isImportingChat = true
                                    importStatusText = "Reading exported chat file..."
                                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                                        stream.bufferedReader().use { it.readText() }
                                    } ?: ""
                                    if (content.isNotBlank()) {
                                        importStatusText = "Analyzing orders with Gemini 3.8 Flash..."
                                        val totalFound = WhatsAppOrderCaptureHub.processExportedChatContent(
                                            fileContent = content,
                                            context = context,
                                            onProgress = { processed, orders ->
                                                importStatusText = "Scanned $processed chats, found $orders bakery orders..."
                                            }
                                        )
                                        snackbarHostState.showSnackbar("🎉 Finished! Imported $totalFound orders to Firebase RTDB!")
                                    } else {
                                        snackbarHostState.showSnackbar("Could not read selected file.")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Import failed: ${e.localizedMessage}")
                                } finally {
                                    isImportingChat = false
                                    importStatusText = ""
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("3️⃣ WhatsApp Full Chat Export (.txt) Importer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("WhatsApp me Chat kholein ➔ 3 dots ➔ More ➔ Export Chat (Without Media) ➔ Save .txt file. Yahan import karke saare orders ek baar me nikal lein!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch("text/*")
                                },
                                enabled = !isImportingChat,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                if (isImportingChat) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(importStatusText, fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.DriveFolderUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("📂 Pick WhatsApp Chat (.txt) File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Live Captured & Saved Orders List
            AnimatedVisibility(visible = capturedOrders.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📦 Auto-Saved Orders (${capturedOrders.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (skippedNonWorkCount > 0) {
                                Surface(
                                    color = Color(0xFF64748B).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("🛡️ $skippedNonWorkCount Non-Work Skipped", color = Color(0xFF475569), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            }
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("🟢 Realtime RTDB Synced", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }

                    capturedOrders.forEach { order ->
                        Card(
                            onClick = { selectedOrderForDetails = order },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (order.isEggless) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (order.isEggless) "🌱" else "🍰", fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(order.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Customer: ${order.customerName} • ${order.weightOrSize}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${order.totalRevenue}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("Tap for details 🔍", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (order.isEggless) {
                                            Surface(color = Color(0xFF10B981).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                Text("Eggless", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                        if (order.customMessageOnCake.isNotBlank()) {
                                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                                Text("✍️ '${order.customMessageOnCake}'", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("📅 ${order.deliveryDate} (${order.deliveryTimeSlot})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Order Details Inspection Modal
            selectedOrderForDetails?.let { detailOrder ->
                AlertDialog(
                    onDismissRequest = { selectedOrderForDetails = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔍 Order Intelligence Breakdown", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section 1: Kya aaya tha WhatsApp pe
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F7E1)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("💬 Kya aaya tha WhatsApp pe (Raw Message):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                    Spacer(Modifier.height(4.dp))
                                    Text("\"${detailOrder.rawText}\"", fontSize = 13.sp, color = Color(0xFF14532D))
                                    Spacer(Modifier.height(4.dp))
                                    Text("From: ${detailOrder.customerName} • Source: ${detailOrder.source}", fontSize = 11.sp, color = Color(0xFF15803D))
                                }
                            }

                            // Section 2: AI ne kaise samjha (Reasoning)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🧠 AI Decision & Extraction Logic:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(4.dp))
                                    Text(detailOrder.aiExplanation.ifBlank { "Automatically detected as bakery cake order. Extracted item name, weight, eggless requirement, and delivery schedule from WhatsApp chat." }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Section 3: Extracted Specs Table
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("📋 Extracted Specifications:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    HorizontalDivider()
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Product:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text(detailOrder.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Weight / Qty:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text("${detailOrder.weightOrSize} (${detailOrder.quantity}x)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Flavor / Type:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text(detailOrder.flavor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Eggless Preference:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text(if (detailOrder.isEggless) "🌱 100% Eggless" else "🥚 Contains Egg", fontWeight = FontWeight.Bold, color = if (detailOrder.isEggless) Color(0xFF10B981) else Color(0xFFD97706), fontSize = 12.sp)
                                    }
                                    if (detailOrder.customMessageOnCake.isNotBlank()) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Name on Cake:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                            Text("\"${detailOrder.customMessageOnCake}\"", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Delivery Schedule:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text("${detailOrder.deliveryDate} at ${detailOrder.deliveryTimeSlot}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Quoted Price:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text("₹${detailOrder.totalRevenue}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Cloud Status:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Text("🟢 Synced to Firebase RTDB", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { selectedOrderForDetails = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done")
                        }
                    }
                )
            }

            // Quick Chat Message Tester
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💬 Test Custom Message Directly:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = manualChatText,
                        onValueChange = { manualChatText = it },
                        placeholder = { Text("e.g. 'Rahul: 1kg butterscotch cake for tonight 8 PM'") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (manualChatText.isNotBlank()) {
                                WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage("Customer", manualChatText, "Manual Test", context)
                                manualChatText = ""
                            }
                        },
                        enabled = manualChatText.isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🤖 Scan Chat")
                    }
                }
            }
        }
    }
}
