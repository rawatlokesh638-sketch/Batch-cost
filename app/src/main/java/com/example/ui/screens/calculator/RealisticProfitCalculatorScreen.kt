package com.example.ui.screens.calculator

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.ui.ProductWithDetails
import com.example.util.CurrencyFormatter

@Composable
fun RealisticProfitCalculatorScreen(
    products: List<ProductWithDetails> = emptyList(),
    currencySymbol: String = "₹"
) {
    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val activeProduct = products.getOrNull(selectedProductIndex)

    // Base inputs
    var quantityText by remember { mutableStateOf("20") }
    var sellingPriceText by remember { mutableStateOf(activeProduct?.product?.sellingPrice?.toString() ?: "500") }
    
    // Direct Costs per unit
    var ingredientCostText by remember { mutableStateOf(activeProduct?.rawIngredientsCost?.toString() ?: "180") }
    var packagingCostText by remember { mutableStateOf(activeProduct?.product?.packagingCost?.toString() ?: "17") }
    var labourCostText by remember { mutableStateOf(activeProduct?.product?.labourCost?.toString() ?: "20") }
    var overheadCostText by remember { mutableStateOf(activeProduct?.product?.electricityGasCost?.toString() ?: "10") }
    var wastagePercentText by remember { mutableStateOf(activeProduct?.product?.wastagePercent?.toString() ?: "3") }

    // Channel & Transaction Costs
    var deliveryCostText by remember { mutableStateOf("25") } // Delivery per unit or flat
    var platformFeePercentText by remember { mutableStateOf("15") } // Swiggy/Zomato/Amazon platform fee %
    var paymentFeePercentText by remember { mutableStateOf("2") } // Razorpay / Payment gateway %
    var marketingCostText by remember { mutableStateOf("15") } // CAC / Ads per unit

    val qty = quantityText.toIntOrNull() ?: 1
    val unitPrice = sellingPriceText.toDoubleOrNull() ?: 0.0

    // Direct per-unit calculations
    val unitIngCost = ingredientCostText.toDoubleOrNull() ?: 0.0
    val unitPkgCost = packagingCostText.toDoubleOrNull() ?: 0.0
    val unitLbrCost = labourCostText.toDoubleOrNull() ?: 0.0
    val unitOvhCost = overheadCostText.toDoubleOrNull() ?: 0.0
    val wastagePct = wastagePercentText.toDoubleOrNull() ?: 0.0
    val unitWstCost = unitIngCost * (wastagePct / 100.0)

    val unitDirectCost = unitIngCost + unitPkgCost + unitLbrCost + unitOvhCost + unitWstCost

    // Channel costs
    val unitDeliveryCost = deliveryCostText.toDoubleOrNull() ?: 0.0
    val platformFeePct = platformFeePercentText.toDoubleOrNull() ?: 0.0
    val unitPlatformFee = unitPrice * (platformFeePct / 100.0)
    val paymentFeePct = paymentFeePercentText.toDoubleOrNull() ?: 0.0
    val unitPaymentFee = unitPrice * (paymentFeePct / 100.0)
    val unitMarketingCost = marketingCostText.toDoubleOrNull() ?: 0.0

    val unitTotalDeductions = unitDirectCost + unitDeliveryCost + unitPlatformFee + unitPaymentFee + unitMarketingCost
    val unitRealisticProfit = unitPrice - unitTotalDeductions

    // Totals for Batch
    val totalRevenue = unitPrice * qty
    val totalDirectCost = unitDirectCost * qty
    val totalDeliveryCost = unitDeliveryCost * qty
    val totalPlatformFee = unitPlatformFee * qty
    val totalPaymentFee = unitPaymentFee * qty
    val totalMarketingCost = unitMarketingCost * qty

    val totalDeductions = unitTotalDeductions * qty
    val totalRealisticProfit = unitRealisticProfit * qty
    val netMarginPercent = if (totalRevenue > 0) (totalRealisticProfit / totalRevenue) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📊 Realistic Net Profit Calculator",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Product Quick Fill Dropdown
        if (products.isNotEmpty()) {
            item {
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
                        Text("Auto-fill from Saved Product Recipe", style = MaterialTheme.typography.labelMedium)
                        Box {
                            Button(
                                onClick = { productDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(activeProduct?.product?.name ?: "Select Product")
                            }

                            DropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                products.forEachIndexed { idx, prodDetails ->
                                    DropdownMenuItem(
                                        text = { Text(prodDetails.product.name) },
                                        onClick = {
                                            selectedProductIndex = idx
                                            sellingPriceText = CurrencyFormatter.formatDoubleRaw(prodDetails.product.sellingPrice)
                                            ingredientCostText = CurrencyFormatter.formatDoubleRaw(prodDetails.rawIngredientsCost)
                                            packagingCostText = CurrencyFormatter.formatDoubleRaw(prodDetails.product.packagingCost)
                                            labourCostText = CurrencyFormatter.formatDoubleRaw(prodDetails.product.labourCost)
                                            overheadCostText = CurrencyFormatter.formatDoubleRaw(prodDetails.product.electricityGasCost)
                                            wastagePercentText = CurrencyFormatter.formatDoubleRaw(prodDetails.product.wastagePercent)
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

        // Revenue & Quantity Card
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
                    Text("1. Batch Volume & Selling Price", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity (Units)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { sellingPriceText = it },
                            label = { Text("Price/Unit ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Total Gross Revenue: ${CurrencyFormatter.format(totalRevenue, currencySymbol)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Direct Production Costs Card
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
                    Text("2. Direct Production Costs (Per Unit)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ingredientCostText,
                            onValueChange = { ingredientCostText = it },
                            label = { Text("Ingredients ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = packagingCostText,
                            onValueChange = { packagingCostText = it },
                            label = { Text("Packaging ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = labourCostText,
                            onValueChange = { labourCostText = it },
                            label = { Text("Labour ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = overheadCostText,
                            onValueChange = { overheadCostText = it },
                            label = { Text("Gas/Overhead ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = wastagePercentText,
                        onValueChange = { wastagePercentText = it },
                        label = { Text("Wastage % (on ingredients)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Channel & Selling Deductions Card
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
                    Text("3. Delivery, Fees & Marketing Deductions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deliveryCostText,
                            onValueChange = { deliveryCostText = it },
                            label = { Text("Delivery ($currencySymbol/unit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = platformFeePercentText,
                            onValueChange = { platformFeePercentText = it },
                            label = { Text("Platform Fee %") },
                            placeholder = { Text("Swiggy/Amazon %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paymentFeePercentText,
                            onValueChange = { paymentFeePercentText = it },
                            label = { Text("Payment Fee %") },
                            placeholder = { Text("UPI/Card %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = marketingCostText,
                            onValueChange = { marketingCostText = it },
                            label = { Text("Marketing/CAC ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // REALISTIC NET PROFIT WATERFALL RESULT CARD
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (totalRealisticProfit >= 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (totalRealisticProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("realistic_profit_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🏁 REALISTIC NET PROFIT WATERFALL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (totalRealisticProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                        )
                    )

                    ProfitLineRow("Gross Revenue ($qty × $currencySymbol$sellingPriceText)", CurrencyFormatter.format(totalRevenue, currencySymbol), isBold = true)
                    ProfitLineRow("− Ingredient Cost", CurrencyFormatter.format(unitIngCost * qty, currencySymbol))
                    ProfitLineRow("− Packaging Cost", CurrencyFormatter.format(unitPkgCost * qty, currencySymbol))
                    ProfitLineRow("− Labour Cost", CurrencyFormatter.format(unitLbrCost * qty, currencySymbol))
                    ProfitLineRow("− Overhead (Gas/Elec)", CurrencyFormatter.format(unitOvhCost * qty, currencySymbol))
                    ProfitLineRow("− Wastage ($wastagePct%)", CurrencyFormatter.format(unitWstCost * qty, currencySymbol))
                    ProfitLineRow("− Delivery Fee", CurrencyFormatter.format(totalDeliveryCost, currencySymbol))
                    ProfitLineRow("− Platform Fee ($platformFeePct%)", CurrencyFormatter.format(totalPlatformFee, currencySymbol))
                    ProfitLineRow("− Payment Fee ($paymentFeePct%)", CurrencyFormatter.format(totalPaymentFee, currencySymbol))
                    ProfitLineRow("− Marketing & Ads", CurrencyFormatter.format(totalMarketingCost, currencySymbol))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )

                    ProfitLineRow(
                        label = "REALISTIC ESTIMATED PROFIT",
                        value = CurrencyFormatter.format(totalRealisticProfit, currencySymbol),
                        isBold = true,
                        valueColor = if (totalRealisticProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                    )

                    ProfitLineRow(
                        label = "Realistic Net Profit Margin",
                        value = CurrencyFormatter.formatPercent(netMarginPercent),
                        isBold = true,
                        valueColor = if (netMarginPercent >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                    )

                    // Progress Bar fraction
                    val profitRatio = if (totalRevenue > 0) (totalRealisticProfit / totalRevenue).toFloat().coerceIn(0f, 1f) else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Profit vs Revenue Ratio:", style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(
                            progress = { profitRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (totalRealisticProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitLineRow(
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
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}
