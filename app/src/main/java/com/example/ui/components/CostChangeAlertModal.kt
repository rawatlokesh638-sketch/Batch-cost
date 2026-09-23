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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyFormatter

data class CostImpactItem(
    val productName: String,
    val oldCost: Double,
    val newCost: Double,
    val diff: Double = newCost - oldCost
)

@Composable
fun CostChangeAlertModal(
    ingredientName: String,
    oldPrice: Double,
    newPrice: Double,
    unit: String,
    currencySymbol: String,
    affectedProducts: List<CostImpactItem> = listOf(
        CostImpactItem("Chocolate Cake", 233.0, 248.0),
        CostImpactItem("Brownie Box", 161.0, 169.0),
        CostImpactItem("Butter Cookies", 92.0, 95.0),
        CostImpactItem("Red Velvet Jar", 120.0, 126.0)
    ),
    onDismiss: () -> Unit,
    onConfirmSync: () -> Unit
) {
    val priceDiff = newPrice - oldPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "🚨 Cost Change Alert: $ingredientName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("cost_change_alert_dialog")
            ) {
                // Ingredient Price Shift Summary
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Material Price Change", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Text(
                                "$ingredientName: ${CurrencyFormatter.format(oldPrice, currencySymbol)} ➔ ${CurrencyFormatter.format(newPrice, currencySymbol)}/$unit",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                        Text(
                            text = "+${CurrencyFormatter.format(priceDiff, currencySymbol)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            fontSize = 15.sp
                        )
                    }
                }

                Text(
                    text = "⚠️ ${affectedProducts.size} products in your library use $ingredientName and are affected:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )

                // Affected Products Table
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    affectedProducts.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Unit Cost: ${CurrencyFormatter.format(item.oldCost, currencySymbol)} ➔ ${CurrencyFormatter.format(item.newCost, currencySymbol)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            Text(
                                "+${CurrencyFormatter.format(item.diff, currencySymbol)}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Syncing will automatically update recipe unit costs and profit calculations across all affected products.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSync()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recalculate & Sync All Products")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
