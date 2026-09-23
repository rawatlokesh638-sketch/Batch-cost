package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterIngredientEntity
import com.example.ui.ProductWithDetails
import com.example.ui.RecordedOrder

import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.GeminiService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val text: String,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIBusinessAssistantSheet(
    products: List<ProductWithDetails>,
    masterIngredients: List<MasterIngredientEntity>,
    recordedOrders: List<RecordedOrder>,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val messages = remember {
        mutableStateListOf(
            ChatMessage("AI", "👋 Hello! I am your AI Business & Profit Assistant. I can analyze your recipes, ingredient costs, and orders. Ask me anything!")
        )
    }

    fun answerQuery(query: String) {
        if (query.isBlank()) return
        messages.add(ChatMessage("User", query))
        
        // Add loading message
        val loadingMsg = ChatMessage("AI", "...", isLoading = true)
        messages.add(loadingMsg)

        scope.launch {
            // Prepare context data
            val contextData = buildJsonObject {
                put("currency", currencySymbol)
                put("products_count", products.size)
                put("orders_count", recordedOrders.size)
                put("total_revenue", recordedOrders.sumOf { it.totalRevenue })
                put("total_cost", recordedOrders.sumOf { it.totalCost })
                
                val productList = buildJsonArray {
                    products.forEach { p ->
                        add(buildJsonObject {
                            put("name", p.product.name)
                            put("price", p.product.sellingPrice)
                            put("cost", p.totalCost)
                            put("margin", if(p.product.sellingPrice > 0) ((p.product.sellingPrice - p.totalCost)/p.product.sellingPrice)*100 else 0.0)
                        })
                    }
                }
                put("products", productList)
            }.toString()

            val response = GeminiService.generateResponse(query, contextData)
            
            // Remove loading and add real response
            messages.remove(loadingMsg)
            messages.add(ChatMessage("AI", response))
        }
        inputQuery = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("💬 AI Business Assistant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Answers derived directly from your store data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Quick Prompt Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Suggested Questions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                listOf(
                    "What is my business profit margin?",
                    "How many orders did I fulfill?",
                    "Tell me about my product catalog."
                ).forEach { prompt ->
                    FilterChip(
                        selected = false,
                        onClick = { answerQuery(prompt) },
                        label = { Text("✨ $prompt", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Chat Messages History
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("ai_chat_messages_list")
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "User"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Text Query Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask about your profits, costs, or recipes...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_query_input"),
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = { answerQuery(inputQuery) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
