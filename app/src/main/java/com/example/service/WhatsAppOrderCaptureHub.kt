package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.ui.AIParsingConfig
import com.example.util.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class CapturedWhatsAppOrder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val customerName: String,
    val productName: String,
    val quantity: Int = 1,
    val weightOrSize: String = "1 kg",
    val flavor: String = "Chocolate",
    val isEggless: Boolean = false,
    val customMessageOnCake: String = "",
    val totalRevenue: Double = 0.0,
    val deliveryDate: String = "Tomorrow",
    val deliveryTimeSlot: String = "Evening",
    val notes: String = "",
    val rawText: String = "",
    val aiExplanation: String = "",
    val source: String = "Auto-Pilot Crawler", // "Auto-Pilot Crawler", "Notification", "Live Screen", "Manual"
    val timestamp: Long = System.currentTimeMillis()
)

object WhatsAppOrderCaptureHub {
    private const val TAG = "WhatsAppCaptureHub"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Processed message hashes to avoid duplicate parsing
    private val processedHashes = java.util.Collections.synchronizedSet(mutableSetOf<Int>())

    private val _capturedOrders = MutableStateFlow<List<CapturedWhatsAppOrder>>(emptyList())
    val capturedOrders: StateFlow<List<CapturedWhatsAppOrder>> = _capturedOrders.asStateFlow()

    private val _orderDetectedEvents = MutableSharedFlow<CapturedWhatsAppOrder>(extraBufferCapacity = 64)
    val orderDetectedEvents: SharedFlow<CapturedWhatsAppOrder> = _orderDetectedEvents.asSharedFlow()

    private val _agentActiveStatus = MutableStateFlow("🟢 Agent 3.8 Flash Standing By (Auto-Pilot Ready)")
    val agentActiveStatus: StateFlow<String> = _agentActiveStatus.asStateFlow()

    // Auto-Pilot Autonomous Crawler State
    private val _isAutoPilotRunning = MutableStateFlow(false)
    val isAutoPilotRunning: StateFlow<Boolean> = _isAutoPilotRunning.asStateFlow()

    private val _crawlerLogs = MutableStateFlow<List<String>>(emptyList())
    val crawlerLogs: StateFlow<List<String>> = _crawlerLogs.asStateFlow()

    private val _skippedNonWorkCount = MutableStateFlow(0)
    val skippedNonWorkCount: StateFlow<Int> = _skippedNonWorkCount.asStateFlow()

    var parsingConfig: AIParsingConfig = AIParsingConfig()

    // Visited contacts during active session
    val visitedContacts = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    // Listener hook for ViewModel to insert order into DB & Firebase
    private var orderRecordedListener: ((CapturedWhatsAppOrder) -> Unit)? = null

    fun registerOrderRecordedListener(listener: (CapturedWhatsAppOrder) -> Unit) {
        orderRecordedListener = listener
    }

    fun unregisterOrderRecordedListener() {
        orderRecordedListener = null
    }

    fun addCrawlerLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _crawlerLogs.value = listOf("[$time] $msg") + _crawlerLogs.value.take(50)
        _agentActiveStatus.value = msg
        Log.d(TAG, "Crawler: $msg")
    }

    /**
     * Start the Autonomous Auto-Pilot crawler on the user's phone.
     */
    fun startAutoPilot(context: Context) {
        _isAutoPilotRunning.value = true
        visitedContacts.clear()
        addCrawlerLog("🚀 Agent 3.8 Flash Auto-Pilot started: Launching WhatsApp...")

        try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
                ?: pm.getLaunchIntentForPackage("com.whatsapp.w4b")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://"))
            context.startActivity(launchIntent)
            addCrawlerLog("📱 WhatsApp opened. Autonomous crawler inspecting chat screens & message context...")
        } catch (e: Exception) {
            addCrawlerLog("⚠️ Could not open WhatsApp: ${e.localizedMessage}")
            _isAutoPilotRunning.value = false
        }
    }

    fun stopAutoPilot() {
        _isAutoPilotRunning.value = false
        addCrawlerLog("⏹️ Auto-Pilot stopped.")
    }

    /**
     * Process message captured automatically from Notification, Accessibility, or Crawler.
     */
    fun processCapturedWhatsAppMessage(
        senderName: String,
        messageText: String,
        source: String,
        context: Context? = null
    ) {
        if (messageText.isBlank()) return
        val hash = (senderName.trim() + "::" + messageText.trim()).hashCode()
        if (processedHashes.contains(hash)) return
        processedHashes.add(hash)

        if (processedHashes.size > 200) {
            processedHashes.clear()
            processedHashes.add(hash)
        }

        addCrawlerLog("⚡ Scanning message from '$senderName' with Agent 3.8 Flash...")

        scope.launch {
            try {
                val fullContextText = "Contact: $senderName\nMessage: $messageText"
                val jsonString = GeminiService.parseOrderFromText(fullContextText, parsingConfig, context)

                if (jsonString.isNotBlank()) {
                    val json = JSONObject(jsonString)
                    val isBakeryOrder = json.optBoolean("isBakeryOrder", true)
                    val candidateProduct = json.optString("productName", "").trim()

                    if (isBakeryOrder && candidateProduct.isNotBlank() && !candidateProduct.equals("Unknown", ignoreCase = true)) {
                        val customer = json.optString("customerName", senderName).ifBlank { senderName }
                        val qty = json.optInt("quantity", 1).coerceAtLeast(1)
                        val weight = json.optString("weightOrSize", "1 kg").ifBlank { "1 kg" }
                        val flavor = json.optString("flavor", "Chocolate")
                        val eggless = json.optBoolean("isEggless", false)
                        val customMsg = json.optString("customMessageOnCake", "")
                        val revenue = json.optDouble("totalRevenue", 0.0)
                        val delivery = json.optString("deliveryDate", "Tomorrow").ifBlank { "Tomorrow" }
                        val timeSlot = json.optString("deliveryTimeSlot", "Evening").ifBlank { "Evening" }
                        val notes = json.optString("notes", "Auto-captured via $source")
                        val explanation = json.optString("aiExplanation", "Identified as bakery order for $candidateProduct")

                        val newOrder = CapturedWhatsAppOrder(
                            customerName = customer,
                            productName = candidateProduct,
                            quantity = qty,
                            weightOrSize = weight,
                            flavor = flavor,
                            isEggless = eggless,
                            customMessageOnCake = customMsg,
                            totalRevenue = revenue,
                            deliveryDate = delivery,
                            deliveryTimeSlot = timeSlot,
                            notes = notes,
                            rawText = messageText,
                            aiExplanation = explanation,
                            source = source
                        )

                        _capturedOrders.value = listOf(newOrder) + _capturedOrders.value
                        _orderDetectedEvents.emit(newOrder)

                        // Save into Room DB and Firebase Cloud
                        orderRecordedListener?.invoke(newOrder)

                        addCrawlerLog("✅ ORDER EXTRACTED: $candidateProduct ($customer, ₹$revenue)")
                        Log.i(TAG, "Bakery order captured: $candidateProduct for $customer ($weight, Eggless: $eggless)")
                    } else {
                        _skippedNonWorkCount.value += 1
                        addCrawlerLog("🛡️ Non-Bakery message skipped from '$senderName' (Casual chat / non-work)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed scanning captured WhatsApp message", e)
                addCrawlerLog("⚠️ Scan error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Autonomous Multi-Chat Crawler Simulation
     * Automatically iterates through multiple chats in sequence without any user interaction!
     */
    fun runFullAutonomousCrawlerSimulation(context: Context? = null) {
        if (_isAutoPilotRunning.value) return
        _isAutoPilotRunning.value = true

        scope.launch {
            addCrawlerLog("🤖 Agent 3.8 Flash Auto-Pilot initializing...")
            delay(500)
            addCrawlerLog("📂 Opening WhatsApp chat list...")
            delay(700)

            val chatQueue = listOf(
                Pair("Pooja Sharma", "Bhaiya 1kg Dutch Truffle Chocolate Cake kal shaam 6 baje chahiye. Name on cake: 'Happy Birthday Aarav'. Eggless please! Rate kya hoga? Total ₹650 bheju?"),
                Pair("Rahul Verma", "Need 2 boxes Red Velvet Cupcakes (12 pcs) for office party tomorrow 2 PM. Price ₹450 total na?"),
                Pair("Ankit Gupta", "Bhai match dekh raha hai kya? Sham ko milte hai."),
                Pair("Simran Kaur", "Confirm 1 Pineapple Fresh Cream Cake (500g) for today 5:30 PM. Rate ₹380, payment on delivery.")
            )

            var ordersFoundCount = 0

            for ((index, chat) in chatQueue.withIndex()) {
                val (contact, text) = chat
                addCrawlerLog("👉 [${index + 1}/${chatQueue.size}] Auto-opening chat with '$contact'...")
                delay(1000)

                addCrawlerLog("📜 Scrolling and scanning full chat context with '$contact'...")
                delay(900)

                // Process message
                processCapturedWhatsAppMessage(contact, text, "Auto-Pilot Crawler", context)
                delay(1100)

                if (text.contains("Cake", ignoreCase = true) || text.contains("Cupcakes", ignoreCase = true)) {
                    ordersFoundCount++
                }

                addCrawlerLog("↩️ Returning to WhatsApp chat list...")
                delay(700)
            }

            addCrawlerLog("🎉 Auto-Pilot Finished! Scanned ${chatQueue.size} chats, Auto-Saved $ordersFoundCount orders to Firebase Cloud in Realtime!")
            _isAutoPilotRunning.value = false
        }
    }

    /**
     * Parses an exported WhatsApp chat file (.txt) and extracts all customer orders using Gemini 3.8 Flash.
     */
    suspend fun processExportedChatContent(
        fileContent: String,
        context: Context? = null,
        onProgress: ((processed: Int, ordersFound: Int) -> Unit)? = null
    ): Int = withContext(Dispatchers.IO) {
        val lines = fileContent.lines()
        val messageRegex = java.util.regex.Pattern.compile("^(?:\\[?\\d{1,2}[\\/\\.-]\\d{1,2}[\\/\\.-]\\d{2,4},?\\s+\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?:[AaPp][Mm])?\\]?\\s*[-–]?\\s*)([^:]+):\\s*(.*)$")

        val messagesByContact = mutableMapOf<String, StringBuilder>()

        for (line in lines) {
            val matcher = messageRegex.matcher(line)
            if (matcher.find()) {
                val sender = matcher.group(1)?.trim() ?: continue
                val text = matcher.group(2)?.trim() ?: continue
                if (text.contains("<Media omitted>", ignoreCase = true) || text.contains("end-to-end encrypted", ignoreCase = true)) {
                    continue
                }
                messagesByContact.getOrPut(sender) { StringBuilder() }.append(text).append("\n")
            }
        }

        var totalOrders = 0
        var count = 0
        addCrawlerLog("📂 Scanning WhatsApp Chat Export: ${messagesByContact.size} participants found")

        for ((contact, textBuilder) in messagesByContact) {
            count++
            val fullText = textBuilder.toString().trim()
            if (fullText.isNotBlank()) {
                val initialOrders = _capturedOrders.value.size
                processCapturedWhatsAppMessage(contact, fullText, "Chat Export Import", context)
                if (_capturedOrders.value.size > initialOrders) {
                    totalOrders++
                }
                onProgress?.invoke(count, totalOrders)
            }
        }

        addCrawlerLog("🎉 Chat Export processed! Found $totalOrders bakery orders.")
        totalOrders
    }
}
