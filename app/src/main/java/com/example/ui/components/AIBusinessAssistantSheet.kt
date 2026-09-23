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

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val text: String
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

    val messages = remember {
        mutableStateListOf(
            ChatMessage("AI", "👋 Hello! I am your AI Business & Profit Assistant. I can analyze your recipes, ingredient costs, and orders. Ask me anything!")
        )
    }

    fun answerQuery(query: String) {
        if (query.isBlank()) return
        messages.add(ChatMessage("User", query))
        val lower = query.lowercase()

        val response = when {
            lower.contains("profit") || lower.contains("margin") || lower.contains("performance") -> {
                if (recordedOrders.isEmpty()) {
                    "📊 **Profit Analysis**: You haven't recorded any orders yet. Once you start recording orders and production batches, I can analyze your profit margins and cost trends."
                } else {
                    val totalRev = recordedOrders.sumOf { it.totalRevenue }
                    val totalCost = recordedOrders.sumOf { it.totalCost }
                    val totalProfit = totalRev - totalCost
                    val margin = if (totalRev > 0) (totalProfit / totalRev) * 100 else 0.0
                    
                    val performance = recordedOrders.groupBy { it.productName }
                        .mapValues { (_, list) -> 
                            val rev = list.sumOf { it.totalRevenue }
                            val cost = list.sumOf { it.totalCost }
                            val prof = rev - cost
                            val mar = if (rev > 0) (prof / rev) * 100 else 0.0
                            mar
                        }
                        .toList()
                        .sortedByDescending { it.second }

                    val topProduct = performance.firstOrNull()
                    
                    "🏆 **Business Performance**:\n- Total Revenue: $currencySymbol${String.format("%.0f", totalRev)}\n- Net Profit: $currencySymbol${String.format("%.0f", totalProfit)}\n- Average Margin: ${String.format("%.1f", margin)}%\n\n" +
                    (if (topProduct != null) "💡 **Top Performer**: ${topProduct.first} has your highest margin at ${String.format("%.1f", topProduct.second)}%." else "")
                }
            }
            lower.contains("order") || lower.contains("revenue") || lower.contains("sale") -> {
                val totalRev = recordedOrders.sumOf { it.totalRevenue }
                val count = recordedOrders.size
                "📊 **Order Summary**:\nYou have recorded **$count orders** with a total revenue of **$currencySymbol${String.format("%.0f", totalRev)}**."
            }
            lower.contains("ingredient") || lower.contains("cost") || lower.contains("recipe") -> {
                if (products.isEmpty()) {
                    "🧑‍🍳 **Recipe Insights**: You haven't added any products yet. Add your first product recipe in the Catalog to see cost breakdowns."
                } else {
                    val avgCost = products.map { it.totalCost }.average()
                    "💡 **Catalog Insights**:\nYou have **${products.size} products** in your catalog.\nAverage cost per unit across all recipes: **$currencySymbol${String.format("%.2f", avgCost)}**."
                }
            }
            else -> {
                "💡 **Business Assistant Tip**:\nI can help you analyze your margins, track orders, and understand your recipe costs. Try asking: 'What is my total revenue?' or 'Which product is most profitable?'"
            }
        }

        messages.add(ChatMessage("AI", response))
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
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (isUser) Color.White else Color.Black,
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
