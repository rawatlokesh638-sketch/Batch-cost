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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RecordedOrder
import com.example.util.CurrencyFormatter

data class CustomerProfile(
    val name: String,
    val phone: String,
    val totalOrders: Int,
    val totalRevenue: Double,
    val lastProduct: String,
    val lastQuantity: Int,
    val lastOrderDate: String,
    val notes: String,
    val repeatCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCrmSheet(
    recordedOrders: List<RecordedOrder>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onCreateRepeatOrder: (CustomerProfile) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dynamically aggregate customer profiles from real recorded orders
    val customers = remember(recordedOrders) {
        if (recordedOrders.isEmpty()) {
            emptyList()
        } else {
            val grouped = recordedOrders.groupBy { it.customerName.trim().ifBlank { "Direct Walk-in" } }
            grouped.map { (name, orders) ->
                val sorted = orders.sortedByDescending { it.id }
                val latest = sorted.first()
                CustomerProfile(
                    name = name,
                    phone = "WhatsApp Contact",
                    totalOrders = orders.size,
                    totalRevenue = orders.sumOf { it.totalRevenue },
                    lastProduct = latest.productName,
                    lastQuantity = latest.quantity,
                    lastOrderDate = latest.date.ifBlank { latest.deliveryDate },
                    notes = if (latest.customMessageOnCake.isNotBlank()) "Note: '${latest.customMessageOnCake}'" else if (latest.isEggless) "Prefers Eggless" else "Regular Customer",
                    repeatCount = orders.size
                )
            }.sortedByDescending { it.totalOrders }
        }
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
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("👥 Customer CRM & Repeat Orders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Track customer history, preferences & loyalty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (customers.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PeopleAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text("No Customer History Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "As orders are captured from WhatsApp or recorded manually, customer order history and repeat patterns will appear here in real-time.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("customer_crm_list")
                ) {
                    items(customers) { customer ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(customer.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Spent", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            CurrencyFormatter.format(customer.totalRevenue, currencySymbol),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857),
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                    ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Repeat, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "🔁 ${customer.name} has placed ${customer.totalOrders} total order(s).",
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Last Order", style = MaterialTheme.typography.labelSmall)
                                        Text("${customer.lastProduct} ×${customer.lastQuantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Date: ${customer.lastOrderDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Notes", style = MaterialTheme.typography.labelSmall)
                                        Text(customer.notes, fontSize = 12.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                                    }
                                }

                                Button(
                                    onClick = { onCreateRepeatOrder(customer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("create_repeat_order_${customer.name.take(3)}")
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Repeat Order for ${customer.name.substringBefore(" ")}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

