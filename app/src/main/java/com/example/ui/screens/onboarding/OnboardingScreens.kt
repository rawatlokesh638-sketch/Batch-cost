package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BatchCostViewModel
import com.example.ui.OnboardingState
import com.example.ui.TempIngredientItem
import com.example.ui.components.SharedIngredientEditCard
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    state: OnboardingState,
    viewModel: BatchCostViewModel
) {
    Scaffold(
        topBar = {
            if (state.step in 2..5) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Step ${state.step - 1} of 4",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { (state.step - 1) / 4f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.updateOnboardingStep(state.step - 1) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.step) {
                1 -> Screen1Welcome(
                    onGetStarted = { viewModel.updateOnboardingStep(2) }
                )
                2 -> Screen2BusinessType(
                    selectedType = state.businessType,
                    customType = state.customBusinessType,
                    onSelectType = { viewModel.updateBusinessType(it) },
                    onCustomTypeChange = { viewModel.updateCustomBusinessType(it) },
                    onNext = { viewModel.updateOnboardingStep(3) }
                )
                3 -> Screen3BusinessDetails(
                    state = state,
                    onUpdate = { name, owner, phone, email, currencySym, currencyCode, logo, addr ->
                        viewModel.updateBusinessDetails(name, owner, phone, email, currencySym, currencyCode, logo, addr)
                    },
                    onNext = { viewModel.updateOnboardingStep(4) }
                )
                4 -> Screen4FirstProduct(
                    state = state,
                    onUpdate = { name, price, unit, sku, cat, desc, weight ->
                        viewModel.updateFirstProduct(name, price, unit, sku, cat, desc, weight)
                    },
                    onNext = { viewModel.updateOnboardingStep(5) }
                )
                5 -> Screen5AddIngredients(
                    state = state,
                    onAddIngredient = { viewModel.addIngredientToOnboarding() },
                    onUpdateIngredient = { index, item -> viewModel.updateIngredientInOnboarding(index, item) },
                    onRemoveIngredient = { index -> viewModel.removeIngredientFromOnboarding(index) },
                    onFinish = { viewModel.completeOnboarding() }
                )
            }
        }
    }
}

@Composable
fun Screen1Welcome(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF065F46),
                        Color(0xFF059669),
                        Color(0xFF047857)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BakeryDining,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to BatchCost",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 32.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Calculate your real product cost, selling price & profit.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureRow(icon = Icons.Default.MonetizationOn, title = "Know Your True Cost", desc = "Auto ingredient quantity to cost conversion")
                    FeatureRow(icon = Icons.Default.TrendingUp, title = "Maximize Profit Margin", desc = "Live price simulator & profit margins")
                    FeatureRow(icon = Icons.Default.Scale, title = "Batch Scaling", desc = "Instant cost calculation for any batch size")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_started_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF047857)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            Text(text = desc, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }
    }
}

@Composable
fun Screen2BusinessType(
    selectedType: String,
    customType: String,
    onSelectType: (String) -> Unit,
    onCustomTypeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val businessOptions = listOf(
        "Home Bakery" to Icons.Default.BakeryDining,
        "Cloud Kitchen" to Icons.Default.Restaurant,
        "Food Manufacturer" to Icons.Default.Storefront,
        "Homemade Products" to Icons.Default.LocalOffer,
        "Candle Business" to Icons.Default.LocalOffer,
        "Soap" to Icons.Default.LocalOffer,
        "Gift/Hampers" to Icons.Default.LocalOffer,
        "Other" to Icons.Default.Add
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Select Business Type",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Choose your line of production to customize units & presets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(businessOptions) { _, (label, icon) ->
                val isSelected = selectedType == label
                Card(
                    onClick = { onSelectType(label) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (selectedType == "Other") {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customType,
                        onValueChange = onCustomTypeChange,
                        label = { Text("Specify Custom Business Category") },
                        placeholder = { Text("e.g., Artisan Chocolates / Jewellery") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            enabled = selectedType.isNotBlank() && (selectedType != "Other" || customType.isNotBlank()),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("business_type_next_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next: Business Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen3BusinessDetails(
    state: OnboardingState,
    onUpdate: (String, String, String, String, String, String, String, String) -> Unit,
    onNext: () -> Unit
) {
    var bName by remember { mutableStateOf(state.businessName) }
    var owner by remember { mutableStateOf(state.ownerName) }
    var phone by remember { mutableStateOf(state.phone) }
    var email by remember { mutableStateOf(state.email) }
    var currencySym by remember { mutableStateOf(state.currencySymbol) }
    var currencyCode by remember { mutableStateOf(state.currencyCode) }
    var logo by remember { mutableStateOf(state.logoIdentifier) }
    var address by remember { mutableStateOf(state.address) }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Business Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Enter details to personalize your batch invoices & product reports.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                OutlinedTextField(
                    value = bName,
                    onValueChange = {
                        bName = it
                        onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                    },
                    label = { Text("Business Name *") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("business_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = owner,
                    onValueChange = {
                        owner = it
                        onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                    },
                    label = { Text("Owner / Manager Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("owner_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                    },
                    label = { Text("Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "$currencyCode ($currencySym)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = {
                                IconButton(onClick = { currencyDropdownExpanded = true }) {
                                    Text(currencySym, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            },
                            modifier = Modifier.clickable { currencyDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false }
                        ) {
                            CurrencyFormatter.SUPPORTED_CURRENCIES.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text("${curr.name} (${curr.symbol})") },
                                    onClick = {
                                        currencySym = curr.symbol
                                        currencyCode = curr.code
                                        currencyDropdownExpanded = false
                                        onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                        },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        onUpdate(bName, owner, phone, email, currencySym, currencyCode, logo, address)
                    },
                    label = { Text("Address / City (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            enabled = bName.isNotBlank() && owner.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("business_details_next_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next: Add First Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun Screen4FirstProduct(
    state: OnboardingState,
    onUpdate: (String, String, String, String, String, String, String) -> Unit,
    onNext: () -> Unit
) {
    var pName by remember { mutableStateOf(state.productName) }
    var price by remember { mutableStateOf(state.sellingPrice) }
    var unit by remember { mutableStateOf(state.productUnit) }
    var sku by remember { mutableStateOf(state.sku) }
    var cat by remember { mutableStateOf(state.category) }
    var desc by remember { mutableStateOf(state.description) }
    var weight by remember { mutableStateOf(state.weight) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Add Your First Product",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Enter selling details for your flagship product (e.g. Chocolate Cake).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                OutlinedTextField(
                    value = pName,
                    onValueChange = {
                        pName = it
                        onUpdate(pName, price, unit, sku, cat, desc, weight)
                    },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Chocolate Cake") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            onUpdate(pName, price, unit, sku, cat, desc, weight)
                        },
                        label = { Text("Selling Price (${state.currencySymbol}) *") },
                        prefix = { Text(state.currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_price_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = {
                            unit = it
                            onUpdate(pName, price, unit, sku, cat, desc, weight)
                        },
                        label = { Text("Unit *") },
                        placeholder = { Text("Piece / Box / Kg") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {
                            weight = it
                            onUpdate(pName, price, unit, sku, cat, desc, weight)
                        },
                        label = { Text("Net Weight / Size") },
                        placeholder = { Text("e.g. 1 kg / 500g") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cat,
                        onValueChange = {
                            cat = it
                            onUpdate(pName, price, unit, sku, cat, desc, weight)
                        },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = desc,
                    onValueChange = {
                        desc = it
                        onUpdate(pName, price, unit, sku, cat, desc, weight)
                    },
                    label = { Text("Product Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            enabled = pName.isNotBlank() && price.toDoubleOrNull() != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("first_product_next_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next: Add Ingredients", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun Screen5AddIngredients(
    state: OnboardingState,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (Int, TempIngredientItem) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val totalIngredientCost = state.ingredients.sumOf { it.calculatedCost() }
    val sellingPrice = state.sellingPrice.toDoubleOrNull() ?: 0.0
    val netProfit = sellingPrice - totalIngredientCost
    val marginPercent = if (sellingPrice > 0) (netProfit / sellingPrice) * 100.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add Ingredients for Recipe",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Enter purchase quantity, price & quantity used for ${state.productName}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Calculated Summary Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
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
                    Text(
                        text = "Calculated Cost",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.format(totalIngredientCost, state.currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Profit (${CurrencyFormatter.formatPercent(marginPercent)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.format(netProfit, state.currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipe Ingredients (${state.ingredients.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onAddIngredient) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(state.ingredients) { index, item ->
                SharedIngredientEditCard(
                    item = item,
                    currencySymbol = state.currencySymbol,
                    onUpdate = { updated -> onUpdateIngredient(index, updated) },
                    onDelete = { onRemoveIngredient(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinish,
            enabled = state.ingredients.any { it.name.isNotBlank() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("finish_onboarding_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Complete Setup & View Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
