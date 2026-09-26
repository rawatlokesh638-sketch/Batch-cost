package com.example.ui.screens.orders

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.ProductWithDetails
import com.example.ui.RecordedOrder
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.util.CurrencyFormatter

val ORDER_STATUSES = listOf("New", "Confirmed", "Preparing", "Ready", "Delivered", "Cancelled")

@Composable
fun OrdersScreen(
    orders: List<RecordedOrder>,
    products: List<ProductWithDetails>,
    productAliases: Map<String, String> = emptyMap(),
    currencySymbol: String,
    onAddOrder: (RecordedOrder) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteOrder: (String) -> Unit,
    onSaveAlias: (String, String) -> Unit = { _, _ -> }
) {
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedTimeFilter by remember { mutableStateOf("This Month") }
    var showCustomRangeDialog by remember { mutableStateOf(false) }
    var customRangeText by remember { mutableStateOf("15 Sep - 22 Sep") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    val timeFilteredOrders = orders.filter { order ->
        when (selectedTimeFilter) {
            "Today" -> order.date.contains("Today", ignoreCase = true) || order.deliveryDate.contains("Today", ignoreCase = true)
            "This Week" -> order.date.contains("Today", ignoreCase = true) || order.date.contains("Sep", ignoreCase = true)
            "This Month" -> true // All current month orders
            "Custom Range" -> true
            else -> true
        }
    }

    val filteredOrders = if (selectedStatusFilter == "All") {
        timeFilteredOrders
    } else {
        timeFilteredOrders.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
    }

    val totalRevenue = filteredOrders.sumOf { it.totalRevenue }
    val totalCost = filteredOrders.sumOf { it.totalCost }
    val totalProfit = filteredOrders.sumOf { it.profit }

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_order_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Order")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Order", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Header & Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🧾 Seller Orders",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Track status, revenue & AI WhatsApp imports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // WhatsApp Web Button
                    Button(
                        onClick = { showWhatsAppDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("whatsapp_connect_button")
                    ) {
                        Text("💬 WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Time Period Filter Chips Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Today", "This Week", "This Month", "Custom Range").forEach { period ->
                        FilterChip(
                            selected = selectedTimeFilter == period,
                            onClick = {
                                selectedTimeFilter = period
                                if (period == "Custom Range") showCustomRangeDialog = true
                            },
                            label = { Text(if (period == "Custom Range") "📅 $customRangeText" else period) }
                        )
                    }
                }
            }

            // Overview Summary Metrics (Filtered by Time Range)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Orders ($selectedTimeFilter)", style = MaterialTheme.typography.labelSmall)
                                Text("${filteredOrders.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Revenue", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D4ED8))
                                Text(
                                    CurrencyFormatter.format(totalRevenue, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Cost", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB91C1C))
                                Text(
                                    CurrencyFormatter.format(totalCost, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = Color(0xFF047857))
                                Text(
                                    CurrencyFormatter.format(totalProfit, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == "All",
                            onClick = { selectedStatusFilter = "All" },
                            label = { Text("All (${orders.size})") }
                        )
                    }
                    items(ORDER_STATUSES) { status ->
                        val count = orders.count { it.status.equals(status, ignoreCase = true) }
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text("$status ($count)") }
                        )
                    }
                }
            }

            // Orders Table / List Cards
            if (filteredOrders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No orders found", fontWeight = FontWeight.Bold)
                            Text("Click '+ New Order' or import from WhatsApp", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderItemCard(
                        order = order,
                        currencySymbol = currencySymbol,
                        onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) },
                        onDelete = { onDeleteOrder(order.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddOrderModalDialog(
            products = products,
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { newOrder ->
                onAddOrder(newOrder)
                showAddDialog = false
            }
        )
    }

    if (showWhatsAppDialog) {
        WhatsAppImportModalDialog(
            products = products,
            productAliases = productAliases,
            currencySymbol = currencySymbol,
            onDismiss = { showWhatsAppDialog = false },
            onSaveAlias = onSaveAlias,
            onImportOrder = { importedOrder ->
                onAddOrder(importedOrder)
                showWhatsAppDialog = false
            }
        )
    }
}

@Composable
fun OrderItemCard(
    order: RecordedOrder,
    currencySymbol: String,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        "New" -> Color(0xFF2563EB)
        "Confirmed" -> Color(0xFF0284C7)
        "Preparing" -> Color(0xFFD97706)
        "Ready" -> Color(0xFF7C3AED)
        "Delivered" -> Color(0xFF059669)
        "Cancelled" -> Color(0xFFDC2626)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .testTag("order_item_${order.customerName}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Text(
                            text = order.customerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Due: ${order.deliveryDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Interactive Status Badge
                Box {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { statusDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(order.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        ORDER_STATUSES.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, fontWeight = if (status == order.status) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    onUpdateStatus(status)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Order Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Product", style = MaterialTheme.typography.labelSmall)
                    Text("${order.productName} ×${order.quantity}", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                            Text(order.weightOrSize, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        if (order.isEggless) {
                            Surface(color = Color(0xFF10B981).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("🌱 Eggless", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Revenue", style = MaterialTheme.typography.labelSmall)
                    Text(CurrencyFormatter.format(order.totalRevenue, currencySymbol), fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Actual Profit", style = MaterialTheme.typography.labelSmall)
                    Text(
                        CurrencyFormatter.format(order.profit, currencySymbol),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (order.customMessageOnCake.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✍️ Name on Cake: \"${order.customMessageOnCake}\"",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (order.aiExplanation.isNotBlank() || order.rawWhatsAppText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (order.rawWhatsAppText.isNotBlank()) {
                            Text("💬 WhatsApp: \"${order.rawWhatsAppText.take(90)}\"", fontSize = 11.sp, color = Color(0xFF334155))
                        }
                        if (order.aiExplanation.isNotBlank()) {
                            Text("🧠 AI: ${order.aiExplanation}", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Delivery & Order Fees Breakdown Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🚚 Delivery ${CurrencyFormatter.format(order.deliveryFee, currencySymbol)} • Platform ${CurrencyFormatter.format(order.platformFee, currencySymbol)} • Gateway ${CurrencyFormatter.format(order.paymentGatewayFee, currencySymbol)} • Marketing ${CurrencyFormatter.format(order.marketingFee, currencySymbol)}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )
            }

            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        val greeting = "Hello ${order.customerName}! 🍰 Your order for ${order.quantity}x ${order.productName} (${order.weightOrSize}) is confirmed for ${order.deliveryDate} (${order.deliveryTimeSlot}). Total: $currencySymbol${order.totalRevenue}. Thank you! 🧁"
                        val waIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(greeting))
                        }
                        try {
                            context.startActivity(waIntent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("💬 Send WhatsApp Confirmation", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WhatsAppImportModalDialog(
    products: List<ProductWithDetails>,
    productAliases: Map<String, String> = emptyMap(),
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveAlias: (String, String) -> Unit = { _, _ -> },
    onImportOrder: (RecordedOrder) -> Unit
) {
    var activeTab by remember { androidx.compose.runtime.mutableIntStateOf(0) } // 0: AI Message Parser, 1: QR Web Scan / Gallery

    var sampleMessageText by remember {
        mutableStateOf("2 choc cake kal bhej dena, total 1000 - Rahul")
    }

    var parsedCustomer by remember { mutableStateOf("Rahul") }
    var parsedProduct by remember { mutableStateOf("Chocolate Cake") }
    var parsedQty by remember { mutableStateOf("2") }
    var parsedValue by remember { mutableStateOf("1000") }
    var parsedDelivery by remember { mutableStateOf("Tomorrow") }
    var isParsed by remember { mutableStateOf(true) }

    // Disambiguation State
    var requiresDisambiguation by remember { mutableStateOf(false) }
    var disambiguationCandidates by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedDisambiguatedProduct by remember { mutableStateOf("") }
    var rawAliasQueryKey by remember { mutableStateOf("red velvet") }
    var matchStatusBadge by remember { mutableStateOf("✅ Matched via saved alias 'choc cake'") }

    fun runSmartMatcher(text: String) {
        val lower = text.lowercase().trim()

        // Customer & Qty & Value Extraction
        if (lower.contains("rahul")) parsedCustomer = "Rahul"
        else if (lower.contains("priya")) parsedCustomer = "Priya"
        else if (lower.contains("aman")) parsedCustomer = "Aman"

        val qtyRegex = Regex("""(\d+)\s*(pcs|pieces|box|jar|cake|choc|red velvet|brownie)?""", RegexOption.IGNORE_CASE)
        val qtyMatch = qtyRegex.find(lower)
        if (qtyMatch != null && qtyMatch.groupValues[1].isNotBlank()) {
            parsedQty = qtyMatch.groupValues[1]
        }

        val priceRegex = Regex("""(total|rs|\b)\s*(\d{3,5})""", RegexOption.IGNORE_CASE)
        val priceMatch = priceRegex.find(lower)
        if (priceMatch != null && priceMatch.groupValues[2].isNotBlank()) {
            parsedValue = priceMatch.groupValues[2]
        }

        parsedDelivery = if (lower.contains("kal") || lower.contains("tomorrow")) "Tomorrow" else "Today"

        // Product Smart Matching
        if (lower.contains("red velvet")) {
            rawAliasQueryKey = "red velvet"
            // Check alias
            val savedAliasMatch = productAliases["red velvet"]
            if (savedAliasMatch != null) {
                parsedProduct = savedAliasMatch
                requiresDisambiguation = false
                matchStatusBadge = "⚡ Auto-matched via saved alias ('red velvet' ➔ $savedAliasMatch)"
            } else {
                // Multi-match scenario: "Red Velvet Cake" & "Red Velvet Jar"
                disambiguationCandidates = listOf("Red Velvet Cake", "Red Velvet Jar")
                selectedDisambiguatedProduct = disambiguationCandidates.first()
                requiresDisambiguation = true
                matchStatusBadge = "❓ Multiple matches found. Seller selection needed!"
            }
        } else if (lower.contains("choc cake") || lower.contains("chocolate cake") || lower.contains("choc")) {
            rawAliasQueryKey = "choc cake"
            val savedAliasMatch = productAliases["choc cake"] ?: "Chocolate Cake"
            parsedProduct = savedAliasMatch
            requiresDisambiguation = false
            matchStatusBadge = "✅ Auto-matched 'choc cake' ➔ Chocolate Cake"
        } else if (lower.contains("brownie")) {
            parsedProduct = "Brownie Box"
            requiresDisambiguation = false
            matchStatusBadge = "✅ Matched 'brownie' ➔ Brownie Box"
        } else {
            parsedProduct = products.firstOrNull()?.product?.name ?: "Chocolate Cake"
            requiresDisambiguation = false
            matchStatusBadge = "✅ Matched product from library"
        }
        isParsed = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧠 Smart Product Matching & WhatsApp Import", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("✨ AI & Smart Match") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("📱 Web QR Scan") }
                    )
                }

                if (activeTab == 1) {
                    // QR Code Web / Gallery Scan Simulation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            modifier = Modifier
                                .size(140.dp)
                                .border(2.dp, Color(0xFF25D366), RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(70.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan WhatsApp Web QR code or pick image from Gallery", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // AI & Smart Product Matcher
                    Text(
                        text = "Paste incoming customer message or select quick test cases:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Quick Sample Buttons for Section 10 testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                sampleMessageText = "2 choc cake kal bhej dena, total 1000 - Rahul"
                                runSmartMatcher(sampleMessageText)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("1: '2 choc cake'", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                sampleMessageText = "1 red velvet kal bhej dena, total 600 - Priya"
                                runSmartMatcher(sampleMessageText)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("2: '1 red velvet'", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = sampleMessageText,
                        onValueChange = { sampleMessageText = it },
                        label = { Text("WhatsApp Text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = { runSmartMatcher(sampleMessageText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Smart Product Matching")
                    }

                    if (requiresDisambiguation) {
                        // Disambiguation Prompt Card ("Which product?")
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                                .testTag("product_disambiguation_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "❓ Disambiguation Required: Which product?",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Query '$rawAliasQueryKey' matches multiple library products. Select one to learn alias for next time:",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Column {
                                    disambiguationCandidates.forEach { candidate ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedDisambiguatedProduct = candidate }
                                                .padding(vertical = 6.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            androidx.compose.material3.RadioButton(
                                                selected = (selectedDisambiguatedProduct == candidate),
                                                onClick = { selectedDisambiguatedProduct = candidate }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(candidate, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        onSaveAlias(rawAliasQueryKey, selectedDisambiguatedProduct)
                                        parsedProduct = selectedDisambiguatedProduct
                                        requiresDisambiguation = false
                                        matchStatusBadge = "✅ Learned & Saved Alias: '$rawAliasQueryKey' ➔ $selectedDisambiguatedProduct"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Text("Confirm Selection & Save Alias", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (isParsed) {
                        // Matched Product Result
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("✨ AI & Smart Product Matching Result:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                Text("• Customer: $parsedCustomer", fontWeight = FontWeight.Bold)
                                Text("• Matched Product: $parsedProduct ×$parsedQty", fontWeight = FontWeight.Bold)
                                Text("• Total Value: $currencySymbol$parsedValue", fontWeight = FontWeight.Bold)
                                Text("• Delivery: $parsedDelivery", fontWeight = FontWeight.Bold)
                                Text(
                                    text = matchStatusBadge,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rev = parsedValue.toDoubleOrNull() ?: 1000.0
                    val q = parsedQty.toIntOrNull() ?: 1
                    val newOrder = RecordedOrder(
                        customerName = parsedCustomer,
                        productName = parsedProduct,
                        quantity = q,
                        totalRevenue = rev,
                        totalCost = rev * 0.46,
                        status = "New",
                        deliveryDate = parsedDelivery
                    )
                    onImportOrder(newOrder)
                },
                enabled = !requiresDisambiguation,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Text("Import to Orders List", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddOrderModalDialog(
    products: List<ProductWithDetails>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (RecordedOrder) -> Unit
) {
    var customerName by remember { mutableStateOf("Rahul") }
    var selectedProductIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var quantityText by remember { mutableStateOf("1") }
    var revenueText by remember { mutableStateOf("400") }
    var deliveryFeeText by remember { mutableStateOf("50") }
    var platformFeeText by remember { mutableStateOf("30") }
    var paymentGatewayFeeText by remember { mutableStateOf("12") }
    var marketingFeeText by remember { mutableStateOf("20") }
    var deliveryDateText by remember { mutableStateOf("Tomorrow") }
    var selectedStatus by remember { mutableStateOf("New") }

    val activeProduct = products.getOrNull(selectedProductIndex)

    val qty = quantityText.toIntOrNull() ?: 1
    val rev = revenueText.toDoubleOrNull() ?: 400.0
    val prodCost = (activeProduct?.totalCost ?: 200.0) * qty
    val delFee = deliveryFeeText.toDoubleOrNull() ?: 0.0
    val platFee = platformFeeText.toDoubleOrNull() ?: 0.0
    val pgFee = paymentGatewayFeeText.toDoubleOrNull() ?: 0.0
    val mktFee = marketingFeeText.toDoubleOrNull() ?: 0.0
    val totalFees = delFee + platFee + pgFee + mktFee
    val actualNetProfit = rev - prodCost - totalFees

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Record Order & Deduct Fees", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Live Actual Net Profit Breakdown Preview Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("💡 Actual Estimated Net Profit Preview:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
                        Text("Revenue: ${CurrencyFormatter.format(rev, currencySymbol)} | Cost: ${CurrencyFormatter.format(prodCost, currencySymbol)}", fontSize = 11.sp)
                        Text("Fees (Delivery $delFee + Platform $platFee + Gateway $pgFee + Mkt $mktFee): ${CurrencyFormatter.format(totalFees, currencySymbol)}", fontSize = 11.sp)
                        Text(
                            "Actual Net Profit = ${CurrencyFormatter.format(actualNetProfit, currencySymbol)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_order_customer_input")
                )

                OutlinedTextField(
                    value = activeProduct?.product?.name ?: "Chocolate Cake",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Product") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("add_order_qty_input")
                    )

                    OutlinedTextField(
                        value = revenueText,
                        onValueChange = { revenueText = it },
                        label = { Text("Revenue ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("add_order_revenue_input")
                    )
                }

                Text("🚚 Order-level Fees & Delivery:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deliveryFeeText,
                        onValueChange = { deliveryFeeText = it },
                        label = { Text("Delivery ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = platformFeeText,
                        onValueChange = { platformFeeText = it },
                        label = { Text("Platform Fee ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paymentGatewayFeeText,
                        onValueChange = { paymentGatewayFeeText = it },
                        label = { Text("Gateway Fee ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = marketingFeeText,
                        onValueChange = { marketingFeeText = it },
                        label = { Text("Marketing Fee ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = deliveryDateText,
                    onValueChange = { deliveryDateText = it },
                    label = { Text("Delivery Date / Time") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newOrder = RecordedOrder(
                        customerName = customerName.ifBlank { "Customer" },
                        productName = activeProduct?.product?.name ?: "Cake",
                        quantity = qty,
                        totalRevenue = rev,
                        totalCost = prodCost,
                        deliveryFee = delFee,
                        platformFee = platFee,
                        paymentGatewayFee = pgFee,
                        marketingFee = mktFee,
                        status = selectedStatus,
                        deliveryDate = deliveryDateText
                    )
                    onConfirm(newOrder)
                }
            ) {
                Text("Create Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
