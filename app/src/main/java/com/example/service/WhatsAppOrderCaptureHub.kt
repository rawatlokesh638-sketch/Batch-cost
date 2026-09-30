package com.example.service

import android.content.Context
import android.util.Log
import com.example.ui.AIParsingConfig
import com.example.util.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    val source: String = "WhatsApp Direct Share", // "WhatsApp Direct Share", "Cloud API Webhook", "Chat Export", "Manual"
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

    private val _agentActiveStatus = MutableStateFlow("🟢 WhatsApp Order Engine Ready (Cloud API & Direct Share)")
    val agentActiveStatus: StateFlow<String> = _agentActiveStatus.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    var parsingConfig: AIParsingConfig = AIParsingConfig()

    // Listener hook for ViewModel to insert order into DB & Firebase
    private var orderRecordedListener: ((CapturedWhatsAppOrder) -> Unit)? = null

    fun registerOrderRecordedListener(listener: (CapturedWhatsAppOrder) -> Unit) {
        orderRecordedListener = listener
    }

    fun unregisterOrderRecordedListener() {
        orderRecordedListener = null
    }

    fun addSyncLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _syncLogs.value = listOf("[$time] $msg") + _syncLogs.value.take(50)
        _agentActiveStatus.value = msg
        Log.d(TAG, "Sync: $msg")
    }

    /**
     * Process message captured via Official Cloud API, Direct Share, or Text Paste
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

        addSyncLog("⚡ Parsing incoming message from '$senderName' ($source)...")

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
                        val notes = json.optString("notes", "Captured via $source")
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

                        // Show system notification to user
                        context?.let { ctx ->
                            NotificationHelper.showOrderDetectedNotification(
                                context = ctx,
                                customerName = customer,
                                productName = candidateProduct,
                                amount = revenue,
                                deliveryDate = delivery
                            )
                        }

                        addSyncLog("✅ ORDER EXTRACTED: $candidateProduct ($customer, ₹$revenue)")
                        Log.i(TAG, "Bakery order captured: $candidateProduct for $customer ($weight, Eggless: $eggless)")
                    } else {
                        addSyncLog("🛡️ Non-Bakery message skipped from '$senderName' (No active order items found)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed parsing incoming WhatsApp message", e)
                addSyncLog("⚠️ Parsing error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Parses an exported WhatsApp chat file (.txt) and extracts all customer orders using Gemini.
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
        addSyncLog("📂 Scanning WhatsApp Chat Export: ${messagesByContact.size} contacts found")

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

        addSyncLog("🎉 Chat Export processed! Found $totalOrders bakery orders.")
        totalOrders
    }

    /**
     * Parse Official WhatsApp Business Cloud API incoming Webhook JSON payload
     */
    fun processCloudApiWebhookPayload(payloadJson: String, context: Context? = null): Boolean {
        return try {
            val root = JSONObject(payloadJson)
            val entry = root.optJSONArray("entry")?.optJSONObject(0) ?: return false
            val changes = entry.optJSONArray("changes")?.optJSONObject(0) ?: return false
            val value = changes.optJSONObject("value") ?: return false
            val contacts = value.optJSONArray("contacts")
            val contactName = contacts?.optJSONObject(0)?.optJSONObject("profile")?.optString("name", "WhatsApp Customer") ?: "WhatsApp Customer"
            
            val messages = value.optJSONArray("messages") ?: return false
            if (messages.length() == 0) return false
            
            val messageObj = messages.optJSONObject(0) ?: return false
            val msgType = messageObj.optString("type", "text")
            val messageBody = if (msgType == "text") {
                messageObj.optJSONObject("text")?.optString("body", "") ?: ""
            } else {
                messageObj.optString("text", "")
            }
            
            if (messageBody.isNotBlank()) {
                processCapturedWhatsAppMessage(
                    senderName = contactName,
                    messageText = messageBody,
                    source = "WhatsApp Cloud API",
                    context = context
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Cloud API webhook", e)
            false
        }
    }
}
