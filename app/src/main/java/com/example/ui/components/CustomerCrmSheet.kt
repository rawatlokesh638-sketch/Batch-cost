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
    val recentFrequencyDays: Int,
    val repeatCount60Days: Int
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

    // Sample / Aggregated Customer profiles from orders
    val customers = listOf(
        CustomerProfile(
            name = "Rahul Sharma",
            phone = "+91 98765 43210",
            totalOrders = 12,
            totalRevenue = 8450.0,
            lastProduct = "Chocolate Cake",
            lastQuantity = 2,
            lastOrderDate = "20 Sep 2026",
            notes = "Prefers less sweet. Regular weekend buyer.",
            recentFrequencyDays = 60,
            repeatCount60Days = 4
        ),
        CustomerProfile(
            name = "Priya Patel",
            phone = "+91 98223 11223",
            totalOrders = 8,
            totalRevenue = 5600.0,
            lastProduct = "Brownie Box",
            lastQuantity = 4,
            lastOrderDate = "18 Sep 2026",
            notes = "Allergic to walnuts. Prefers dark chocolate.",
            recentFrequencyDays = 60,
            repeatCount60Days = 3
        ),
        CustomerProfile(
            name = "Amit Verma",
            phone = "+91 99112 33445",
            totalOrders = 5,
            totalRevenue = 3750.0,
            lastProduct = "Butter Cookies",
            lastQuantity = 3,
            lastOrderDate = "15 Sep 2026",
            notes = "Corporate bulk buyer for Friday meetings.",
            recentFrequencyDays = 60,
            repeatCount60Days = 2
        )
    )

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
                                        text = "🔁 **${customer.name}** has ordered **${customer.repeatCount60Days} times** in the last 60 days (${customer.totalOrders} total lifetime orders).",
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
