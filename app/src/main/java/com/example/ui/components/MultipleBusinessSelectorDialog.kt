package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BusinessAccount(
    val id: String,
    val name: String,
    val category: String,
    val currencySymbol: String,
    val activeProductsCount: Int
)

@Composable
fun MultipleBusinessSelectorDialog(
    currentBusinessName: String,
    onDismiss: () -> Unit,
    onSelectBusiness: (BusinessAccount) -> Unit
) {
    val businesses = remember {
        mutableListOf(
            BusinessAccount("b1", "Sweet Crumb Bakery", "Bakery & Confectionery", "₹", 4),
            BusinessAccount("b2", "Aura Soy Candles", "Handmade Candles & Decor", "₹", 6),
            BusinessAccount("b3", "Organic Soap Co.", "Bath & Body Essentials", "₹", 5)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newBusName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🏪 Switch / Manage Businesses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("multiple_business_dialog")
            ) {
                Text("Manage multiple independent stores under one account with separate products, orders, and reports:", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(businesses) { bus ->
                        val isCurrent = bus.name.equals(currentBusinessName, ignoreCase = true)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isCurrent) Color(0xFF3B82F6) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectBusiness(bus)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(32.dp).background(if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(bus.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${bus.category} • ${bus.activeProductsCount} products", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier.background(Color(0xFFDBEAFE), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Active", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddDialog) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(8.dp)
                    ) {
                        Text("Add New Business", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = newBusName,
                            onValueChange = { newBusName = it },
                            label = { Text("Business Name") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (newBusName.isNotBlank()) {
                                    businesses.add(BusinessAccount("b${businesses.size + 1}", newBusName, "Retail", "₹", 0))
                                    newBusName = ""
                                    showAddDialog = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Business")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Another Business")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Close")
            }
        }
    )
}
