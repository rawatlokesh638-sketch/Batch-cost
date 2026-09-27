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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterIngredientEntity
import com.example.ui.RecordedOrder

data class BusinessReminder(
    val title: String,
    val description: String,
    val type: ReminderType,
    val timeAgo: String
)

enum class ReminderType {
    PENDING_ORDER, DELIVERY_TODAY, LOW_INVENTORY, EXPIRING_INGREDIENT, COST_CHANGED
}

@Composable
fun RemindersDialog(
    recordedOrders: List<RecordedOrder> = emptyList(),
    masterIngredients: List<MasterIngredientEntity> = emptyList(),
    currencySymbol: String = "₹",
    onDismiss: () -> Unit
) {
    val reminders = remember(recordedOrders, masterIngredients, currencySymbol) {
        val list = mutableListOf<BusinessReminder>()

        // 1. Pending orders
        val pendingOrders = recordedOrders.filter { it.status.equals("Pending", ignoreCase = true) || it.status.equals("Confirmed", ignoreCase = true) }
        if (pendingOrders.isNotEmpty()) {
            val totalRev = pendingOrders.sumOf { it.totalRevenue }
            list.add(
                BusinessReminder(
                    title = "📦 Active Orders Pending Fulfillment",
                    description = "${pendingOrders.size} order(s) active worth $currencySymbol${String.format("%.0f", totalRev)}: ${pendingOrders.take(2).joinToString { "${it.customerName} (${it.productName})" }}${if (pendingOrders.size > 2) "..." else ""}",
                    type = ReminderType.PENDING_ORDER,
                    timeAgo = "Real-time"
                )
            )
        }

        // 2. Delivery scheduled today / tomorrow
        val urgentDeliveries = recordedOrders.filter {
            it.deliveryDate.contains("Today", ignoreCase = true) || it.deliveryDate.contains("Tomorrow", ignoreCase = true)
        }
        if (urgentDeliveries.isNotEmpty()) {
            list.add(
                BusinessReminder(
                    title = "🚚 Urgent Deliveries Scheduled",
                    description = "${urgentDeliveries.size} cake/bakery order(s) scheduled for delivery soon: ${urgentDeliveries.take(2).joinToString { "${it.productName} (${it.deliveryDate})" }}",
                    type = ReminderType.DELIVERY_TODAY,
                    timeAgo = "Schedule"
                )
            )
        }

        // 3. Pantry Ingredients requiring attention
        if (masterIngredients.isNotEmpty()) {
            val lowStock = masterIngredients.filter { it.purchaseQty <= 1.0 }
            if (lowStock.isNotEmpty()) {
                list.add(
                    BusinessReminder(
                        title = "⚠️ Pantry Stock Alert",
                        description = "${lowStock.size} ingredient(s) in pantry: ${lowStock.take(3).joinToString { "${it.name} (${it.purchaseQty} ${it.purchaseUnit})" }}",
                        type = ReminderType.LOW_INVENTORY,
                        timeAgo = "Pantry Status"
                    )
                )
            }
        }

        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔔 Smart Operational Reminders", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_dialog")
            ) {
                Text("Real-time live operations & orders alerts:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (reminders.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).background(Color(0xFF22C55E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Text("All Clear! No Urgent Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF15803D))
                            Text("Your orders and bakery operations are up to date in real-time.", fontSize = 12.sp, color = Color(0xFF166534), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(reminders) { reminder ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (reminder.type) {
                                        ReminderType.LOW_INVENTORY, ReminderType.COST_CHANGED -> Color(0xFFFEF2F2)
                                        ReminderType.PENDING_ORDER, ReminderType.DELIVERY_TODAY -> Color(0xFFEFF6FF)
                                        else -> Color(0xFFF8FAFC)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        when (reminder.type) {
                                            ReminderType.LOW_INVENTORY, ReminderType.COST_CHANGED -> Color(0xFFEF4444)
                                            ReminderType.PENDING_ORDER, ReminderType.DELIVERY_TODAY -> Color(0xFF3B82F6)
                                            else -> Color(0xFFCBD5E1)
                                        },
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(reminder.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(reminder.timeAgo, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(reminder.description, fontSize = 12.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It")
            }
        }
    )
}

