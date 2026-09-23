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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

data class SubscriptionPlan(
    val title: String,
    val priceMonthly: String,
    val priceAnnual: String,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

@Composable
fun MonetizationPlansDialog(
    currentPlan: String = "PRO",
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    var isAnnualBilling by remember { mutableStateOf(false) }

    val plans = listOf(
        SubscriptionPlan(
            title = "FREE",
            priceMonthly = "₹0",
            priceAnnual = "₹0",
            description = "For hobbyists starting out",
            features = listOf("5 batches/month", "5 products", "Basic costing & orders", "Watermark on labels")
        ),
        SubscriptionPlan(
            title = "STARTER",
            priceMonthly = "₹99 / mo",
            priceAnnual = "₹999 / yr",
            description = "For growing local makers",
            features = listOf("Unlimited batches & products", "Product library & history", "Basic labels", "No watermarks")
        ),
        SubscriptionPlan(
            title = "BUSINESS",
            priceMonthly = "₹249 / mo",
            priceAnnual = "₹2,499 / yr",
            description = "For professional bakeries & stores",
            features = listOf("Everything Starter", "PDF labels & branding", "WhatsApp import & price lists", "Customer CRM & analytics"),
            isPopular = true
        ),
        SubscriptionPlan(
            title = "PRO",
            priceMonthly = "₹499 / mo",
            priceAnnual = "₹4,999 / yr",
            description = "For serious multi-product businesses",
            features = listOf("Everything Business", "Inventory & cost alerts", "Advanced reports & AI recipe parsing", "Team roles & permissions")
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("💳 Subscription & Monetization Plans", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monetization_plans_dialog")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Choose billing cycle:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { isAnnualBilling = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isAnnualBilling) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Monthly", fontSize = 11.sp, color = if (!isAnnualBilling) Color.White else Color.Black)
                        }
                        Button(
                            onClick = { isAnnualBilling = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAnnualBilling) Color(0xFF10B981) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Annual (Save 20%)", fontSize = 11.sp, color = if (isAnnualBilling) Color.White else Color.Black)
                        }
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(plans) { plan ->
                        val isCurrent = currentPlan.equals(plan.title, ignoreCase = true)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (plan.isPopular) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (plan.isPopular) Color(0xFF3B82F6) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (plan.isPopular) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier.background(Color(0xFF3B82F6), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (isAnnualBilling) plan.priceAnnual else plan.priceMonthly,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        fontSize = 15.sp
                                    )
                                }
                                Text(plan.description, fontSize = 11.sp, color = Color.Gray)
                                plan.features.take(2).forEach { feat ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(feat, fontSize = 11.sp, color = Color(0xFF334155))
                                    }
                                }

                                Button(
                                    onClick = { onSelectPlan(plan.title) },
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isCurrent) Color(0xFF64748B) else MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (isCurrent) "Current Plan" else "Select ${plan.title}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Close")
            }
        }
    )
}
