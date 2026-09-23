package com.example.ui.screens.label

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.ProductWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val DESIGN_STYLES = listOf("Minimal", "Premium", "Bakery", "Homemade", "Luxury", "Classic")
val LABEL_SIZES = listOf("Standard (3\"×2\")", "Square Jar (2.5\"×2.5\")", "Circle Seal (2\"×2\")", "Box Label (4\"×3\")")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelGeneratorScreen(
    profile: BusinessProfileEntity,
    products: List<ProductWithDetails>,
    currencySymbol: String = "₹",
    initialProduct: ProductWithDetails? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedProductIndex by remember { mutableIntStateOf(0) }
    val activeProduct = products.getOrNull(selectedProductIndex) ?: initialProduct

    // Editable Label Fields
    var productName by remember(activeProduct) { mutableStateOf(activeProduct?.product?.name ?: "CHOCOLATE CAKE") }
    var netWeight by remember(activeProduct) { mutableStateOf(activeProduct?.product?.weight ?: "500g") }
    var ingredientsText by remember(activeProduct) {
        val ingList = activeProduct?.recipeIngredients?.map { it.ingredientName }?.joinToString(", ")
        mutableStateOf(if (!ingList.isNullOrBlank()) ingList else "Wheat Flour, Sugar, Cocoa Powder, Butter, Dark Chocolate, Milk, Eggs, Vanilla Extract, Raising Agents.")
    }
    var mrpText by remember(activeProduct) { mutableStateOf(activeProduct?.product?.sellingPrice?.let { "${it.toInt()}" } ?: "299") }
    var batchNo by remember { mutableStateOf("B${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())}") }
    var mfgDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var bestBefore by remember { mutableStateOf("5 Days from Mfg") }
    
    var businessName by remember { mutableStateOf(profile.businessName.ifBlank { "Sweet Delights Bakery" }) }
    var contactPhone by remember { mutableStateOf(profile.phone.ifBlank { "+91 98765 43210" }) }
    var addressText by remember { mutableStateOf("12 Baker Street, Model Town") }
    var socialHandle by remember { mutableStateOf("@sweetdelights_bakery") }
    var fssaiNumber by remember { mutableStateOf("FSSAI Lic: 21523098000123") }

    // Customization & Style Choices
    var selectedStyle by remember { mutableStateOf("Bakery") }
    var selectedSize by remember { mutableStateOf("Standard (3\"×2\")") }
    var isVeg by remember { mutableStateOf(true) }
    var showQrCode by remember { mutableStateOf(true) }
    var qrContent by remember { mutableStateOf("https://instagram.com/sweetdelights") }
    var activeTab by remember { mutableIntStateOf(0) }

    var showExportSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showQrInfoModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏷️ Label Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Printing label...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print Label")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Legal Compliance Disclaimer Banner
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Note: Labels remain 100% editable. Packaging regulations (FSSAI/FDA) depend on jurisdiction and product type. Verify statutory details before commercial printing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }

            // Product Dropdown Selector
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Select Product to Populate Template:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(products.size) { index ->
                                val p = products[index]
                                FilterChip(
                                    selected = selectedProductIndex == index,
                                    onClick = {
                                        selectedProductIndex = index
                                        productName = p.product.name.uppercase()
                                        netWeight = p.product.weight ?: "500g"
                                        mrpText = "${p.product.sellingPrice.toInt()}"
                                        val ingList = p.recipeIngredients.map { it.ingredientName }.joinToString(", ")
                                        if (ingList.isNotBlank()) ingredientsText = ingList
                                    },
                                    label = { Text(p.product.name) }
                                )
                            }
                        }
                    }
                }
            }

            // Tabs: Preview & Style / Content / Layout
            item {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("🎨 Design & Preview") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("✍️ Edit Details") }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("⚙️ Size & QR") }
                    )
                }
            }

            if (activeTab == 0) {
                // Style Selector Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Design Template Style:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(DESIGN_STYLES) { style ->
                                FilterChip(
                                    selected = selectedStyle == style,
                                    onClick = { selectedStyle = style },
                                    label = { Text(style, fontWeight = if (selectedStyle == style) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                    }
                }

                // Live Printable Label Preview Card
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LIVE PRINTABLE LABEL PREVIEW", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        LabelCardPreview(
                            style = selectedStyle,
                            labelSize = selectedSize,
                            productName = productName,
                            netWeight = netWeight,
                            ingredientsText = ingredientsText,
                            mrpText = mrpText,
                            currencySymbol = currencySymbol,
                            batchNo = batchNo,
                            mfgDate = mfgDate,
                            bestBefore = bestBefore,
                            businessName = businessName,
                            contactPhone = contactPhone,
                            addressText = addressText,
                            socialHandle = socialHandle,
                            fssaiNumber = fssaiNumber,
                            isVeg = isVeg,
                            showQrCode = showQrCode,
                            qrContent = qrContent,
                            onQrClick = { showQrInfoModal = true }
                        )
                    }
                }

                // Export Options Bar (PNG, JPG, PDF)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("📥 Export & Print High-Res Label:", fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showExportSuccessDialog = "PNG" },
                                    modifier = Modifier.weight(1f).testTag("export_png_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PNG", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showExportSuccessDialog = "JPG" },
                                    modifier = Modifier.weight(1f).testTag("export_jpg_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("JPG", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showExportSuccessDialog = "PDF" },
                                    modifier = Modifier.weight(1f).testTag("export_pdf_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == 1) {
                // Content Editor Form
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Edit Label Information:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = netWeight,
                                onValueChange = { netWeight = it },
                                label = { Text("Net Weight") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = mrpText,
                                onValueChange = { mrpText = it },
                                label = { Text("MRP ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = ingredientsText,
                            onValueChange = { ingredientsText = it },
                            label = { Text("Ingredients List") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = batchNo,
                                onValueChange = { batchNo = it },
                                label = { Text("Batch No.") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = mfgDate,
                                onValueChange = { mfgDate = it },
                                label = { Text("Mfg Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = bestBefore,
                            onValueChange = { bestBefore = it },
                            label = { Text("Best Before / Expiry") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text("Brand & Business Details:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Contact Phone") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = socialHandle,
                                onValueChange = { socialHandle = it },
                                label = { Text("Instagram / Social") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = fssaiNumber,
                            onValueChange = { fssaiNumber = it },
                            label = { Text("FSSAI / License No.") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            } else {
                // Size & QR Customization
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Label Dimensions & Layout:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(LABEL_SIZES) { size ->
                                FilterChip(
                                    selected = selectedSize == size,
                                    onClick = { selectedSize = size },
                                    label = { Text(size) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vegetarian Symbol (Green Dot):", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = isVeg,
                                onCheckedChange = { isVeg = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include QR Code on Label:", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = showQrCode,
                                onCheckedChange = { showQrCode = it }
                            )
                        }

                        if (showQrCode) {
                            OutlinedTextField(
                                value = qrContent,
                                onValueChange = { qrContent = it },
                                label = { Text("QR Link / UPI ID") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showQrInfoModal) {
        AlertDialog(
            onDismissRequest = { showQrInfoModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔳 Scanned Product Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✅ Verified Authentic Fresh Batch", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("• Product: $productName ($netWeight)", fontWeight = FontWeight.Bold)
                            Text("• Batch: $batchNo | Mfg Date: $mfgDate", style = MaterialTheme.typography.bodySmall)
                            Text("• Best Before: $bestBefore", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("📋 Ingredients:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(ingredientsText, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("🏬 Made Fresh By:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(businessName, fontWeight = FontWeight.Bold)
                        Text("$addressText | Ph: $contactPhone", style = MaterialTheme.typography.bodySmall)
                        Text("Social: $socialHandle | $fssaiNumber", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Want to order a fresh batch?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("Direct verification & order link", fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Opening WhatsApp order chat for $productName...", Toast.LENGTH_SHORT).show()
                                    showQrInfoModal = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Text("Order via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrInfoModal = false }) {
                    Text("Close Preview")
                }
            }
        )
    }
}

@Composable
fun LabelCardPreview(
    style: String,
    labelSize: String,
    productName: String,
    netWeight: String,
    ingredientsText: String,
    mrpText: String,
    currencySymbol: String,
    batchNo: String,
    mfgDate: String,
    bestBefore: String,
    businessName: String,
    contactPhone: String,
    addressText: String,
    socialHandle: String,
    fssaiNumber: String,
    isVeg: Boolean,
    showQrCode: Boolean,
    qrContent: String,
    onQrClick: () -> Unit = {}
) {
    val isCircle = labelSize.contains("Circle")

    // Theme Colors based on Style
    val bgColor = when (style) {
        "Minimal" -> Color.White
        "Premium" -> Color(0xFF0F172A)
        "Bakery" -> Color(0xFFFFFBEB)
        "Homemade" -> Color(0xFFF5E6D3)
        "Luxury" -> Color(0xFF18181B)
        "Classic" -> Color(0xFFFAFAFA)
        else -> Color.White
    }

    val textColor = when (style) {
        "Premium", "Luxury" -> Color(0xFFF8FAFC)
        else -> Color(0xFF1E293B)
    }

    val accentColor = when (style) {
        "Minimal" -> Color.Black
        "Premium" -> Color(0xFF38BDF8)
        "Bakery" -> Color(0xFFD97706)
        "Homemade" -> Color(0xFF854D0E)
        "Luxury" -> Color(0xFFEAB308)
        "Classic" -> Color(0xFF15803D)
        else -> Color.DarkGray
    }

    val cardShape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(2.dp, accentColor, cardShape)
            .testTag("label_preview_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Business Name & Veg Symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Veg Symbol Badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(1.5.dp, if (isVeg) Color(0xFF16A34A) else Color(0xFFDC2626)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isVeg) Color(0xFF16A34A) else Color(0xFFDC2626), CircleShape)
                    )
                }

                Text(
                    text = businessName.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text("HANDMADE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(accentColor.copy(alpha = 0.4f))
            )

            // Product Title
            Text(
                text = productName.ifBlank { "CHOCOLATE CAKE" },
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "NET WT: $netWeight",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            // Ingredients Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(textColor.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("INGREDIENTS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(
                        text = ingredientsText,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.85f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Price, Batch & Date Table Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("BATCH NO", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(batchNo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Column {
                    Text("MFG DATE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(mfgDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Column {
                    Text("BEST BEFORE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(bestBefore, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("MRP (INCL. TAXES)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currencySymbol$mrpText", fontSize = 14.sp, fontWeight = FontWeight.Black, color = accentColor)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(accentColor.copy(alpha = 0.3f))
            )

            // Footer: Contact & QR Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(addressText, fontSize = 9.sp, color = textColor.copy(alpha = 0.7f), maxLines = 1)
                    Text("Ph: $contactPhone | $socialHandle", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                    if (fssaiNumber.isNotBlank()) {
                        Text(fssaiNumber, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (showQrCode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable { onQrClick() }
                            .testTag("qr_code_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Code (Tap to Scan)",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
