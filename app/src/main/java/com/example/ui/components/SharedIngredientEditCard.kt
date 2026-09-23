package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TempIngredientItem
import com.example.util.CurrencyFormatter

@Composable
fun SharedIngredientEditCard(
    item: TempIngredientItem,
    currencySymbol: String,
    onUpdate: (TempIngredientItem) -> Unit,
    onDelete: () -> Unit
) {
    val computedCost = item.calculatedCost()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onUpdate(item.copy(name = it)) },
                    label = { Text("Ingredient Name") },
                    placeholder = { Text("e.g. Maida / Sugar") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_ingredient_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.purchaseQty,
                    onValueChange = { onUpdate(item.copy(purchaseQty = it)) },
                    label = { Text("Purchase Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = item.purchaseUnit,
                    onValueChange = { onUpdate(item.copy(purchaseUnit = it)) },
                    label = { Text("Purchase Unit") },
                    placeholder = { Text("kg / l / pc") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = item.purchasePrice,
                    onValueChange = { onUpdate(item.copy(purchasePrice = it)) },
                    label = { Text("Price ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.usedQty,
                    onValueChange = { onUpdate(item.copy(usedQty = it)) },
                    label = { Text("Used Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = item.usedUnit,
                    onValueChange = { onUpdate(item.copy(usedUnit = it)) },
                    label = { Text("Used Unit") },
                    placeholder = { Text("g / ml / pc") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Effective Cost", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = CurrencyFormatter.format(computedCost, currencySymbol),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 🗑️ WASTAGE SELECTION CONTROL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🗑️ Ingredient Wastage Setting:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("No wastage", "Fixed %", "Actual waste").forEach { wType ->
                        FilterChip(
                            selected = item.wastageType == wType,
                            onClick = { onUpdate(item.copy(wastageType = wType)) },
                            label = { Text(wType, fontSize = 11.sp) },
                            modifier = Modifier.testTag("wastage_type_chip_${wType.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                if (item.wastageType == "Fixed %") {
                    OutlinedTextField(
                        value = item.wastagePercent,
                        onValueChange = { onUpdate(item.copy(wastagePercent = it)) },
                        label = { Text("Wastage Percentage (%)") },
                        placeholder = { Text("e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("wastage_percent_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                } else if (item.wastageType == "Actual waste") {
                    OutlinedTextField(
                        value = item.actualWasteQty,
                        onValueChange = { onUpdate(item.copy(actualWasteQty = it)) },
                        label = { Text("Wasted Qty (${item.usedUnit})") },
                        placeholder = { Text("e.g. 150") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("actual_waste_qty_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}
