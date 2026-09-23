package com.example.ui.screens.calculator

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.ui.ProductWithDetails
import com.example.util.CurrencyFormatter

@Composable
fun SellingPriceCalculatorScreen(
    products: List<ProductWithDetails> = emptyList(),
    currencySymbol: String = "₹"
) {
    var costInputText by remember { mutableStateOf("200") }
    var calcMode by remember { mutableIntStateOf(0) } // 0: Target Margin %, 1: Fixed Desired Profit (₹), 2: Target Markup %
    
    // Target parameters
    var targetMarginPercentText by remember { mutableStateOf("30") }
    var targetProfitAmountText by remember { mutableStateOf("100") }
    var targetMarkupPercentText by remember { mutableStateOf("30") }

    var productDropdownExpanded by remember { mutableStateOf(false) }
    var showMarginVsMarkupExplanation by remember { mutableStateOf(true) }

    val costVal = costInputText.toDoubleOrNull() ?: 0.0

    // Calculations based on modes
    val targetMarginPct = targetMarginPercentText.toDoubleOrNull() ?: 0.0
    val targetProfitAmt = targetProfitAmountText.toDoubleOrNull() ?: 0.0
    val targetMarkupPct = targetMarkupPercentText.toDoubleOrNull() ?: 0.0

    // Selling Price = Cost / (1 - Margin)
    val marginSellingPrice = if (targetMarginPct < 100.0) costVal / (1.0 - (targetMarginPct / 100.0)) else 0.0
    val marginProfit = marginSellingPrice - costVal

    // Fixed Profit Price
    val fixedProfitSellingPrice = costVal + targetProfitAmt
    val fixedProfitMarginPct = if (fixedProfitSellingPrice > 0) (targetProfitAmt / fixedProfitSellingPrice) * 100.0 else 0.0

    // Selling Price = Cost * (1 + Markup)
    val markupSellingPrice = costVal * (1.0 + (targetMarkupPct / 100.0))
    val markupProfit = markupSellingPrice - costVal
    val markupMarginPct = if (markupSellingPrice > 0) (markupProfit / markupSellingPrice) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💰 Selling Price & Margin Calculator",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Cost Source Input Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Enter Unit Production Cost", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = costInputText,
                            onValueChange = { costInputText = it },
                            label = { Text("Unit Cost ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("selling_calc_cost_input")
                        )

                        if (products.isNotEmpty()) {
                            Box {
                                Button(
                                    onClick = { productDropdownExpanded = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pick Saved Product")
                                }

                                DropdownMenu(
                                    expanded = productDropdownExpanded,
                                    onDismissRequest = { productDropdownExpanded = false }
                                ) {
                                    products.forEach { prodDetails ->
                                        DropdownMenuItem(
                                            text = {
                                                Text("${prodDetails.product.name} (${CurrencyFormatter.format(prodDetails.totalCost, currencySymbol)})")
                                            },
                                            onClick = {
                                                costInputText = CurrencyFormatter.formatDoubleRaw(prodDetails.totalCost)
                                                productDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mode Selector Tabs
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("2. Pricing Objective Target", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    TabRow(
                        selectedTabIndex = calcMode,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = calcMode == 0,
                            onClick = { calcMode = 0 },
                            text = { Text("Target Margin %", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("mode_target_margin")
                        )
                        Tab(
                            selected = calcMode == 1,
                            onClick = { calcMode = 1 },
                            text = { Text("Fixed Profit ($currencySymbol)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("mode_fixed_profit")
                        )
                        Tab(
                            selected = calcMode == 2,
                            onClick = { calcMode = 2 },
                            text = { Text("Markup %", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("mode_markup")
                        )
                    }

                    when (calcMode) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Target Profit Margin % (Profit ÷ Selling Price)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Preset Margin Quick Chips (20%, 30%, 40%, 50%)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("20", "30", "40", "50").forEach { pct ->
                                        FilterChip(
                                            selected = targetMarginPercentText == pct,
                                            onClick = { targetMarginPercentText = pct },
                                            label = { Text("$pct% Margin", fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("chip_margin_$pct")
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = targetMarginPercentText,
                                    onValueChange = { targetMarginPercentText = it },
                                    label = { Text("Custom Target Margin %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Fixed Desired Profit Amount (e.g. 'I want $currencySymbol 100 profit')",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = targetProfitAmountText,
                                    onValueChange = { targetProfitAmountText = it },
                                    label = { Text("Desired Net Profit per Unit ($currencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Target Markup % (Profit ÷ Unit Cost)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = targetMarkupPercentText,
                                    onValueChange = { targetMarkupPercentText = it },
                                    label = { Text("Target Markup %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // RECOMMENDED SELLING PRICE RESULT BANNER
        item {
            val (recPrice, recProfit, recMarginPct, formulaText) = when (calcMode) {
                0 -> Quadruple(
                    marginSellingPrice,
                    marginProfit,
                    targetMarginPct,
                    "Formula: $currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)} ÷ (1 - ${targetMarginPct / 100.0}) = $currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)} ÷ ${1.0 - (targetMarginPct / 100.0)} = ${CurrencyFormatter.format(marginSellingPrice, currencySymbol)}"
                )
                1 -> Quadruple(
                    fixedProfitSellingPrice,
                    targetProfitAmt,
                    fixedProfitMarginPct,
                    "Formula: Unit Cost ($currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)}) + Desired Profit ($currencySymbol${CurrencyFormatter.formatDoubleRaw(targetProfitAmt)}) = ${CurrencyFormatter.format(fixedProfitSellingPrice, currencySymbol)}"
                )
                else -> Quadruple(
                    markupSellingPrice,
                    markupProfit,
                    markupMarginPct,
                    "Formula: Unit Cost ($currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)}) × (1 + ${targetMarkupPct / 100.0}) = ${CurrencyFormatter.format(markupSellingPrice, currencySymbol)}"
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recommended_price_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💡 RECOMMENDED SELLING PRICE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = CurrencyFormatter.format(recPrice, currencySymbol),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Unit Cost: ${CurrencyFormatter.format(costVal, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Net Profit", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = CurrencyFormatter.format(recProfit, currencySymbol),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Margin: ${CurrencyFormatter.formatPercent(recMarginPct)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Text(
                        text = formulaText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // PRESET MARGIN COMPARISON TABLE CARD
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📊 Quick Target Margin Matrix (for Cost = ${CurrencyFormatter.format(costVal, currencySymbol)})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    listOf(20.0, 30.0, 40.0, 50.0).forEach { marginPct ->
                        val price = if (marginPct < 100.0) costVal / (1.0 - (marginPct / 100.0)) else 0.0
                        val profit = price - costVal

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (targetMarginPercentText == marginPct.toInt().toString()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${marginPct.toInt()}% Margin", fontWeight = FontWeight.Bold)
                                Text("Selling Price: ${CurrencyFormatter.format(price, currencySymbol)}", style = MaterialTheme.typography.bodyMedium)
                            }

                            Text(
                                text = "Profit: ${CurrencyFormatter.format(profit, currencySymbol)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // EDUCATIONAL CARD: MARGIN VS MARKUP EXPLAINED
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
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
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💡 Margin vs. Markup: Not the same!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = if (showMarginVsMarkupExplanation) "Hide" else "Show",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showMarginVsMarkupExplanation = !showMarginVsMarkupExplanation }
                        )
                    }

                    AnimatedVisibility(visible = showMarginVsMarkupExplanation) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "• Margin is profit relative to SELLING PRICE: (Profit ÷ Selling Price).\n" +
                                        "• Markup is profit relative to COST: (Profit ÷ Cost).\n\n" +
                                        "Example for Cost = $currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)}:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            val ex30MarginPrice = costVal / 0.70
                            val ex30MarkupPrice = costVal * 1.30

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🎯 30% Margin Target:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Selling Price = $currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)} ÷ 0.70 = ${CurrencyFormatter.format(ex30MarginPrice, currencySymbol)}")
                                    Text("Profit = ${CurrencyFormatter.format(ex30MarginPrice - costVal, currencySymbol)} (30% of selling price)")

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text("📈 30% Markup Target:", fontWeight = FontWeight.Bold)
                                    Text("Selling Price = $currencySymbol${CurrencyFormatter.formatDoubleRaw(costVal)} × 1.30 = ${CurrencyFormatter.format(ex30MarkupPrice, currencySymbol)}")
                                    Text("Profit = ${CurrencyFormatter.format(ex30MarkupPrice - costVal, currencySymbol)} (30% of cost)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
