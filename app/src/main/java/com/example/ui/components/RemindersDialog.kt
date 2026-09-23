package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BusinessReminder(
    val title: String,
    val description: String,
    val type: ReminderType,
    val timeAgo: String
)

enum class ReminderType {
    PENDING_ORDER, DELIVERY_TODAY, LOW_INVENTORY, EXPIRING_INGREDIENT, COST_CHANGED, SUBSCRIPTION_EXPIRY
}

@Composable
fun RemindersDialog(
    onDismiss: () -> Unit
) {
    val reminders = listOf(
        BusinessReminder(
            title = "📦 Pending Order Confirmation",
            description = "Rahul Sharma ordered 2x Chocolate Cake (₹1,000) pending confirmation.",
            type = ReminderType.PENDING_ORDER,
            timeAgo = "10 mins ago"
        ),
        BusinessReminder(
            title = "🚚 Delivery Due Today",
            description = "3 orders scheduled for delivery by 4:00 PM today.",
            type = ReminderType.DELIVERY_TODAY,
            timeAgo = "1 hour ago"
        ),
        BusinessReminder(
            title = "⚠️ Low Inventory Alert",
            description = "Butter stock is low (320g remaining vs 500g minimum).",
            type = ReminderType.LOW_INVENTORY,
            timeAgo = "3 hours ago"
        ),
        BusinessReminder(
            title = "⏳ Expiring Ingredient",
            description = "Heavy Cream batch #44 expires in 2 days (Use soon).",
            type = ReminderType.EXPIRING_INGREDIENT,
            timeAgo = "5 hours ago"
        ),
        BusinessReminder(
            title = "📈 Product Cost Changed",
            description = "Butter price increase (₹450 ➔ ₹520/kg) affected 4 product recipes.",
            type = ReminderType.COST_CHANGED,
            timeAgo = "Yesterday"
        ),
        BusinessReminder(
            title = "🔔 Pro Subscription Expiry",
            description = "Your professional maker tier renews in 14 days.",
            type = ReminderType.SUBSCRIPTION_EXPIRY,
            timeAgo = "2 days ago"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔔 Smart Business Reminders", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_dialog")
            ) {
                Text("Automated alerts across orders, inventory, costs, and subscriptions:", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(reminders) { reminder ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
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
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
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
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss All")
            }
        }
    )
}
