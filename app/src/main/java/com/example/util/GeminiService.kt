package com.example.util

import android.content.Context
import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {
    private var inMemoryApiKey: String = ""

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)
        inMemoryApiKey = prefs.getString("custom_gemini_api_key", "") ?: ""
    }

    fun getEffectiveApiKey(context: Context? = null): String {
        if (inMemoryApiKey.isNotBlank()) {
            return inMemoryApiKey.trim()
        }
        if (context != null) {
            val prefs = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)
            val savedKey = prefs.getString("custom_gemini_api_key", "") ?: ""
            if (savedKey.isNotBlank()) {
                inMemoryApiKey = savedKey.trim()
                return inMemoryApiKey
            }
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    fun isKeyConfigured(context: Context? = null): Boolean {
        return getEffectiveApiKey(context).isNotBlank()
    }

    fun setCustomApiKey(context: Context, key: String) {
        inMemoryApiKey = key.trim()
        val prefs = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_gemini_api_key", inMemoryApiKey).apply()
    }

    private fun getModel38(apiKey: String): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-3.8-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 40
                topP = 0.95f
            }
        )
    }

    private fun getModelFallback(apiKey: String): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 40
                topP = 0.95f
            }
        )
    }

    private suspend fun generateContentSafe(prompt: String, apiKey: String): String {
        return try {
            val res = getModel38(apiKey).generateContent(prompt)
            res.text ?: ""
        } catch (e: Exception) {
            try {
                val resFallback = getModelFallback(apiKey).generateContent(prompt)
                resFallback.text ?: ""
            } catch (e2: Exception) {
                throw e2
            }
        }
    }

    private suspend fun generateContentSafe(content: com.google.ai.client.generativeai.type.Content, apiKey: String): String {
        return try {
            val res = getModel38(apiKey).generateContent(content)
            res.text ?: ""
        } catch (e: Exception) {
            try {
                val resFallback = getModelFallback(apiKey).generateContent(content)
                resFallback.text ?: ""
            } catch (e2: Exception) {
                throw e2
            }
        }
    }

    suspend fun extractBusinessInfo(text: String, config: com.example.ui.AIParsingConfig, context: Context? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext ""
        }

        val prompt = """
            You are a Business Intelligence AI for a bakery. Scan the following WhatsApp chat text and extract ANY useful business information.
            Look for:
            1. New Orders (Product, Qty, Date, Customer)
            2. Price Inquiries (What are they asking about?)
            3. Customer Feedback or Complaints
            4. Delivery Requests
            
            Business Context: ${config.customContext}
            
            Return a JSON object with this structure:
            {
              "orders": [ { "customerName": "...", "productName": "...", "quantity": 1, "totalRevenue": 0.0, "status": "New", "deliveryDate": "...", "notes": "..." } ],
              "inquiries": [ { "customer": "...", "topic": "...", "details": "..." } ],
              "insights": "Any general observations about what customers want right now"
            }
            
            If multiple items are found, include them all. If none, return empty lists.
            
            Chat Text:
            "$text"
        """.trimIndent()

        return@withContext try {
            val rawText = generateContentSafe(prompt, apiKey)
            rawText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateResponse(prompt: String, contextData: String, context: Context? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext "AI Business Assistant is initializing. Please try again in a moment."
        }

        val fullPrompt = """
            You are a helpful AI Business Assistant for a bakery/small business owner.
            Here is the current business data in JSON format:
            $contextData
            
            User Question: $prompt
            
            Provide a concise, professional, and helpful response. Use the data to give specific answers.
            If the data is missing or zero, suggest what the user should do next.
            Answer in the same language as the user (Hinglish/Hindi/English).
        """.trimIndent()

        return@withContext try {
            val text = generateContentSafe(fullPrompt, apiKey)
            text.ifBlank { "I'm sorry, I couldn't generate a response." }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}. Please check your internet and API key."
        }
    }

    suspend fun parseOrderFromImage(bitmap: android.graphics.Bitmap, config: com.example.ui.AIParsingConfig, context: Context? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext ""
        }

        val prompt = """
            You are an order extraction AI for a bakery. Look at this image of a handwritten bill or order note and extract the details.
            Follow these rules for extraction:
            1. Product Name: ${config.productNameRule}
            2. Quantity: ${config.quantityRule}
            3. Price/Revenue: ${config.priceRule}
            
            Business Context: ${config.customContext}
            
            Return ONLY a JSON array of objects (if multiple orders are present) or a single JSON object with these fields:
            {
              "customerName": "...",
              "productName": "...",
              "quantity": 1,
              "totalRevenue": 0.0,
              "deliveryDate": "...",
              "status": "New",
              "notes": "Handwritten bill scan"
            }
            If it's an array, return [ {...}, {...} ].
            If any field is unknown, use null or default values.
        """.trimIndent()

        return@withContext try {
            val content = com.google.ai.client.generativeai.type.content {
                image(bitmap)
                text(prompt)
            }
            val rawText = generateContentSafe(content, apiKey)
            rawText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun parseOrderFromText(text: String, config: com.example.ui.AIParsingConfig, context: Context? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext ""
        }

        val prompt = """
            You are an order extraction AI. Extract order details from this WhatsApp message based on the following rules:
            
            1. Product Name: ${config.productNameRule}
            2. Quantity: ${config.quantityRule}
            3. Price/Revenue: ${config.priceRule}
            
            Business Context: ${config.customContext}
            
            Message:
            "$text"
            
            Return ONLY a JSON object with these fields:
            {
              "customerName": "...",
              "productName": "...",
              "quantity": 1,
              "totalRevenue": 0.0,
              "deliveryDate": "e.g. Tomorrow or 25th Oct",
              "status": "e.g. Pending, New, Confirmed",
              "notes": "..."
            }
            If any field is unknown, use null or default values.
        """.trimIndent()

        return@withContext try {
            val rawText = generateContentSafe(prompt, apiKey)
            // Clean markdown if present
            rawText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
        } catch (e: Exception) {
            ""
        }
    }
}
