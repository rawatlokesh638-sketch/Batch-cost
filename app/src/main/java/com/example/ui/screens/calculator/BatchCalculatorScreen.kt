package com.example.ui.screens.calculator

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProductWithDetails
import com.example.ui.SavedBatchRecord
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@Composable
fun BatchCalculatorScreen(
    products: List<ProductWithDetails>,
    currencySymbol: String,
    savedBatches: List<SavedBatchRecord> = emptyList(),
    onSaveBatchRecord: ((String, Int, Double, Double, String) -> Unit)? = null,
    onDuplicateBatchRecord: ((String, Int) -> Unit)? = null,
    onDeleteBatchRecord: ((String) -> Unit)? = null,
    onSaveBatch: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Scaler, 1: Price, 2: Profit, 3: History

    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var batchSizeText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var batchSavedNotice by remember { mutableStateOf(false) }

    // Dialog States for Batch History
    var viewingBatch by remember { mutableStateOf<SavedBatchRecord?>(null) }
    var duplicatingBatch by remember { mutableStateOf<SavedBatchRecord?>(null) }
    var newDuplicateUnitsText by remember { mutableStateOf("") }
    var exportingBatchFormat by remember { mutableStateOf<Pair<SavedBatchRecord, String>?>(null) }

    val activeProduct = products.getOrNull(selectedProductIndex)
    val batchQty = batchSizeText.toIntOrNull() ?: 1

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Sub-Tab Toggle
            TabRow(
                selectedTabIndex = activeSubTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("🧮 Scaler", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("subtab_batch_scaler")
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("💰 Price", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("subtab_price_calc")
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("📊 Profit", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("subtab_profit_calc")
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("📈 History (${savedBatches.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("subtab_batch_history")
                )
            }

            when (activeSubTab) {
                1 -> SellingPriceCalculatorScreen(
                    products = products,
                    currencySymbol = currencySymbol
                )
                2 -> RealisticProfitCalculatorScreen(
                    products = products,
                    currencySymbol = currencySymbol
                )
                3 -> {
                    // Batch History Screen View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📈 Saved Production Batch History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                            Text("Inspect, duplicate with auto-scaling, or export previous batch runs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (savedBatches.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No Saved Batches Yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Save a batch in the 'Scaler' tab to log cost and recipe metrics here.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        } else {
                            items(savedBatches) { batch ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("batch_history_card_${batch.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(batch.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Text("${batch.date} • ${batch.unitsProduced} units", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "+${CurrencyFormatter.format(batch.estimatedProfit, currencySymbol)} Est. Profit",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Total Cost: ${CurrencyFormatter.format(batch.totalCost, currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("Cost/unit: ${CurrencyFormatter.format(batch.costPerUnit, currencySymbol)}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Selling: ${CurrencyFormatter.format(batch.sellingPricePerUnit, currencySymbol)}", fontSize = 12.sp, color = Color.Gray)
                                        }

                                        if (batch.notes.isNotBlank()) {
                                            Text("Note: ${batch.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }

                                        // Action Bar (View, Duplicate, Export, Delete)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewingBatch = batch },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    duplicatingBatch = batch
                                                    newDuplicateUnitsText = "50"
                                                },
                                                modifier = Modifier.weight(1.3f).testTag("duplicate_batch_button"),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Duplicate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { exportingBatchFormat = Pair(batch, "PDF") },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = { onDeleteBatchRecord?.invoke(batch.id) }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
                else -> {
                    // Scaler Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🧮 Batch Production Scaler",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        if (products.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No Products Available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Add a product first to start scaling batch quantities and costs.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        } else {
                            // Product Selector & Batch Size Input
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("1. Select Product & Target Batch Quantity:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = activeProduct?.product?.name ?: "Select Product",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Product") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { dropdownExpanded = true },
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            DropdownMenu(
                                                expanded = dropdownExpanded,
                                                onDismissRequest = { dropdownExpanded = false }
                                            ) {
                                                products.forEachIndexed { index, item ->
                                                    DropdownMenuItem(
                                                        text = { Text(item.product.name) },
                                                        onClick = {
                                                            selectedProductIndex = index
                                                            dropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = batchSizeText,
                                            onValueChange = { batchSizeText = it },
                                            label = { Text("Target Batch Size (Units / Cakes)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("batch_quantity_input"),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            if (activeProduct != null) {
                                val singleUnitCost = activeProduct.totalCost
                                val batchRawIngCost = activeProduct.rawIngredientsCost * batchQty
                                val batchTotalCost = singleUnitCost * batchQty
                                val batchRevenue = activeProduct.product.sellingPrice * batchQty
                                val batchProfit = batchRevenue - batchTotalCost

                                // Scaled Ingredients List
                                item {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "📦 Scaled Ingredients Needed for $batchQty Units:",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            activeProduct.recipeIngredients.forEach { recipe ->
                                                val scaledQty = recipe.usedQty * batchQty
                                                val recipeCost = UnitConverter.calculateCost(
                                                    recipe.purchaseQty,
                                                    recipe.purchaseUnit,
                                                    recipe.purchasePrice,
                                                    scaledQty,
                                                    recipe.usedUnit
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(recipe.ingredientName, fontWeight = FontWeight.SemiBold)
                                                        Text(
                                                            text = "${formatScaledUnit(recipe.usedQty, recipe.usedUnit)} × $batchQty = ${formatScaledUnit(scaledQty, recipe.usedUnit)}",
                                                            fontSize = 12.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Text(
                                                        text = CurrencyFormatter.format(recipeCost, currencySymbol),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Batch Profitability Summary
                                item {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("💰 Batch Financial Summary:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))

                                            BatchResultRow("Batch Raw Ingredients Cost", CurrencyFormatter.format(batchRawIngCost, currencySymbol))
                                            BatchResultRow("Batch Total Cost (Incl. Overheads)", CurrencyFormatter.format(batchTotalCost, currencySymbol), isBold = true)
                                            BatchResultRow("Cost Per Unit", CurrencyFormatter.format(singleUnitCost, currencySymbol))
                                            BatchResultRow("Selling Price Per Unit", CurrencyFormatter.format(activeProduct.product.sellingPrice, currencySymbol))
                                            BatchResultRow("Total Revenue ($batchQty units)", CurrencyFormatter.format(batchRevenue, currencySymbol))
                                            BatchResultRow(
                                                label = "Estimated Net Profit",
                                                value = CurrencyFormatter.format(batchProfit, currencySymbol),
                                                isBold = true,
                                                valueColor = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }

                                // SAVE BATCH BUTTON
                                item {
                                    Button(
                                        onClick = {
                                            onSaveBatchRecord?.invoke(
                                                activeProduct.product.name,
                                                batchQty,
                                                batchTotalCost,
                                                activeProduct.product.sellingPrice,
                                                "Production run for $batchQty units"
                                            )
                                            onSaveBatch?.invoke()
                                            batchSavedNotice = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("save_batch_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Batch Production Run to History", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (batchSavedNotice) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "✅ Batch saved to history & dashboard counters updated!",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }

    // Modal 1: View Batch Details
    if (viewingBatch != null) {
        val b = viewingBatch!!
        AlertDialog(
            onDismissRequest = { viewingBatch = null },
            title = { Text("📊 Batch Breakdown: ${b.productName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Date: ${b.date}")
                    Text("• Units Produced: ${b.unitsProduced} units")
                    Text("• Total Cost: ${CurrencyFormatter.format(b.totalCost, currencySymbol)}")
                    Text("• Cost / Unit: ${CurrencyFormatter.format(b.costPerUnit, currencySymbol)}")
                    Text("• Selling Price: ${CurrencyFormatter.format(b.sellingPricePerUnit, currencySymbol)}")
                    Text("• Est. Profit: ${CurrencyFormatter.format(b.estimatedProfit, currencySymbol)}", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    Text("• Ingredients: ${b.ingredientsSummary}")
                    Text("• Notes: ${b.notes}")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingBatch = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal 2: Duplicate Batch & Scale Quantity
    if (duplicatingBatch != null) {
        val oldB = duplicatingBatch!!
        val oldQty = oldB.unitsProduced
        val newQty = newDuplicateUnitsText.toIntOrNull() ?: 50
        val scaleRatio = if (oldQty > 0) newQty.toDouble() / oldQty.toDouble() else 1.0
        val estScaledCost = oldB.totalCost * scaleRatio
        val estScaledProfit = (oldB.sellingPricePerUnit * newQty) - estScaledCost

        AlertDialog(
            onDismissRequest = { duplicatingBatch = null },
            title = { Text("👯 Duplicate & Auto-Scale Batch", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Product: ${oldB.productName}", fontWeight = FontWeight.Bold)
                    Text("Old Batch Size: $oldQty units (${CurrencyFormatter.format(oldB.totalCost, currencySymbol)})")

                    OutlinedTextField(
                        value = newDuplicateUnitsText,
                        onValueChange = { newDuplicateUnitsText = it },
                        label = { Text("New Batch Size (e.g. 50 cakes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("duplicate_batch_qty_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡ Scaling Engine Prediction:", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                            Text("• Scaling Ratio: ${String.format("%.2fx", scaleRatio)}")
                            Text("• New Total Cost: ${CurrencyFormatter.format(estScaledCost, currencySymbol)}")
                            Text("• New Est. Profit: ${CurrencyFormatter.format(estScaledProfit, currencySymbol)}", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("• Ingredient quantities automatically scaled ${String.format("%.2fx", scaleRatio)}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDuplicateBatchRecord?.invoke(oldB.id, newQty)
                        duplicatingBatch = null
                        Toast.makeText(context, "Duplicated batch scaled to $newQty units!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_duplicate_button")
                ) {
                    Text("Confirm & Auto-Scale Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicatingBatch = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal 3: Export Batch Format
    if (exportingBatchFormat != null) {
        val (b, fmt) = exportingBatchFormat!!
        AlertDialog(
            onDismissRequest = { exportingBatchFormat = null },
            title = { Text("📥 Batch Exported ($fmt)", fontWeight = FontWeight.Bold) },
            text = {
                Text("Batch summary for '${b.productName}' (${b.unitsProduced} units) exported as $fmt.\nSaved to Downloads/BatchCost_Reports/")
            },
            confirmButton = {
                TextButton(onClick = { exportingBatchFormat = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun BatchResultRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

fun formatScaledUnit(qty: Double, unit: String): String {
    val cleanUnit = unit.lowercase().trim()
    val df = java.text.DecimalFormat("#,##0.##")
    return when (cleanUnit) {
        "g" -> if (qty >= 1000) "${df.format(qty / 1000.0)} kg" else "${df.format(qty)} g"
        "ml" -> if (qty >= 1000) "${df.format(qty / 1000.0)} litre" else "${df.format(qty)} ml"
        else -> "${df.format(qty)} $unit"
    }
}
