package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.ProductWithDetails
import com.example.ui.RecordedOrder
import com.example.ui.components.ProductCard
import com.example.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun DashboardScreen(
    profile: BusinessProfileEntity,
    products: List<ProductWithDetails>,
    recordedOrders: List<RecordedOrder>,
    batchCount: Int,
    onNavigateToProducts: () -> Unit,
    onNavigateToBatchCalc: () -> Unit,
    onSelectProduct: (Long) -> Unit,
    onAddNewProduct: () -> Unit,
    onRecordNewOrder: (customerName: String, productName: String, qty: Int, revenue: Double, cost: Double) -> Unit,
    onIncrementBatchCount: () -> Unit
) {
    val currencySym = profile.currencySymbol

    var showRecordOrderDialog by remember { mutableStateOf(false) }
    var showCreateLabelModal by remember { mutableStateOf(false) }
    var showWhatsAppModal by remember { mutableStateOf(false) }
    var showAIAssistantSheet by remember { mutableStateOf(false) }
    var showAIRecipeModal by remember { mutableStateOf(false) }
    var showPriceListModal by remember { mutableStateOf(false) }
    var showCrmSheet by remember { mutableStateOf(false) }
    var showRemindersModal by remember { mutableStateOf(false) }
    var showMultiBusinessModal by remember { mutableStateOf(false) }
    var showTeamModal by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }

    // Financial Calculation for "This Month"
    val orderRevenue = recordedOrders.sumOf { it.totalRevenue }
    val orderCost = recordedOrders.sumOf { it.totalCost }

    val displayRevenue = orderRevenue
    val displayCost = orderCost
    val displayProfit = displayRevenue - displayCost
    val displayMargin = if (displayRevenue > 0) (displayProfit / displayRevenue) * 100.0 else 0.0

    val displayProductsCount = products.size
    val displayBatchesCount = batchCount
    val displayOrdersCount = recordedOrders.size
    val lowStockCount = 0 // Future: implement real stock tracking

    // Dynamic greeting based on hour
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = when {
        hour in 5..11 -> "Good morning 👋"
        hour in 12..16 -> "Good afternoon 👋"
        else -> "Good evening 👋"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP GREETING HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_top_greeting")
            ) {
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${profile.businessName} • ${profile.ownerName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // THIS MONTH FINANCIAL CARDS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📊 Business Dashboard Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialSummaryCard(
                            title = "Revenue",
                            value = CurrencyFormatter.format(displayRevenue, currencySym),
                            containerColor = Color(0xFFEFF6FF),
                            textColor = Color(0xFF1D4ED8),
                            modifier = Modifier.weight(1f)
                        )

                        FinancialSummaryCard(
                            title = "Cost",
                            value = CurrencyFormatter.format(displayCost, currencySym),
                            containerColor = Color(0xFFFEF2F2),
                            textColor = Color(0xFFB91C1C),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialSummaryCard(
                            title = "Net Profit",
                            value = CurrencyFormatter.format(displayProfit, currencySym),
                            containerColor = Color(0xFFECFDF5),
                            textColor = Color(0xFF047857),
                            modifier = Modifier.weight(1f)
                        )

                        FinancialSummaryCard(
                            title = "Margin",
                            value = CurrencyFormatter.formatPercent(displayMargin),
                            containerColor = Color(0xFFFAF5FF),
                            textColor = Color(0xFF6B21A8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // QUICK ACTIONS SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionButton(
                            label = "💬 Ask AI Business Assistant",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { showAIAssistantSheet = true },
                            testTag = "action_ai_assistant"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "🤖 AI Recipe & Photo Importer",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { showAIRecipeModal = true },
                            testTag = "action_ai_recipe_importer"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "📋 Price List Generator",
                            icon = Icons.Default.Receipt,
                            onClick = { showPriceListModal = true },
                            testTag = "action_price_list"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "👥 Customer CRM",
                            icon = Icons.Default.ShoppingBag,
                            onClick = { showCrmSheet = true },
                            testTag = "action_customer_crm"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "🔔 Smart Reminders",
                            icon = Icons.Default.Storefront,
                            onClick = { showRemindersModal = true },
                            testTag = "action_smart_reminders"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "🏪 Multi-Business",
                            icon = Icons.Default.Storefront,
                            onClick = { showMultiBusinessModal = true },
                            testTag = "action_multi_business"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "👨💼 Team & Roles",
                            icon = Icons.Default.ShoppingBag,
                            onClick = { showTeamModal = true },
                            testTag = "action_team_accounts"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "🔐 Firebase Auth",
                            icon = Icons.Default.Receipt,
                            onClick = { showAuthModal = true },
                            testTag = "action_firebase_auth"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "+ Create Batch",
                            icon = Icons.Default.Calculate,
                            onClick = {
                                onIncrementBatchCount()
                                onNavigateToBatchCalc()
                            },
                            testTag = "action_create_batch"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "+ Add Product",
                            icon = Icons.Default.Add,
                            onClick = onAddNewProduct,
                            testTag = "action_add_product"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "+ Record Order",
                            icon = Icons.Default.Receipt,
                            onClick = { showRecordOrderDialog = true },
                            testTag = "action_record_order"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "🏷️ Create Label",
                            icon = Icons.Default.Label,
                            onClick = { showCreateLabelModal = true },
                            testTag = "action_create_label"
                        )
                    }

                    item {
                        QuickActionButton(
                            label = "📷 Import WhatsApp Order",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { showWhatsAppModal = true },
                            testTag = "action_import_whatsapp"
                        )
                    }
                }
            }
        }

        // BUSINESS SNAPSHOT SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Business Snapshot",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SnapshotChip(title = "Products", count = "$displayProductsCount", modifier = Modifier.weight(1f))
                    SnapshotChip(title = "Batches", count = "$displayBatchesCount", modifier = Modifier.weight(1f))
                    SnapshotChip(title = "Orders", count = "$displayOrdersCount", modifier = Modifier.weight(1f))
                    SnapshotChip(
                        title = "Low Stock",
                        count = "$lowStockCount",
                        isWarning = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // INSIGHTS SECTION
        if (recordedOrders.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    InsightCard(
                        icon = Icons.Default.TrendingUp,
                        title = "Top Performer",
                        description = "📈 ${recordedOrders.groupBy { it.productName }.maxByOrNull { it.value.size }?.key ?: "N/A"} is your most ordered product.",
                        containerColor = Color(0xFFECFDF5),
                        iconColor = Color(0xFF059669)
                    )

                    InsightCard(
                        icon = Icons.Default.Lightbulb,
                        title = "Profit Optimization",
                        description = "💡 Keep recording batches to see cost trends and inflation alerts.",
                        containerColor = Color(0xFFEFF6FF),
                        iconColor = Color(0xFF2563EB)
                    )
                }
            }
        }

        // 🏆 PRODUCT PERFORMANCE BREAKDOWN
        if (recordedOrders.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .testTag("product_performance_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🏆 Product Performance Ranking", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        val performance = recordedOrders.groupBy { it.productName }
                            .map { (name, list) -> 
                                val rev = list.sumOf { it.totalRevenue }
                                val cost = list.sumOf { it.totalCost }
                                val profit = rev - cost
                                val margin = if (rev > 0) (profit / rev) * 100 else 0.0
                                Triple(name, "Revenue: ${currencySym}${String.format("%.0f", rev)}", "Profit: ${currencySym}${String.format("%.0f", profit)} (${String.format("%.1f", margin)}%)")
                            }
                            .sortedByDescending { it.first } // Simplified sorting

                        performance.take(4).forEach { (pName, revText, profitText) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(pName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(revText, fontSize = 12.sp, color = Color(0xFF1E40AF))
                                }
                                Text(profitText, fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 📈 PRODUCT COST TREND & INFLATION TRACKER
        // Hidden in zero-state

        // PRODUCTS RECENT LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Catalog (${products.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToProducts() }
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
                        Text("No products added yet.", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddNewProduct) {
                            Text("Create First Product")
                        }
                    }
                }
            }
        } else {
            items(products) { productWithDetails ->
                ProductCard(
                    productWithDetails = productWithDetails,
                    currencySymbol = currencySym,
                    onClick = { onSelectProduct(productWithDetails.product.id) }
                )
            }
        }
    }

    // Interactive Dialogs
    if (showRecordOrderDialog) {
        RecordOrderDialog(
            products = products,
            currencySymbol = currencySym,
            onDismiss = { showRecordOrderDialog = false },
            onConfirmOrder = { customerName, productName, qty, revenue, cost ->
                onRecordNewOrder(customerName, productName, qty, revenue, cost)
                showRecordOrderDialog = false
            }
        )
    }

    if (showPriceListModal) {
        com.example.ui.components.PriceListGeneratorDialog(
            products = products,
            profile = profile,
            onDismiss = { showPriceListModal = false }
        )
    }

    if (showCrmSheet) {
        com.example.ui.components.CustomerCrmSheet(
            recordedOrders = recordedOrders,
            currencySymbol = currencySym,
            onDismiss = { showCrmSheet = false },
            onCreateRepeatOrder = { customer ->
                showCrmSheet = false
                onRecordNewOrder(customer.name, customer.lastProduct, customer.lastQuantity, 400.0, 200.0)
            }
        )
    }

    if (showRemindersModal) {
        com.example.ui.components.RemindersDialog(
            onDismiss = { showRemindersModal = false }
        )
    }

    if (showMultiBusinessModal) {
        com.example.ui.components.MultipleBusinessSelectorDialog(
            currentBusinessName = profile.businessName,
            onDismiss = { showMultiBusinessModal = false },
            onSelectBusiness = { bus ->
                showMultiBusinessModal = false
            }
        )
    }

    if (showTeamModal) {
        com.example.ui.components.TeamManagementSheet(
            onDismiss = { showTeamModal = false }
        )
    }

    if (showAuthModal) {
        com.example.ui.components.FirebaseAuthDialog(
            currentUserEmail = profile.email ?: "",
            onDismiss = { showAuthModal = false },
            onLoginSuccess = { email ->
                showAuthModal = false
            }
        )
    }

    if (showWhatsAppModal) {
        WhatsAppImportModal(
            products = products,
            currencySymbol = currencySym,
            onDismiss = { showWhatsAppModal = false },
            onImportSuccess = { customerName, productName, qty, revenue, cost ->
                onRecordNewOrder(customerName, productName, qty, revenue, cost)
                showWhatsAppModal = false
            }
        )
    }

    if (showAIAssistantSheet) {
        com.example.ui.components.AIBusinessAssistantSheet(
            products = products,
            masterIngredients = emptyList(),
            recordedOrders = recordedOrders,
            currencySymbol = currencySym,
            onDismiss = { showAIAssistantSheet = false }
        )
    }

    if (showAIRecipeModal) {
        com.example.ui.components.AIRecipeBuilderDialog(
            masterIngredients = emptyList(),
            currencySymbol = currencySym,
            onDismiss = { showAIRecipeModal = false },
            onConfirmImport = { productName, yieldUnits, ingredients ->
                showAIRecipeModal = false
                onAddNewProduct()
            }
        )
    }
}

@Composable
fun FinancialSummaryCard(
    title: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SnapshotChip(
    title: String,
    count: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.border(
            width = 1.dp,
            color = if (isWarning) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isWarning) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InsightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    containerColor: Color,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = iconColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
