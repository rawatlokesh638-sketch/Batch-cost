package com.example.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.ProductWithDetails
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productWithDetails: ProductWithDetails,
    currencySymbol: String,
    onBack: () -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onEdit: (ProductWithDetails) -> Unit,
    onGenerateLabel: ((ProductWithDetails) -> Unit)? = null
) {
    val prod = productWithDetails.product
    val recipe = productWithDetails.recipeIngredients

    var simulatedPrice by remember { mutableDoubleStateOf(prod.sellingPrice) }
    var batchMultiplier by remember { mutableIntStateOf(1) }
    var showBatchScaler by remember { mutableStateOf(false) }

    val rawIngCost = productWithDetails.rawIngredientsCost
    val wastageCost = productWithDetails.wastageCost
    val pkgCost = productWithDetails.packagingCost
    val lbrCost = productWithDetails.labourCost
    val ovhCost = productWithDetails.overheadCost

    val totalUnitCost = productWithDetails.totalCost
    val netProfit = simulatedPrice - totalUnitCost
    val marginPercent = if (simulatedPrice > 0) (netProfit / simulatedPrice) * 100.0 else 0.0

    // Multiplied values for Batch Scaler
    val batchRawCost = rawIngCost * batchMultiplier
    val batchTotalCost = totalUnitCost * batchMultiplier
    val batchRevenue = simulatedPrice * batchMultiplier
    val batchProfit = netProfit * batchMultiplier

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(prod.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onGenerateLabel != null) {
                        IconButton(onClick = { onGenerateLabel(productWithDetails) }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Generate Label")
                        }
                    }
                    IconButton(onClick = { onEdit(productWithDetails) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                    }
                    IconButton(onClick = { onDelete(prod) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Level Profitability Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Selling Price (${prod.unit})", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = CurrencyFormatter.format(simulatedPrice, currencySymbol),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Profit (${CurrencyFormatter.formatPercent(marginPercent)})", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = CurrencyFormatter.format(netProfit, currencySymbol),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }

                        // Cost Progress Bar
                        val costFraction = if (simulatedPrice > 0) (totalUnitCost / simulatedPrice).toFloat().coerceIn(0f, 1f) else 1f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Unit Cost: ${CurrencyFormatter.format(totalUnitCost, currencySymbol)}", style = MaterialTheme.typography.labelSmall)
                                Text("Cost Ratio: ${CurrencyFormatter.formatPercent(costFraction.toDouble() * 100)}", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { costFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (costFraction < 0.7f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // AUTOMATIC ITEMIZED COST & PROFIT BREAKDOWN CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📊 Automatic Cost & Profit Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        CostLineItem(label = "Ingredient Cost", value = CurrencyFormatter.format(rawIngCost, currencySymbol))
                        CostLineItem(label = "Packaging Cost", value = CurrencyFormatter.format(pkgCost, currencySymbol))
                        CostLineItem(label = "Labour Cost", value = CurrencyFormatter.format(lbrCost, currencySymbol))
                        CostLineItem(label = "Gas & Overhead", value = CurrencyFormatter.format(ovhCost, currencySymbol))
                        CostLineItem(label = "Wastage (${prod.wastagePercent}%)", value = CurrencyFormatter.format(wastageCost, currencySymbol))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        CostLineItem(
                            label = "Total Unit Cost",
                            value = CurrencyFormatter.format(totalUnitCost, currencySymbol),
                            isBold = true
                        )
                        CostLineItem(
                            label = "Selling Price",
                            value = CurrencyFormatter.format(simulatedPrice, currencySymbol),
                            isBold = true
                        )
                        CostLineItem(
                            label = "Calculated Net Profit",
                            value = CurrencyFormatter.format(netProfit, currencySymbol),
                            isBold = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Live Price Simulator Slider
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Live Price Simulator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Adjust selling price slider to see instant profit impact:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val minVal = (totalUnitCost.toFloat() * 0.8f).coerceAtLeast(10f)
                        val maxVal = (totalUnitCost.toFloat() * 3f).coerceAtLeast(100f)
                        val safeRange = if (maxVal > minVal) minVal..maxVal else minVal..(minVal + 100f)

                        Slider(
                            value = simulatedPrice.toFloat().coerceIn(safeRange.start, safeRange.endInclusive),
                            onValueChange = { simulatedPrice = it.toDouble() },
                            valueRange = safeRange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Batch Cost Scaler Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Batch Cost Scaler",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Button(onClick = { showBatchScaler = !showBatchScaler }) {
                                Text(if (showBatchScaler) "Hide" else "Calculate Batch")
                            }
                        }

                        AnimatedVisibility(visible = showBatchScaler) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = batchMultiplier.toString(),
                                    onValueChange = { batchMultiplier = it.toIntOrNull() ?: 1 },
                                    label = { Text("Batch Quantity (${prod.unit}s)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Batch Summary for $batchMultiplier ${prod.unit}(s):", fontWeight = FontWeight.Bold)
                                        Text("Total Raw Material Cost: ${CurrencyFormatter.format(batchRawCost, currencySymbol)}")
                                        Text("Total Batch Cost: ${CurrencyFormatter.format(batchTotalCost, currencySymbol)}")
                                        Text("Total Revenue: ${CurrencyFormatter.format(batchRevenue, currencySymbol)}")
                                        Text("Total Net Profit: ${CurrencyFormatter.format(batchProfit, currencySymbol)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recipe Ingredients Breakdown List
            item {
                Text(
                    text = "Recipe Ingredients (${recipe.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(recipe) { ing ->
                val cost = UnitConverter.calculateCost(
                    ing.purchaseQty,
                    ing.purchaseUnit,
                    ing.purchasePrice,
                    ing.usedQty,
                    ing.usedUnit
                )
                val percentOfTotal = if (rawIngCost > 0) (cost / rawIngCost) * 100.0 else 0.0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ing.ingredientName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Purchase: ${ing.purchaseQty} ${ing.purchaseUnit} @ ${CurrencyFormatter.format(ing.purchasePrice, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Used in recipe: ${ing.usedQty} ${ing.usedUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.format(cost * batchMultiplier, currencySymbol),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = CurrencyFormatter.formatPercent(percentOfTotal),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CostLineItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}
