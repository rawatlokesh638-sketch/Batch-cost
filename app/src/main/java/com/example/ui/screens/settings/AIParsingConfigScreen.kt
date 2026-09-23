package com.example.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AIParsingConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIParsingConfigScreen(
    currentConfig: AIParsingConfig,
    onBack: () -> Unit,
    onSave: (AIParsingConfig) -> Unit
) {
    var productNameRule by remember { mutableStateOf(currentConfig.productNameRule) }
    var quantityRule by remember { mutableStateOf(currentConfig.quantityRule) }
    var priceRule by remember { mutableStateOf(currentConfig.priceRule) }
    var customContext by remember { mutableStateOf(currentConfig.customContext) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Parsing Rules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSave(
                                AIParsingConfig(
                                    productNameRule = productNameRule,
                                    quantityRule = quantityRule,
                                    priceRule = priceRule,
                                    customContext = customContext
                                )
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Customize how the AI extracts details from WhatsApp messages. Be specific for better results.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = productNameRule,
                onValueChange = { productNameRule = it },
                label = { Text("Product Name Rule") },
                placeholder = { Text("e.g. Look for the cake or bakery item name mentioned.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = quantityRule,
                onValueChange = { quantityRule = it },
                label = { Text("Quantity Rule") },
                placeholder = { Text("e.g. Extract the number of items or weight (kg/pcs).") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = priceRule,
                onValueChange = { priceRule = it },
                label = { Text("Price/Revenue Rule") },
                placeholder = { Text("e.g. Identify the total amount or price per unit if mentioned.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = customContext,
                onValueChange = { customContext = it },
                label = { Text("Business Context") },
                placeholder = { Text("e.g. This is a home-based bakery named 'Sweet Treats'.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Pro Tip 💡",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "If you often use codes or abbreviations (e.g. '1/2kg BC' for '1/2kg Black Forest Cake'), mention it in the Business Context so the AI learns your shorthand.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
