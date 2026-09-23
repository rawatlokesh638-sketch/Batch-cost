package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProductWithDetails
import com.example.util.CurrencyFormatter

@Composable
fun RecordOrderDialog(
    products: List<ProductWithDetails>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmOrder: (customerName: String, productName: String, qty: Int, revenue: Double, cost: Double) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var selectedProdIndex by remember { mutableIntStateOf(0) }
    var qtyText by remember { mutableStateOf("1") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val activeProd = products.getOrNull(selectedProdIndex)
    val qty = qtyText.toIntOrNull() ?: 1
    val unitPrice = activeProd?.product?.sellingPrice ?: 0.0
    val unitCost = activeProd?.totalCost ?: 0.0
    val totalRevenue = unitPrice * qty
    val totalCost = unitCost * qty
    val profit = totalRevenue - totalCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Customer Order 🛍️", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name / Phone") },
                    placeholder = { Text("e.g. Priya Sharma") },
                    modifier = Modifier.fillMaxWidth().testTag("order_customer_name"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Box {
                    OutlinedTextField(
                        value = activeProd?.product?.name ?: "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Item") },
                        modifier = Modifier.fillMaxWidth().testTag("order_product_select"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.matchParentSize(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    ) {}

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        products.forEachIndexed { idx, p ->
                            DropdownMenuItem(
                                text = { Text("${p.product.name} (${CurrencyFormatter.format(p.product.sellingPrice, currencySymbol)})") },
                                onClick = {
                                    selectedProdIndex = idx
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("order_quantity_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Order Financial Breakdown:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Total Revenue: ${CurrencyFormatter.format(totalRevenue, currencySymbol)}")
                        Text("Production Cost: ${CurrencyFormatter.format(totalCost, currencySymbol)}")
                        Text(
                            "Net Profit: ${CurrencyFormatter.format(profit, currencySymbol)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank() && activeProd != null) {
                        onConfirmOrder(customerName, activeProd.product.name, qty, totalRevenue, totalCost)
                    }
                },
                enabled = customerName.isNotBlank() && activeProd != null,
                modifier = Modifier.testTag("save_recorded_order_btn")
            ) {
                Text("Record Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateLabelModal(
    products: List<ProductWithDetails>,
    currencySymbol: String,
    businessName: String,
    onDismiss: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var batchNo by remember { mutableStateOf("BATCH-#1042") }
    var netWeight by remember { mutableStateOf("500 g") }
    var customTagline by remember { mutableStateOf("Handcrafted with love ❤️") }
    var copiedNotice by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val activeProd = products.getOrNull(selectedIndex)

    val labelText = """
        =================================
        🏷️ ${businessName.uppercase()}
        =================================
        Product: ${activeProd?.product?.name ?: "Product"}
        M.R.P: ${CurrencyFormatter.format(activeProd?.product?.sellingPrice ?: 0.0, currencySymbol)} (Incl. of all taxes)
        Net Qty: ${netWeight.ifBlank { activeProd?.product?.weight ?: "500 g" }}
        Batch No: $batchNo
        Category: ${activeProd?.product?.category ?: "Fresh Homemade"}
        
        Ingredients: ${activeProd?.recipeIngredients?.joinToString(", ") { it.ingredientName } ?: "Quality Ingredients"}
        
        $customTagline
        =================================
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏷️ Create Product Label Sticker", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Product for Label", style = MaterialTheme.typography.labelSmall)
                DropdownProductSelector(
                    products = products,
                    selectedIndex = selectedIndex,
                    currencySymbol = currencySymbol,
                    onSelect = { selectedIndex = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNo,
                        onValueChange = { batchNo = it },
                        label = { Text("Batch No") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = netWeight,
                        onValueChange = { netWeight = it },
                        label = { Text("Weight / Size") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = customTagline,
                    onValueChange = { customTagline = it },
                    label = { Text("Packaging Tagline") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Label Sticker Preview
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("PREVIEW PACKAGING STICKER:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            lineHeight = 16.sp
                        )
                    }
                }

                if (copiedNotice) {
                    Text("Label copied to clipboard!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(labelText))
                    copiedNotice = true
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Label Text")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun WhatsAppImportModal(
    products: List<ProductWithDetails>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onImportSuccess: (customerName: String, productName: String, qty: Int, totalRevenue: Double, totalCost: Double) -> Unit
) {
    var rawText by remember { mutableStateOf("Hi! Please confirm order for 2 Chocolate Cakes for tomorrow 5pm. Customer Name: Sneha") }
    var parsedMessage by remember { mutableStateOf<String?>(null) }
    var detectedProduct by remember { mutableStateOf<ProductWithDetails?>(null) }
    var detectedQty by remember { mutableIntStateOf(2) }
    var detectedCustomer by remember { mutableStateOf("Sneha") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📷 Import WhatsApp Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Paste WhatsApp customer message or order chat:", style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("whatsapp_input_text"),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        // Smart auto-detection algorithm
                        val foundProd = products.find { p ->
                            rawText.contains(p.product.name, ignoreCase = true) ||
                            p.product.name.split(" ").any { rawText.contains(it, ignoreCase = true) }
                        } ?: products.firstOrNull()

                        val digits = Regex("""\b(\d+)\b""").findAll(rawText).mapNotNull { it.value.toIntOrNull() }.filter { it < 100 }.firstOrNull() ?: 1
                        val nameMatch = Regex("""Name:\s*([A-Za-z]+)""", RegexOption.IGNORE_CASE).find(rawText)?.groupValues?.get(1) ?: "WhatsApp Customer"

                        detectedProduct = foundProd
                        detectedQty = digits
                        detectedCustomer = nameMatch
                        parsedMessage = "Detected Item: ${foundProd?.product?.name ?: "Unknown"} | Qty: $digits | Customer: $nameMatch"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ai_extract_whatsapp_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Extract Order Details")
                }

                detectedProduct?.let { prod ->
                    val totalRev = prod.product.sellingPrice * detectedQty
                    val totalCost = prod.totalCost * detectedQty

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Extracted Order Summary:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("Customer: $detectedCustomer")
                            Text("Product: ${prod.product.name} x $detectedQty")
                            Text("Total Revenue: ${CurrencyFormatter.format(totalRev, currencySymbol)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    detectedProduct?.let { prod ->
                        val rev = prod.product.sellingPrice * detectedQty
                        val cost = prod.totalCost * detectedQty
                        onImportSuccess(detectedCustomer, prod.product.name, detectedQty, rev, cost)
                    }
                },
                enabled = detectedProduct != null,
                modifier = Modifier.testTag("confirm_whatsapp_import_btn")
            ) {
                Text("Confirm & Record Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DropdownProductSelector(
    products: List<ProductWithDetails>,
    selectedIndex: Int,
    currencySymbol: String,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = products.getOrNull(selectedIndex)

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = current?.product?.name ?: "Select Product",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Button(
            onClick = { expanded = true },
            modifier = Modifier.matchParentSize(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        ) {}

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.forEachIndexed { idx, p ->
                DropdownMenuItem(
                    text = { Text("${p.product.name} (${CurrencyFormatter.format(p.product.sellingPrice, currencySymbol)})") },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}
