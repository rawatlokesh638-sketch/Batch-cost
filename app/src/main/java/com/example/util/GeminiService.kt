package com.example.util

import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
            }
        )
    }

    suspend fun generateResponse(prompt: String, contextData: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
            return@withContext "AI Assistant is not fully configured. Please ensure your GEMINI_API_KEY is set in the Secrets panel."
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
            val response = model.generateContent(fullPrompt)
            response.text ?: "I'm sorry, I couldn't generate a response."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}. Please check your internet and API key."
        }
    }

    suspend fun parseOrderFromText(text: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
            return@withContext ""
        }

        val prompt = """
            Extract order details from this WhatsApp message:
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
            val response = model.generateContent(prompt)
            val rawText = response.text ?: ""
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
