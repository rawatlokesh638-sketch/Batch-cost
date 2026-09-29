package com.example.ui.screens.calculator

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterIngredientEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.ProductWithDetails
import com.example.ui.TempIngredientItem
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CustomBatchIngredient(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val purchaseQty: String = "1",
    val purchaseUnit: String = "kg",
    val purchasePrice: String = "80",
    val usedQty: String = "250",
    val usedUnit: String = "g",
    val supplier: String = "",
    val notes: String = ""
) {
    fun calculatedCost(): Double {
        val pQty = purchaseQty.toDoubleOrNull() ?: 1.0
        val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
        val uQty = usedQty.toDoubleOrNull() ?: 0.0
        return UnitConverter.calculateCost(pQty, purchaseUnit, pPrice, uQty, usedUnit)
    }

    fun stepExplanation(currencySymbol: String): String {
        val pQty = purchaseQty.toDoubleOrNull() ?: 1.0
        val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
        val uQty = usedQty.toDoubleOrNull() ?: 0.0
        return UnitConverter.formatCalculationSteps(pQty, purchaseUnit, pPrice, uQty, usedUnit, currencySymbol)
    }
}

@Composable
fun InteractiveBatchCostSheetScreen(
    currencySymbol: String,
    products: List<ProductWithDetails>,
    masterIngredients: List<MasterIngredientEntity> = emptyList(),
    onSaveBatchRecord: ((String, Int, Double, Double, String) -> Unit)? = null,
    onSaveNewProduct: ((ProductEntity, List<TempIngredientItem>) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Batch Header
    var batchTitle by remember { mutableStateOf("Fresh Artisan Cake Batch") }
    var batchNotes by remember { mutableStateOf("") }
    var yieldCountText by remember { mutableStateOf("10") }
    var yieldUnitText by remember { mutableStateOf("cakes") }

    // Dynamic Ingredients list initialized with realistic recipe
    var ingredientsList by remember {
        mutableStateOf(
            listOf(
                CustomBatchIngredient(name = "Flour (Maida)", purchaseQty = "10", purchaseUnit = "kg", purchasePrice = "400", usedQty = "2.5", usedUnit = "kg"),
                CustomBatchIngredient(name = "Caster Sugar", purchaseQty = "5", purchaseUnit = "kg", purchasePrice = "250", usedQty = "2", usedUnit = "kg"),
                CustomBatchIngredient(name = "Unsalted Butter", purchaseQty = "500", purchaseUnit = "g", purchasePrice = "280", usedQty = "1000", usedUnit = "g"),
                CustomBatchIngredient(name = "Dark Chocolate (55%)", purchaseQty = "1", purchaseUnit = "kg", purchasePrice = "850", usedQty = "1.5", usedUnit = "kg"),
                CustomBatchIngredient(name = "Dairy Whipping Cream", purchaseQty = "1", purchaseUnit = "L", purchasePrice = "320", usedQty = "1.5", usedUnit = "L")
            )
        )
    }

    // Overheads
    var packagingCostText by remember { mutableStateOf("250") } // e.g. ₹25/box * 10
    var labourCostText by remember { mutableStateOf("400") } // Baker hours
    var electricityGasCostText by remember { mutableStateOf("150") } // Oven utility
    var deliveryCostText by remember { mutableStateOf("100") } // Logistics
    var otherCostText by remember { mutableStateOf("50") } // Misc
    var wastagePercentText by remember { mutableStateOf("3") } // 3% wastage

    // Selling & Profit
    var sellingPricePerUnitText by remember { mutableStateOf("650") }

    // GST Module (Phase 7)
    var isGstEnabled by remember { mutableStateOf(true) }
    var selectedGstRateText by remember { mutableStateOf("5") } // 5% bakery GST standard
    var isGstInclusive by remember { mutableStateOf(false) } // False: Added on top; True: inclusive

    // Modals & Popups
    var showProductPicker by remember { mutableStateOf(false) }
    var showPantryPickerIndex by remember { mutableStateOf<Int?>(null) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var showCostSheetPreview by remember { mutableStateOf(false) }

    // Computations
    val yieldCount = yieldCountText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val rawIngredientsTotal = ingredientsList.sumOf { it.calculatedCost() }
    val wastagePct = wastagePercentText.toDoubleOrNull() ?: 0.0
    val wastageTotal = rawIngredientsTotal * (wastagePct / 100.0)

    val packagingTotal = packagingCostText.toDoubleOrNull() ?: 0.0
    val labourTotal = labourCostText.toDoubleOrNull() ?: 0.0
    val utilityTotal = electricityGasCostText.toDoubleOrNull() ?: 0.0
    val deliveryTotal = deliveryCostText.toDoubleOrNull() ?: 0.0
    val otherTotal = otherCostText.toDoubleOrNull() ?: 0.0

    val totalOverheads = packagingTotal + labourTotal + utilityTotal + deliveryTotal + otherTotal
    val totalBatchCost = rawIngredientsTotal + wastageTotal + totalOverheads

    val costPerUnit = totalBatchCost / yieldCount
    val sellingPricePerUnit = sellingPricePerUnitText.toDoubleOrNull() ?: 0.0

    // Unit Economics
    val profitPerUnit = sellingPricePerUnit - costPerUnit
    val totalBatchRevenue = sellingPricePerUnit * yieldCount
    val totalBatchProfit = profitPerUnit * yieldCount
    val profitMarginPercent = if (sellingPricePerUnit > 0) (profitPerUnit / sellingPricePerUnit) * 100.0 else 0.0
    val markupPercent = if (costPerUnit > 0) (profitPerUnit / costPerUnit) * 100.0 else 0.0

    // GST Calculations
    val gstRate = if (isGstEnabled) (selectedGstRateText.toDoubleOrNull() ?: 0.0) else 0.0
    val (baseSellingPrice, gstAmountPerUnit, finalCustomerPrice) = if (isGstEnabled && gstRate > 0) {
        if (isGstInclusive) {
            val base = sellingPricePerUnit / (1.0 + (gstRate / 100.0))
            val gst = sellingPricePerUnit - base
            Triple(base, gst, sellingPricePerUnit)
        } else {
            val gst = sellingPricePerUnit * (gstRate / 100.0)
            val finalPrice = sellingPricePerUnit + gst
            Triple(sellingPricePerUnit, gst, finalPrice)
        }
    } else {
        Triple(sellingPricePerUnit, 0.0, sellingPricePerUnit)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("interactive_batch_cost_sheet"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER TITLE & PRESETS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("🧮 Interactive Batch Cost Sheet", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }

                        if (products.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showProductPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Load Recipe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        "Professional recipe costing, ingredient math, overheads, yield, margin vs markup, and GST invoice helper.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = batchTitle,
                            onValueChange = { batchTitle = it },
                            label = { Text("Batch / Recipe Name") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = yieldCountText,
                            onValueChange = { yieldCountText = it },
                            label = { Text("Yield Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f).testTag("batch_yield_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = yieldUnitText,
                            onValueChange = { yieldUnitText = it },
                            label = { Text("Unit (cakes/jars)") },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 1. INGREDIENTS LIST SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("1. Raw Ingredients Costing", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Auto unit conversion (kg, g, mg, L, ml, pcs, dozen, packs)", fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = {
                        ingredientsList = ingredientsList + CustomBatchIngredient()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_ingredient_row_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Row", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // INGREDIENTS CARDS
        itemsIndexed(ingredientsList) { index, item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("ingredient_row_$index")
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
                        Text(
                            text = "${index + 1}. ${item.name.ifBlank { "New Ingredient" }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (masterIngredients.isNotEmpty()) {
                                TextButton(
                                    onClick = { showPantryPickerIndex = index },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("Pick Pantry", fontSize = 11.sp)
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (ingredientsList.size > 1) {
                                        ingredientsList = ingredientsList.toMutableList().also { it.removeAt(index) }
                                    } else {
                                        Toast.makeText(context, "Keep at least 1 ingredient", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Ingredient Name Input
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { newName ->
                            ingredientsList = ingredientsList.toMutableList().also {
                                it[index] = item.copy(name = newName)
                            }
                        },
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Purchase Quantity, Unit & Purchase Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = item.purchaseQty,
                            onValueChange = { v ->
                                ingredientsList = ingredientsList.toMutableList().also { it[index] = item.copy(purchaseQty = v) }
                            },
                            label = { Text("Buy Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        UnitDropdownSelector(
                            selectedUnit = item.purchaseUnit,
                            onUnitSelected = { u ->
                                ingredientsList = ingredientsList.toMutableList().also { it[index] = item.copy(purchaseUnit = u) }
                            },
                            modifier = Modifier.weight(1.1f),
                            label = "Buy Unit"
                        )

                        OutlinedTextField(
                            value = item.purchasePrice,
                            onValueChange = { v ->
                                ingredientsList = ingredientsList.toMutableList().also { it[index] = item.copy(purchasePrice = v) }
                            },
                            label = { Text("Price ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Used Quantity & Used Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.usedQty,
                            onValueChange = { v ->
                                ingredientsList = ingredientsList.toMutableList().also { it[index] = item.copy(usedQty = v) }
                            },
                            label = { Text("Used Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        UnitDropdownSelector(
                            selectedUnit = item.usedUnit,
                            onUnitSelected = { u ->
                                ingredientsList = ingredientsList.toMutableList().also { it[index] = item.copy(usedUnit = u) }
                            },
                            modifier = Modifier.weight(1.2f),
                            label = "Used Unit"
                        )

                        // Calculated Cost Badge
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cost", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    text = CurrencyFormatter.format(item.calculatedCost(), currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Transparent Step-by-Step Explanation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📐 Math: ${item.stepExplanation(currencySymbol)}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // RAW INGREDIENTS SUB-TOTAL
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Ingredients Cost (${ingredientsList.size} items)", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    Text(
                        text = CurrencyFormatter.format(rawIngredientsTotal, currencySymbol),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }

        // 2. OVERHEADS & ADDITIONAL COSTS
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("2. Packaging & Overheads Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Add your real business overheads so you don't lose money.", fontSize = 11.sp, color = Color.Gray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = packagingCostText,
                            onValueChange = { packagingCostText = it },
                            label = { Text("Packaging ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = labourCostText,
                            onValueChange = { labourCostText = it },
                            label = { Text("Labour ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = electricityGasCostText,
                            onValueChange = { electricityGasCostText = it },
                            label = { Text("Gas/Electricity ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = deliveryCostText,
                            onValueChange = { deliveryCostText = it },
                            label = { Text("Delivery ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = otherCostText,
                            onValueChange = { otherCostText = it },
                            label = { Text("Other Expenses ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = wastagePercentText,
                            onValueChange = { wastagePercentText = it },
                            label = { Text("Wastage %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (wastagePct > 0) {
                        Text(
                            text = "Wastage ($wastagePct%): +${CurrencyFormatter.format(wastageTotal, currencySymbol)}",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // 3. TOTAL BATCH COST & UNIT YIELD
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("3. Total Batch Cost & Yield", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Batch Cost ($yieldCount $yieldUnitText):", fontWeight = FontWeight.SemiBold)
                        Text(CurrencyFormatter.format(totalBatchCost, currencySymbol), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFFB45309))
                    }

                    HorizontalDivider(color = Color(0xFFFDE68A))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Cost Per Single Unit:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Total Batch Cost ÷ $yieldCount $yieldUnitText", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            text = CurrencyFormatter.format(costPerUnit, currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // 4. SELLING PRICE, MARGIN & MARKUP
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("4. Target Selling Price & Profit", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Quick Margin Preset Chips (30%, 40%, 50%, 60%)
                    Text("Quick Margin Targets (calculate selling price):", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(30, 40, 50, 60).forEach { targetPct ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    // Price = Cost / (1 - margin)
                                    val calcPrice = if (targetPct < 100) costPerUnit / (1.0 - (targetPct / 100.0)) else costPerUnit * 1.5
                                    sellingPricePerUnitText = String.format(Locale.ROOT, "%.0f", calcPrice)
                                },
                                label = { Text("$targetPct% Margin", fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sellingPricePerUnitText,
                        onValueChange = { sellingPricePerUnitText = it },
                        label = { Text("Selling Price Per Unit ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("batch_selling_price_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Financial metrics breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Profit Per Unit:", fontSize = 13.sp)
                            Text(
                                text = CurrencyFormatter.format(profitPerUnit, currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (profitPerUnit >= 0) Color(0xFF15803D) else Color.Red
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Batch Revenue ($yieldCount units):", fontSize = 13.sp)
                            Text(CurrencyFormatter.format(totalBatchRevenue, currencySymbol), fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Batch Net Profit:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = CurrencyFormatter.format(totalBatchProfit, currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (totalBatchProfit >= 0) Color(0xFF15803D) else Color.Red
                            )
                        }

                        HorizontalDivider(color = Color(0xFFBBF7D0))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Profit Margin:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                                Text("Profit ÷ Selling Price", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text("${String.format("%.1f", profitMarginPercent)}%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF15803D))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Markup:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1D4ED8))
                                Text("Profit ÷ Production Cost", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text("${String.format("%.1f", markupPercent)}%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1D4ED8))
                        }
                    }

                    // Distinction explanation
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Margin vs Markup: Margin (${String.format("%.1f", profitMarginPercent)}%) shows what percentage of your final selling price is actual cash profit. Markup (${String.format("%.1f", markupPercent)}%) is the percentage added on top of your production cost.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // 5. GST / TAX MODULE (Phase 7)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("gst_module_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("5. GST & Tax Calculation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("FSSAI / GST invoice breakdown", fontSize = 11.sp, color = Color.Gray)
                        }

                        Switch(
                            checked = isGstEnabled,
                            onCheckedChange = { isGstEnabled = it }
                        )
                    }

                    AnimatedVisibility(visible = isGstEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Select GST Slab:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("0", "5", "12", "18").forEach { rate ->
                                    FilterChip(
                                        selected = selectedGstRateText == rate,
                                        onClick = { selectedGstRateText = rate },
                                        label = { Text("$rate% GST", fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isGstInclusive) "Mode: Price is Tax-Inclusive" else "Mode: Tax Added On Top (Exclusive)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                OutlinedButton(
                                    onClick = { isGstInclusive = !isGstInclusive },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (isGstInclusive) "Switch to Exclusive" else "Switch to Inclusive", fontSize = 11.sp)
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Base Selling Price (excl. GST):", fontSize = 12.sp)
                                    Text(CurrencyFormatter.format(baseSellingPrice, currencySymbol), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CGST (${gstRate / 2}%):", fontSize = 11.sp, color = Color.Gray)
                                    Text(CurrencyFormatter.format(gstAmountPerUnit / 2.0, currencySymbol), fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SGST (${gstRate / 2}%):", fontSize = 11.sp, color = Color.Gray)
                                    Text(CurrencyFormatter.format(gstAmountPerUnit / 2.0, currencySymbol), fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total GST Amount:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(CurrencyFormatter.format(gstAmountPerUnit, currencySymbol), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D4ED8))
                                }

                                HorizontalDivider(color = Color(0xFFBFDBFE))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Final Invoiced Price Per Unit:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(CurrencyFormatter.format(finalCustomerPrice, currencySymbol), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E40AF))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. ACTION BUTTONS: SAVE, EXPORT, SHARE
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSaveBatchRecord?.invoke(
                            batchTitle.ifBlank { "Production Batch" },
                            yieldCount,
                            totalBatchCost,
                            sellingPricePerUnit,
                            "Yield: $yieldCount $yieldUnitText | Margin: ${String.format("%.1f", profitMarginPercent)}% | GST: $gstRate%"
                        )
                        showSavedDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_interactive_batch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save to Batch Production History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val costSheetText = buildCostSheetShareText(
                                batchTitle = batchTitle,
                                yieldCount = yieldCount,
                                yieldUnit = yieldUnitText,
                                ingredients = ingredientsList,
                                rawIngTotal = rawIngredientsTotal,
                                overheadsTotal = totalOverheads,
                                totalCost = totalBatchCost,
                                costPerUnit = costPerUnit,
                                sellingPrice = sellingPricePerUnit,
                                profitPerUnit = profitPerUnit,
                                marginPct = profitMarginPercent,
                                markupPct = markupPercent,
                                isGst = isGstEnabled,
                                gstRate = gstRate,
                                finalPrice = finalCustomerPrice,
                                currencySymbol = currencySymbol
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Cost Sheet - $batchTitle")
                                putExtra(Intent.EXTRA_TEXT, costSheetText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Cost Sheet"))
                        },
                        modifier = Modifier.weight(1f).height(46.dp).testTag("share_cost_sheet_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val costSheetText = buildCostSheetShareText(
                                batchTitle = batchTitle,
                                yieldCount = yieldCount,
                                yieldUnit = yieldUnitText,
                                ingredients = ingredientsList,
                                rawIngTotal = rawIngredientsTotal,
                                overheadsTotal = totalOverheads,
                                totalCost = totalBatchCost,
                                costPerUnit = costPerUnit,
                                sellingPrice = sellingPricePerUnit,
                                profitPerUnit = profitPerUnit,
                                marginPct = profitMarginPercent,
                                markupPct = markupPercent,
                                isGst = isGstEnabled,
                                gstRate = gstRate,
                                finalPrice = finalCustomerPrice,
                                currencySymbol = currencySymbol
                            )
                            clipboardManager.setText(AnnotatedString(costSheetText))
                            Toast.makeText(context, "📋 Full Cost Sheet copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }

    // Modal 1: Load Existing Product into Batch Calculator
    if (showProductPicker) {
        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text("Select Product to Load", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(products) { _, p ->
                        Card(
                            onClick = {
                                batchTitle = p.product.name
                                sellingPricePerUnitText = p.product.sellingPrice.toString()
                                packagingCostText = (p.packagingCost * yieldCount).toString()
                                labourCostText = (p.labourCost * yieldCount).toString()
                                electricityGasCostText = (p.product.electricityGasCost * yieldCount).toString()
                                wastagePercentText = p.product.wastagePercent.toString()

                                if (p.recipeIngredients.isNotEmpty()) {
                                    ingredientsList = p.recipeIngredients.map { r ->
                                        CustomBatchIngredient(
                                            name = r.ingredientName,
                                            purchaseQty = r.purchaseQty.toString(),
                                            purchaseUnit = r.purchaseUnit,
                                            purchasePrice = r.purchasePrice.toString(),
                                            usedQty = (r.usedQty * yieldCount).toString(),
                                            usedUnit = r.usedUnit
                                        )
                                    }
                                }
                                showProductPicker = false
                                Toast.makeText(context, "Loaded '${p.product.name}' into Batch Calculator!", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p.product.name, fontWeight = FontWeight.Bold)
                                Text(CurrencyFormatter.format(p.product.sellingPrice, currencySymbol), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("Cancel") }
            }
        )
    }

    // Modal 2: Pantry Ingredient Picker for row
    if (showPantryPickerIndex != null) {
        val targetIdx = showPantryPickerIndex!!
        AlertDialog(
            onDismissRequest = { showPantryPickerIndex = null },
            title = { Text("Select from Pantry Master", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(masterIngredients) { _, item ->
                        Card(
                            onClick = {
                                if (targetIdx in ingredientsList.indices) {
                                    ingredientsList = ingredientsList.toMutableList().also {
                                        it[targetIdx] = it[targetIdx].copy(
                                            name = item.name,
                                            purchaseQty = item.purchaseQty.toString(),
                                            purchaseUnit = item.purchaseUnit,
                                            purchasePrice = item.purchasePrice.toString()
                                        )
                                    }
                                }
                                showPantryPickerIndex = null
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold)
                                    Text("${item.purchaseQty} ${item.purchaseUnit}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(CurrencyFormatter.format(item.purchasePrice, currencySymbol), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPantryPickerIndex = null }) { Text("Close") }
            }
        )
    }

    // Modal 3: Save Success Dialog
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            title = { Text("✅ Batch Logged Successfully", fontWeight = FontWeight.Bold) },
            text = {
                Text("'$batchTitle' ($yieldCount $yieldUnitText) has been recorded in your Production History.\n\nTotal Batch Cost: ${CurrencyFormatter.format(totalBatchCost, currencySymbol)}\nCost per Unit: ${CurrencyFormatter.format(costPerUnit, currencySymbol)}\nEst. Profit: ${CurrencyFormatter.format(totalBatchProfit, currencySymbol)}")
            },
            confirmButton = {
                Button(onClick = { showSavedDialog = false }) {
                    Text("Awesome")
                }
            }
        )
    }
}

@Composable
fun UnitDropdownSelector(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Unit"
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UnitConverter.COMMON_UNITS.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun buildCostSheetShareText(
    batchTitle: String,
    yieldCount: Int,
    yieldUnit: String,
    ingredients: List<CustomBatchIngredient>,
    rawIngTotal: Double,
    overheadsTotal: Double,
    totalCost: Double,
    costPerUnit: Double,
    sellingPrice: Double,
    profitPerUnit: Double,
    marginPct: Double,
    markupPct: Double,
    isGst: Boolean,
    gstRate: Double,
    finalPrice: Double,
    currencySymbol: String
): String {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    val sb = StringBuilder()
    sb.appendLine("📋 BATCH COST SHEET: $batchTitle")
    sb.appendLine("📅 Date: $dateStr")
    sb.appendLine("📦 Output Yield: $yieldCount $yieldUnit")
    sb.appendLine("─────────────────────────")
    sb.appendLine("🧂 INGREDIENTS:")
    ingredients.forEachIndexed { i, ing ->
        sb.appendLine("  ${i + 1}. ${ing.name}: ${ing.usedQty} ${ing.usedUnit} = ${CurrencyFormatter.format(ing.calculatedCost(), currencySymbol)}")
    }
    sb.appendLine("  Subtotal Ingredients: ${CurrencyFormatter.format(rawIngTotal, currencySymbol)}")
    sb.appendLine("─────────────────────────")
    sb.appendLine("📦 OVERHEADS & PACKAGING: ${CurrencyFormatter.format(overheadsTotal, currencySymbol)}")
    sb.appendLine("💰 TOTAL BATCH COST: ${CurrencyFormatter.format(totalCost, currencySymbol)}")
    sb.appendLine("🎯 COST PER UNIT: ${CurrencyFormatter.format(costPerUnit, currencySymbol)}")
    sb.appendLine("🏷️ SELLING PRICE PER UNIT: ${CurrencyFormatter.format(sellingPrice, currencySymbol)}")
    sb.appendLine("💵 NET PROFIT PER UNIT: ${CurrencyFormatter.format(profitPerUnit, currencySymbol)}")
    sb.appendLine("📊 PROFIT MARGIN: ${String.format("%.1f", marginPct)}%")
    sb.appendLine("📈 MARKUP: ${String.format("%.1f", markupPct)}%")
    if (isGst && gstRate > 0) {
        sb.appendLine("─────────────────────────")
        sb.appendLine("🧾 GST ($gstRate%): +${CurrencyFormatter.format(finalPrice - sellingPrice, currencySymbol)}")
        sb.appendLine("🛒 FINAL INVOICE PRICE: ${CurrencyFormatter.format(finalPrice, currencySymbol)}")
    }
    sb.appendLine("─────────────────────────")
    sb.appendLine("Generated by BatchCost Bakery Pro")
    return sb.toString()
}
