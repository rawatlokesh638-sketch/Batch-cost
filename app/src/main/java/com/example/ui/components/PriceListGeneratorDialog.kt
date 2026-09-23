package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.ProductWithDetails
import com.example.util.CurrencyFormatter

@Composable
fun PriceListGeneratorDialog(
    products: List<ProductWithDetails>,
    profile: BusinessProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currencySym = profile.currencySymbol

    // Selected product names for price list
    val selectedProducts = remember {
        mutableStateListOf<String>().apply {
            addAll(products.map { it.product.name }.ifEmpty { listOf("Chocolate Cake", "Brownie Box", "Butter Cookies", "Red Velvet Jar") })
        }
    }

    val sampleProducts = if (products.isEmpty()) {
        listOf(
            Pair("Chocolate Cake", 500.0),
            Pair("Brownie Box", 300.0),
            Pair("Butter Cookies", 250.0),
            Pair("Red Velvet Jar", 250.0)
        )
    } else {
        products.map { Pair(it.product.name, it.product.sellingPrice) }
    }

    fun buildPriceListText(): String {
        val sb = StringBuilder()
        sb.append("📋 *${profile.businessName.ifBlank { "Our Bakery" }} - Business Price List* 📋\n")
        sb.append("📞 Contact: ${profile.phone.ifBlank { "+91 9876543210" }}\n\n")
        sb.append("Freshly baked with premium ingredients & love! Order now:\n\n")

        sampleProducts.forEach { (name, price) ->
            if (selectedProducts.contains(name)) {
                sb.append("• *${name}* ➔ ${CurrencyFormatter.format(price, currencySym)}\n")
            }
        }
        sb.append("\n✨ *Bulk orders & custom cakes available on request!*")
        return sb.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📋 Business Price List Generator", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("price_list_generator_dialog")
            ) {
                Text("Select products to include in your shareable WhatsApp price list:", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(sampleProducts) { (name, price) ->
                        val isSelected = selectedProducts.contains(name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .clickable {
                                    if (isSelected) selectedProducts.remove(name) else selectedProducts.add(name)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedProducts.add(name) else selectedProducts.remove(name)
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(name, fontWeight = FontWeight.Bold)
                            }
                            Text(CurrencyFormatter.format(price, currencySym), fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        }
                    }
                }

                // WhatsApp Preview Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬 WhatsApp Message Preview:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                        Text(
                            text = buildPriceListText(),
                            fontSize = 11.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = buildPriceListText()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        setPackage("com.whatsapp")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(fallbackIntent, "Share Price List"))
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("share_whatsapp_price_list_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Price List to WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
