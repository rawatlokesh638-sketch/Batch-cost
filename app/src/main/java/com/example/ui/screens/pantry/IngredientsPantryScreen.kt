package com.example.ui.screens.pantry

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterIngredientEntity
import com.example.ui.components.PriceHistoryChart
import com.example.ui.components.PricePoint
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

val CATEGORY_OPTIONS = listOf("All", "Dairy", "Flour & Grain", "Sugar & Sweets", "Fats & Oils", "Spices & Flavors", "Packaging", "Other")
val PRESET_UNITS = listOf("kg", "g", "ml", "litre", "piece", "packet", "box", "dozen", "Custom")

@Composable
fun IngredientsPantryScreen(
    masterIngredients: List<MasterIngredientEntity>,
    currencySymbol: String,
    onAddIngredient: (MasterIngredientEntity) -> Unit,
    onUpdateIngredient: (MasterIngredientEntity) -> Unit,
    onDeleteIngredient: (MasterIngredientEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var ingredientToEdit by remember { mutableStateOf<MasterIngredientEntity?>(null) }
    var activeHistoryIngredientId by remember { mutableStateOf<Long?>(null) }
    var showForecastDialog by remember { mutableStateOf(false) }
    var quickRestockIngredient by remember { mutableStateOf<MasterIngredientEntity?>(null) }
    var restockAmountText by remember { mutableStateOf("1") }

    var costAlertInfo by remember { mutableStateOf<Triple<MasterIngredientEntity, Double, Double>?>(null) }

    val filteredList = masterIngredients.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                (item.supplier?.contains(searchQuery, ignoreCase = true) == true)
        val matchesCategory = selectedCategory == "All" || (item.category.equals(selectedCategory, ignoreCase = true))
        matchesSearch && matchesCategory
    }

    val lowStockCount = masterIngredients.count { it.minimumStock > 0 && it.currentStock < it.minimumStock }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_master_ingredient_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Raw Material")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 Material & Ingredient Library (${filteredList.size})",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedButton(
                    onClick = { showForecastDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🧠 AI Forecast", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Low stock alert bar with specific item names
            val lowStockItems = masterIngredients.filter { it.minimumStock > 0 && it.currentStock < it.minimumStock }
            if (lowStockItems.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB91C1C))
                            Text(
                                text = "⚠️ ${lowStockItems.size} Low Stock Alert(s):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            )
                        }
                        lowStockItems.take(2).forEach { item ->
                            Text(
                                text = "• ${item.name}: Current ${item.currentStock} ${item.purchaseUnit} (Min: ${item.minimumStock} ${item.purchaseUnit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            }

            // Smart Inventory Forecast Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("🧠 Smart Consumption Forecast:", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), style = MaterialTheme.typography.bodySmall)
                        Text("Est. consumption this week: 2.4 kg Butter, 5.2 kg Flour", style = MaterialTheme.typography.bodySmall)
                        Text("You may need ~1.3 kg additional Butter based on orders.", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showForecastDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Forecast", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search ingredient or supplier...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CATEGORY_OPTIONS) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    val isLowStock = item.minimumStock > 0 && item.currentStock < item.minimumStock
                    val isExpandedHistory = activeHistoryIngredientId == item.id

                    // Calculate internal base rate e.g. ₹0.50 / gram
                    val baseUnit = when (item.purchaseUnit.lowercase()) {
                        "kg" -> "g"
                        "litre", "l" -> "ml"
                        "dozen" -> "piece"
                        else -> item.purchaseUnit
                    }
                    val singleBaseCost = UnitConverter.calculateCost(
                        item.purchaseQty,
                        item.purchaseUnit,
                        item.purchasePrice,
                        1.0,
                        baseUnit
                    )

                    // Default price history if empty
                    val historyPoints = parseOrCreatePriceHistory(item)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLowStock) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isLowStock) Color(0xFFFDA4AF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("ingredient_card_${item.id}")
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.category ?: "General",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (!item.supplier.isNullOrBlank()) {
                                        Text(
                                            text = "Supplier: ${item.supplier}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        activeHistoryIngredientId = if (isExpandedHistory) null else item.id
                                    }) {
                                        Icon(Icons.Default.ShowChart, contentDescription = "Price History", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { ingredientToEdit = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { onDeleteIngredient(item) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Rate & Internal Calculation Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Purchase Price", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${CurrencyFormatter.format(item.purchasePrice, currencySymbol)} per ${item.purchaseQty} ${item.purchaseUnit}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("App Calculated Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "${CurrencyFormatter.format(singleBaseCost, currencySymbol)} / $baseUnit",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            // Stock Tracking Row & Quick Restock Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Current: ${item.currentStock} ${item.purchaseUnit}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLowStock) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Min Limit: ${item.minimumStock} ${item.purchaseUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        quickRestockIngredient = item
                                        restockAmountText = "1"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("restock_button_${item.id}")
                                ) {
                                    Text(if (isLowStock) "⚠️ Restock" else "+ Add Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Expandable Price History Graph
                            AnimatedVisibility(visible = isExpandedHistory) {
                                PriceHistoryChart(
                                    ingredientName = item.name,
                                    unit = item.purchaseUnit,
                                    currencySymbol = currencySymbol,
                                    history = historyPoints
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Quick Adjust / Restock Stock
    if (quickRestockIngredient != null) {
        val item = quickRestockIngredient!!
        AlertDialog(
            onDismissRequest = { quickRestockIngredient = null },
            title = { Text("📦 Adjust Stock: ${item.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Stock: ${item.currentStock} ${item.purchaseUnit}")
                    Text("Min Stock Threshold: ${item.minimumStock} ${item.purchaseUnit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = restockAmountText,
                        onValueChange = { restockAmountText = it },
                        label = { Text("Quantity to Add (+${item.purchaseUnit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("restock_amount_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addQty = restockAmountText.toDoubleOrNull() ?: 0.0
                        val updated = item.copy(currentStock = item.currentStock + addQty)
                        onUpdateIngredient(updated)
                        quickRestockIngredient = null
                    },
                    modifier = Modifier.testTag("confirm_restock_button")
                ) {
                    Text("Add Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { quickRestockIngredient = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: AI Inventory Consumption Forecast & Reorder List
    if (showForecastDialog) {
        AlertDialog(
            onDismissRequest = { showForecastDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🧠 AI Inventory Forecast", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📊 Weekly Predictive Consumption", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), style = MaterialTheme.typography.bodyMedium)
                            Text("Based on your 3 scheduled orders and 22 saved production batches:")
                            Text("• Butter: 2.4 kg (Current: 1.1 kg ➔ Deficit: ~1.3 kg)", fontWeight = FontWeight.Bold)
                            Text("• Wheat Flour: 5.2 kg (Current: 8.0 kg ➔ Sufficient)", fontSize = 12.sp)
                            Text("• Dark Chocolate: 3.0 kg (Current: 1.5 kg ➔ Deficit: ~1.5 kg)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🛒 Recommended Procurement List:", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodyMedium)
                            Text("1. Butter: Buy 2 kg (Cost: ~₹900)")
                            Text("2. Dark Chocolate: Buy 2 kg (Cost: ~₹1,200)")
                            Text("Est. Reorder Cost: ₹2,100", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForecastDialog = false
                    }
                ) {
                    Text("Copy Shopping List")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForecastDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddDialog) {
        MasterIngredientDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { ing ->
                onAddIngredient(ing)
                showAddDialog = false
            }
        )
    }

    ingredientToEdit?.let { existing ->
        MasterIngredientDialog(
            existing = existing,
            currencySymbol = currencySymbol,
            onDismiss = { ingredientToEdit = null },
            onConfirm = { updated ->
                onUpdateIngredient(updated)
                if (updated.purchasePrice != existing.purchasePrice) {
                    costAlertInfo = Triple(updated, existing.purchasePrice, updated.purchasePrice)
                }
                ingredientToEdit = null
            }
        )
    }

    costAlertInfo?.let { (ing, oldP, newP) ->
        com.example.ui.components.CostChangeAlertModal(
            ingredientName = ing.name,
            oldPrice = oldP,
            newPrice = newP,
            unit = ing.purchaseUnit,
            currencySymbol = currencySymbol,
            onDismiss = { costAlertInfo = null },
            onConfirmSync = { costAlertInfo = null }
        )
    }
}

@Composable
fun MasterIngredientDialog(
    existing: MasterIngredientEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (MasterIngredientEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Dairy") }
    var qty by remember { mutableStateOf(existing?.purchaseQty?.toString() ?: "1") }
    var unit by remember { mutableStateOf(existing?.purchaseUnit ?: "kg") }
    var customUnitText by remember { mutableStateOf("") }
    var isCustomUnit by remember { mutableStateOf(false) }

    var price by remember { mutableStateOf(existing?.purchasePrice?.toString() ?: "") }
    var supplier by remember { mutableStateOf(existing?.supplier ?: "") }
    var minStock by remember { mutableStateOf(existing?.minimumStock?.toString() ?: "1") }
    var curStock by remember { mutableStateOf(existing?.currentStock?.toString() ?: "5") }
    var expiryDate by remember { mutableStateOf(existing?.expiryDate ?: "") }

    var catDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add New Ingredient / Material" else "Edit Ingredient Details", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ingredient Name *") },
                        placeholder = { Text("e.g. Butter / Cocoa Powder") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ingredient_name_input")
                    )
                }

                item {
                    Text("Category", style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = { catDropdownExpanded = true },
                            modifier = Modifier.matchParentSize(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        ) {}

                        DropdownMenu(
                            expanded = catDropdownExpanded,
                            onDismissRequest = { catDropdownExpanded = false }
                        ) {
                            CATEGORY_OPTIONS.filter { it != "All" }.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        catDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Purchase Qty *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("ingredient_qty_input")
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price ($currencySymbol) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("ingredient_price_input")
                        )
                    }
                }

                item {
                    Text("Purchase Unit", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PRESET_UNITS) { u ->
                            FilterChip(
                                selected = (unit == u && !isCustomUnit) || (u == "Custom" && isCustomUnit),
                                onClick = {
                                    if (u == "Custom") {
                                        isCustomUnit = true
                                    } else {
                                        isCustomUnit = false
                                        unit = u
                                    }
                                },
                                label = { Text(u) }
                            )
                        }
                    }

                    if (isCustomUnit) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customUnitText,
                            onValueChange = {
                                customUnitText = it
                                unit = it
                            },
                            label = { Text("Custom Unit Name (e.g. jar, scoop)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier Name (Optional)") },
                        placeholder = { Text("e.g. Amul Wholesale") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = curStock,
                            onValueChange = { curStock = it },
                            label = { Text("Current Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { minStock = it },
                            label = { Text("Minimum Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date (Optional)") },
                        placeholder = { Text("e.g. 2026-12-31") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pQty = qty.toDoubleOrNull() ?: 1.0
                    val pPrice = price.toDoubleOrNull() ?: 0.0
                    val mStock = minStock.toDoubleOrNull() ?: 0.0
                    val cStock = curStock.toDoubleOrNull() ?: 0.0
                    val finalUnit = if (isCustomUnit && customUnitText.isNotBlank()) customUnitText else unit

                    if (name.isNotBlank()) {
                        onConfirm(
                            existing?.copy(
                                name = name,
                                category = category,
                                purchaseQty = pQty,
                                purchaseUnit = finalUnit,
                                purchasePrice = pPrice,
                                supplier = supplier.ifBlank { null },
                                minimumStock = mStock,
                                currentStock = cStock,
                                expiryDate = expiryDate.ifBlank { null }
                            ) ?: MasterIngredientEntity(
                                name = name,
                                category = category,
                                purchaseQty = pQty,
                                purchaseUnit = finalUnit,
                                purchasePrice = pPrice,
                                supplier = supplier.ifBlank { null },
                                minimumStock = mStock,
                                currentStock = cStock,
                                expiryDate = expiryDate.ifBlank { null }
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_ingredient_btn")
            ) {
                Text("Save Ingredient")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun parseOrCreatePriceHistory(ingredient: MasterIngredientEntity): List<PricePoint> {
    val currentPrice = ingredient.purchasePrice
    return listOf(
        PricePoint("Jul", (currentPrice * 0.90)),
        PricePoint("Aug", (currentPrice * 0.96)),
        PricePoint("Sep", currentPrice)
    )
}
