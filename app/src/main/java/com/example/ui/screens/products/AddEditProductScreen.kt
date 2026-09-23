package com.example.ui.screens.products

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.ProductWithDetails
import com.example.ui.TempIngredientItem
import com.example.ui.components.SharedIngredientEditCard
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    existingProductWithDetails: ProductWithDetails? = null,
    currencySymbol: String,
    onBack: () -> Unit,
    onSaveProduct: (ProductEntity, List<TempIngredientItem>) -> Unit
) {
    val isEdit = existingProductWithDetails != null
    val existingProd = existingProductWithDetails?.product

    var pName by remember { mutableStateOf(existingProd?.name ?: "") }
    var price by remember { mutableStateOf(existingProd?.sellingPrice?.toString() ?: "500") }
    var unit by remember { mutableStateOf(existingProd?.unit ?: "Piece") }
    var category by remember { mutableStateOf(existingProd?.category ?: "General") }
    var weight by remember { mutableStateOf(existingProd?.weight ?: "500g") }

    var packagingCost by remember { mutableStateOf(existingProd?.packagingCost?.toString() ?: "17") }

    // 👷 LABOUR COST METHOD & INPUTS
    var labourMethod by remember { mutableStateOf("Fixed Batch") } // "Fixed Batch", "Hourly Rate"
    var fixedLabourCostText by remember { mutableStateOf(existingProd?.labourCost?.toString() ?: "20") }
    var hourlyWorkerRateText by remember { mutableStateOf("100") }
    var hourlyTimeHoursText by remember { mutableStateOf("4") }
    var workersCountText by remember { mutableStateOf("1") }

    val calculatedLabourCost: Double = if (labourMethod == "Fixed Batch") {
        fixedLabourCostText.toDoubleOrNull() ?: 0.0
    } else {
        val rate = hourlyWorkerRateText.toDoubleOrNull() ?: 0.0
        val hrs = hourlyTimeHoursText.toDoubleOrNull() ?: 0.0
        val count = workersCountText.toDoubleOrNull() ?: 1.0
        rate * hrs * count
    }

    // ⚡ OVERHEAD COST METHOD & INPUTS
    var overheadMethod by remember { mutableStateOf("Per Batch") } // "Per Batch", "Monthly Allocation"
    var fixedOverheadText by remember { mutableStateOf(existingProd?.electricityGasCost?.toString() ?: "10") }
    var monthlyElectricityText by remember { mutableStateOf("3000") }
    var monthlyGasText by remember { mutableStateOf("1500") }
    var monthlyRentText by remember { mutableStateOf("5000") }
    var expectedMonthlyBatchesText by remember { mutableStateOf("30") }

    val calculatedOverheadCost: Double = if (overheadMethod == "Per Batch") {
        fixedOverheadText.toDoubleOrNull() ?: 0.0
    } else {
        val elec = monthlyElectricityText.toDoubleOrNull() ?: 0.0
        val gas = monthlyGasText.toDoubleOrNull() ?: 0.0
        val rent = monthlyRentText.toDoubleOrNull() ?: 0.0
        val totalMonthly = elec + gas + rent
        val batches = expectedMonthlyBatchesText.toDoubleOrNull() ?: 30.0
        if (batches > 0) totalMonthly / batches else 0.0
    }

    val ingredients = remember {
        mutableStateListOf<TempIngredientItem>().apply {
            if (existingProductWithDetails != null) {
                addAll(
                    existingProductWithDetails.recipeIngredients.map {
                        TempIngredientItem(
                            name = it.ingredientName,
                            purchaseQty = it.purchaseQty.toString(),
                            purchaseUnit = it.purchaseUnit,
                            purchasePrice = it.purchasePrice.toString(),
                            usedQty = it.usedQty.toString(),
                            usedUnit = it.usedUnit
                        )
                    }
                )
            } else {
                add(TempIngredientItem(name = "Maida", purchaseQty = "1", purchaseUnit = "kg", purchasePrice = "50", usedQty = "250", usedUnit = "g"))
                add(TempIngredientItem(name = "Sugar", purchaseQty = "1", purchaseUnit = "kg", purchasePrice = "60", usedQty = "150", usedUnit = "g"))
                add(TempIngredientItem(name = "Butter", purchaseQty = "1", purchaseUnit = "kg", purchasePrice = "500", usedQty = "200", usedUnit = "g", wastageType = "Actual waste", actualWasteQty = "50"))
                add(TempIngredientItem(name = "Chocolate", purchaseQty = "1", purchaseUnit = "kg", purchasePrice = "700", usedQty = "150", usedUnit = "g", wastageType = "Fixed %", wastagePercent = "10"))
                add(TempIngredientItem(name = "Egg", purchaseQty = "12", purchaseUnit = "piece", purchasePrice = "84", usedQty = "4", usedUnit = "piece"))
            }
        }
    }

    val totalRawIngredientCost = ingredients.sumOf { it.calculatedCost() }
    val pkgVal = packagingCost.toDoubleOrNull() ?: 0.0

    val totalUnitCost = totalRawIngredientCost + pkgVal + calculatedLabourCost + calculatedOverheadCost
    val sellVal = price.toDoubleOrNull() ?: 0.0
    val netProfit = sellVal - totalUnitCost
    val margin = if (sellVal > 0) (netProfit / sellVal) * 100.0 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Product Recipe" else "Add New Product Recipe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Cost Calculation Card Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Calculated Unit Cost", style = MaterialTheme.typography.labelMedium)
                        Text(
                            CurrencyFormatter.format(totalUnitCost, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated Net Profit", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${CurrencyFormatter.format(netProfit, currencySymbol)} (${CurrencyFormatter.formatPercent(margin)})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text("Product Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                item {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Product Name *") },
                        placeholder = { Text("e.g. Chocolate Cake") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_product_name_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Selling Price ($currencySymbol) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_product_price_input")
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit *") },
                            placeholder = { Text("Piece / Box / Kg") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Net Weight / Size") },
                            placeholder = { Text("e.g. 500g") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = packagingCost,
                        onValueChange = { packagingCost = it },
                        label = { Text("Packaging Cost per Unit ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 👷 LABOUR COST SECTION (Method 1 vs Method 2)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👷 Labour Cost Calculation Method:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fixed Batch", "Hourly Rate").forEach { method ->
                                    FilterChip(
                                        selected = labourMethod == method,
                                        onClick = { labourMethod = method },
                                        label = { Text(if (method == "Fixed Batch") "Method 1: Fixed Batch" else "Method 2: Hourly Rate") },
                                        modifier = Modifier.testTag("labour_method_chip_${method.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }

                            if (labourMethod == "Fixed Batch") {
                                OutlinedTextField(
                                    value = fixedLabourCostText,
                                    onValueChange = { fixedLabourCostText = it },
                                    label = { Text("Fixed Batch Labour ($currencySymbol/batch)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("fixed_labour_input"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = hourlyWorkerRateText,
                                            onValueChange = { hourlyWorkerRateText = it },
                                            label = { Text("Worker Rate ($currencySymbol/hr)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        OutlinedTextField(
                                            value = hourlyTimeHoursText,
                                            onValueChange = { hourlyTimeHoursText = it },
                                            label = { Text("Hours Taken") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        OutlinedTextField(
                                            value = workersCountText,
                                            onValueChange = { workersCountText = it },
                                            label = { Text("Workers") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                    Text(
                                        text = "⚡ Hourly Labour = $hourlyWorkerRateText/hr × $hourlyTimeHoursText hrs × $workersCountText workers = ${CurrencyFormatter.format(calculatedLabourCost, currencySymbol)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ⚡ OVERHEAD COST SECTION (Per Batch vs Monthly Allocation)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡ Overhead Cost Allocation (Elec, Gas, Rent):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Per Batch", "Monthly Allocation").forEach { method ->
                                    FilterChip(
                                        selected = overheadMethod == method,
                                        onClick = { overheadMethod = method },
                                        label = { Text(if (method == "Per Batch") "Per Batch Overhead" else "Monthly Bill Allocation") },
                                        modifier = Modifier.testTag("overhead_method_chip_${method.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }

                            if (overheadMethod == "Per Batch") {
                                OutlinedTextField(
                                    value = fixedOverheadText,
                                    onValueChange = { fixedOverheadText = it },
                                    label = { Text("Direct Batch Overhead (Elec/Gas/Water) ($currencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("fixed_overhead_input"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = monthlyElectricityText,
                                            onValueChange = { monthlyElectricityText = it },
                                            label = { Text("Monthly Elec ($currencySymbol)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        OutlinedTextField(
                                            value = monthlyGasText,
                                            onValueChange = { monthlyGasText = it },
                                            label = { Text("Monthly Gas ($currencySymbol)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        OutlinedTextField(
                                            value = monthlyRentText,
                                            onValueChange = { monthlyRentText = it },
                                            label = { Text("Monthly Rent ($currencySymbol)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = expectedMonthlyBatchesText,
                                        onValueChange = { expectedMonthlyBatchesText = it },
                                        label = { Text("Expected Monthly Batches Count (e.g. 30)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Text(
                                        text = "⚡ Allocated Overhead = (${CurrencyFormatter.format((monthlyElectricityText.toDoubleOrNull() ?: 0.0) + (monthlyGasText.toDoubleOrNull() ?: 0.0) + (monthlyRentText.toDoubleOrNull() ?: 0.0), currencySymbol)}) ÷ $expectedMonthlyBatchesText batches = ${CurrencyFormatter.format(calculatedOverheadCost, currencySymbol)}/batch",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recipe Ingredients (${ingredients.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { ingredients.add(TempIngredientItem()) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                itemsIndexed(ingredients) { index, item ->
                    SharedIngredientEditCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onUpdate = { updatedItem -> ingredients[index] = updatedItem },
                        onDelete = { if (ingredients.size > 1) ingredients.removeAt(index) }
                    )
                }
            }

            Button(
                onClick = {
                    if (pName.isNotBlank() && price.toDoubleOrNull() != null) {
                        val productObj = ProductEntity(
                            id = existingProd?.id ?: 0,
                            name = pName,
                            sellingPrice = sellVal,
                            unit = unit,
                            category = category,
                            weight = weight.ifBlank { null },
                            packagingCost = pkgVal,
                            labourCost = calculatedLabourCost,
                            electricityGasCost = calculatedOverheadCost,
                            wastagePercent = 0.0
                        )
                        onSaveProduct(productObj, ingredients.toList())
                    }
                },
                enabled = pName.isNotBlank() && price.toDoubleOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_recipe_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Product & Recipe", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
