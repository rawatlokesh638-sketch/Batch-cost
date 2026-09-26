package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BatchCostViewModel
import com.example.ui.ProductWithDetails
import com.example.ui.screens.calculator.BatchCalculatorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.pantry.IngredientsPantryScreen
import com.example.ui.screens.products.AddEditProductScreen
import com.example.ui.screens.products.ProductDetailScreen
import com.example.ui.screens.products.ProductsScreen
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.ui.screens.label.LabelGeneratorScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.theme.BatchCostTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: BatchCostViewModel by viewModels()
    private val incomingIntent = kotlinx.coroutines.flow.MutableStateFlow<android.content.Intent?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingIntent.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingIntent.value = intent
        try {
            com.example.util.GeminiService.init(this)
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setProjectId("axial-mind-bmbw7")
                    .setApplicationId("1:803498997787:web:abd330aad4be527dcb60be")
                    .setApiKey("AIzaSyD50PSKxaQlBjl5f6a9X4H4WnwZlOrYMC4")
                    .setDatabaseUrl("https://axial-mind-bmbw7-default-rtdb.firebaseio.com")
                    .setStorageBucket("axial-mind-bmbw7.firebasestorage.app")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
            val firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            // Log or handle error if needed
        }
        enableEdgeToEdge()

        setContent {
            BatchCostTheme {
                val prefs = remember { getSharedPreferences("app_prefs", MODE_PRIVATE) }
                var isLoggedIn by remember {
                    mutableStateOf(
                        try {
                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null || prefs.getBoolean("is_logged_in", false)
                        } catch (e: Exception) {
                            prefs.getBoolean("is_logged_in", false)
                        }
                    )
                }

                val currentIntent by incomingIntent.collectAsStateWithLifecycle()
                val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
                val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val productsWithDetails by viewModel.fullProductsWithDetails.collectAsStateWithLifecycle()
                val masterIngredients by viewModel.masterIngredients.collectAsStateWithLifecycle()
                val selectedProductId by viewModel.selectedProductId.collectAsStateWithLifecycle()
                val recordedOrders by viewModel.recordedOrders.collectAsStateWithLifecycle()
                val batchCount by viewModel.batchCount.collectAsStateWithLifecycle()
                val productAliases by viewModel.productAliases.collectAsStateWithLifecycle()
                val savedBatches by viewModel.savedBatches.collectAsStateWithLifecycle()

                var isAddingNewProduct by remember { mutableStateOf(false) }
                var editingProductWithDetails by remember { mutableStateOf<ProductWithDetails?>(null) }
                var showingLabelProduct by remember { mutableStateOf<ProductWithDetails?>(null) }

                var showPriceListModal by remember { mutableStateOf(false) }
                var showCrmSheet by remember { mutableStateOf(false) }
                var showRemindersModal by remember { mutableStateOf(false) }
                var showMultiBusinessModal by remember { mutableStateOf(false) }
                var showTeamModal by remember { mutableStateOf(false) }
                var showAuthModal by remember { mutableStateOf(false) }
                var showAiAssistantSheet by remember { mutableStateOf(false) }
                var showMonetizationModal by remember { mutableStateOf(false) }
                var showWhatsAppLink by remember { mutableStateOf(false) }
                var showAIParsingConfig by remember { mutableStateOf(false) }
                var showOrderLinkGen by remember { mutableStateOf(false) }
                var showVisualScanner by remember { mutableStateOf(false) }
                var sharedTextFromIntent by remember { mutableStateOf<String?>(null) }

                androidx.compose.runtime.LaunchedEffect(currentIntent) {
                    val it = currentIntent ?: return@LaunchedEffect
                    if (it.action == android.content.Intent.ACTION_SEND) {
                        if (it.type?.startsWith("text/") == true) {
                            val text = it.getStringExtra(android.content.Intent.EXTRA_TEXT)
                            if (!text.isNullOrBlank()) {
                                sharedTextFromIntent = text
                                showWhatsAppLink = true
                            }
                        } else if (it.type?.startsWith("image/") == true) {
                            showVisualScanner = true
                        }
                    }
                }

                // Global Auto-Clipboard Smart Detector
                val context = androidx.compose.ui.platform.LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var detectedClipboardText by remember { mutableStateOf<String?>(null) }
                var dismissedClipboardText by remember { mutableStateOf<String?>(null) }

                androidx.compose.runtime.DisposableEffect(Unit) {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    fun checkClip() {
                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                        val lower = clip.lowercase()
                        val isOrder = (lower.contains("cake") || lower.contains("cupcake") || lower.contains("pastry") ||
                                       lower.contains("kg") || lower.contains("eggless") || lower.contains("order") ||
                                       lower.contains("brownie") || lower.contains("rate") || lower.contains("delivery"))
                        if (isOrder && clip != dismissedClipboardText && clip.length > 5) {
                            detectedClipboardText = clip
                        }
                    }
                    val listener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
                        checkClip()
                    }
                    clipboard?.addPrimaryClipChangedListener(listener)
                    checkClip()
                    onDispose {
                        clipboard?.removePrimaryClipChangedListener(listener)
                    }
                }
                val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()

                if (!isLoggedIn) {
                    com.example.ui.components.LoginSignupScreen(
                        onLoginSuccess = { email ->
                            prefs.edit().putBoolean("is_logged_in", true).apply()
                            isLoggedIn = true
                        }
                    )
                } else if (profile == null || !profile!!.isOnboardingCompleted) {
                    OnboardingScreen(
                        state = onboardingState,
                        viewModel = viewModel
                    )
                } else {
                    val activeProfile = profile!!
                    val selectedProduct = productsWithDetails.find { it.product.id == selectedProductId }

                    when {
                        isAddingNewProduct -> {
                            AddEditProductScreen(
                                existingProductWithDetails = null,
                                currencySymbol = activeProfile.currencySymbol,
                                onBack = { isAddingNewProduct = false },
                                onSaveProduct = { product, ingredients ->
                                    viewModel.createProductWithIngredients(product, ingredients)
                                    isAddingNewProduct = false
                                }
                            )
                        }
                        editingProductWithDetails != null -> {
                            AddEditProductScreen(
                                existingProductWithDetails = editingProductWithDetails,
                                currencySymbol = activeProfile.currencySymbol,
                                onBack = { editingProductWithDetails = null },
                                onSaveProduct = { product, ingredients ->
                                    viewModel.updateProductAndIngredients(product, ingredients)
                                    editingProductWithDetails = null
                                }
                            )
                        }
                        showingLabelProduct != null -> {
                            LabelGeneratorScreen(
                                profile = activeProfile,
                                products = productsWithDetails,
                                currencySymbol = activeProfile.currencySymbol,
                                initialProduct = showingLabelProduct,
                                onBack = { showingLabelProduct = null }
                            )
                        }
                        selectedProduct != null -> {
                            ProductDetailScreen(
                                productWithDetails = selectedProduct,
                                currencySymbol = activeProfile.currencySymbol,
                                onBack = { viewModel.selectProductForDetail(null) },
                                onDelete = { prod ->
                                    viewModel.deleteProduct(prod)
                                },
                                onEdit = { item ->
                                    editingProductWithDetails = item
                                },
                                onGenerateLabel = { item ->
                                    showingLabelProduct = item
                                }
                            )
                        }
                        showWhatsAppLink -> {
                            val config by viewModel.aiParsingConfig.collectAsStateWithLifecycle()
                            com.example.ui.screens.whatsapp.WhatsAppIntegrationScreen(
                                onBack = { 
                                    showWhatsAppLink = false
                                    sharedTextFromIntent = null
                                },
                                parsingConfig = config,
                                initialSharedText = sharedTextFromIntent,
                                onImportOrder = { customer, product, qty, rev, cost, status, delivery, notes ->
                                    viewModel.recordNewOrder(customer, product, qty, rev, cost, status, delivery)
                                }
                            )
                        }
                        showAIParsingConfig -> {
                            val config by viewModel.aiParsingConfig.collectAsStateWithLifecycle()
                            com.example.ui.screens.settings.AIParsingConfigScreen(
                                currentConfig = config,
                                onBack = { showAIParsingConfig = false },
                                onSave = { newConfig ->
                                    viewModel.saveAIParsingConfig(newConfig)
                                    showAIParsingConfig = false
                                }
                            )
                        }
                        showVisualScanner -> {
                            val config by viewModel.aiParsingConfig.collectAsStateWithLifecycle()
                            com.example.ui.screens.orders.VisualOrderScannerScreen(
                                parsingConfig = config,
                                onBack = { showVisualScanner = false },
                                onImportOrders = { orders ->
                                    orders.forEach { order ->
                                        viewModel.recordNewOrder(
                                            order.customerName,
                                            order.productName,
                                            order.quantity,
                                            order.totalRevenue,
                                            order.totalCost,
                                            order.status,
                                            order.deliveryDate
                                        )
                                    }
                                    showVisualScanner = false
                                }
                            )
                        }
                        showOrderLinkGen -> {
                            com.example.ui.screens.settings.OrderLinkGeneratorScreen(
                                profile = activeProfile,
                                onBack = { showOrderLinkGen = false }
                            )
                        }
                        else -> {
                            androidx.compose.material3.Scaffold(
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                topBar = {
                                    androidx.compose.material3.TopAppBar(
                                        title = { Text(activeProfile.businessName.ifBlank { "Bakery Cost & Profit Pro" }, fontWeight = FontWeight.Bold) },
                                        actions = {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Syncing to Firebase Cloud...")
                                                        viewModel.syncDataToFirebase { ok ->
                                                            scope.launch {
                                                                if (ok) snackbarHostState.showSnackbar("☁️ Firebase Cloud Sync Complete!")
                                                                else snackbarHostState.showSnackbar("⚠️ Cloud sync error. Saved locally.")
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                when (cloudSyncStatus) {
                                                    com.example.data.firebase.CloudSyncStatus.SYNCING -> {
                                                        CircularProgressIndicator(modifier = Modifier.padding(6.dp), strokeWidth = 2.dp)
                                                    }
                                                    com.example.data.firebase.CloudSyncStatus.ERROR -> {
                                                        Icon(Icons.Default.CloudOff, contentDescription = "Sync Error", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                    else -> {
                                                        Icon(Icons.Default.CloudDone, contentDescription = "Cloud Synced", tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }

                                            com.example.ui.components.TopOverflowMenu(
                                                onSelectTab = { viewModel.selectTab(it) },
                                                onOpenPriceList = { showPriceListModal = true },
                                                onOpenCrm = { showCrmSheet = true },
                                                onOpenReminders = { showRemindersModal = true },
                                                onOpenMultiBusiness = { showMultiBusinessModal = true },
                                                onOpenTeam = { showTeamModal = true },
                                                onOpenAuth = { showAuthModal = true },
                                                onOpenAiAssistant = { showAiAssistantSheet = true },
                                                onOpenMonetization = { showMonetizationModal = true },
                                                onOpenWhatsAppLink = { showWhatsAppLink = true },
                                                onOpenAIParsingConfig = { showAIParsingConfig = true },
                                                onManualSync = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Syncing to Firebase Cloud...")
                                                        viewModel.syncDataToFirebase { ok ->
                                                            scope.launch {
                                                                if (ok) snackbarHostState.showSnackbar("☁️ All business data synced to Firebase!")
                                                                else snackbarHostState.showSnackbar("⚠️ Cloud sync error. Saved locally.")
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    // Global Auto-Clipboard Smart Detector Banner
                                    androidx.compose.animation.AnimatedVisibility(visible = detectedClipboardText != null) {
                                        androidx.compose.material3.Card(
                                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0F766E)),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    Icons.Default.ContentPaste,
                                                    contentDescription = null,
                                                    tint = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("WhatsApp Order Copied!", color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
                                                    Text(
                                                        "\"${detectedClipboardText?.take(50)}...\"",
                                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                                        fontSize = 11.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                androidx.compose.material3.Button(
                                                    onClick = {
                                                        val textToScan = detectedClipboardText ?: ""
                                                        detectedClipboardText = null
                                                        dismissedClipboardText = textToScan
                                                        scope.launch {
                                                            com.example.service.WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage(
                                                                senderName = "Clipboard Customer",
                                                                messageText = textToScan,
                                                                source = "Auto Clipboard Bar",
                                                                context = context
                                                            )
                                                            snackbarHostState.showSnackbar("⚡ Order extracted & synced to Firebase Realtime DB!")
                                                        }
                                                    },
                                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)),
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("⚡ AI Save", color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 11.sp)
                                                }
                                                androidx.compose.material3.IconButton(
                                                    onClick = {
                                                        dismissedClipboardText = detectedClipboardText
                                                        detectedClipboardText = null
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    androidx.compose.material3.Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Dismiss",
                                                        tint = androidx.compose.ui.graphics.Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                    ) {
                                        AnimatedContent(
                                            targetState = currentTab,
                                            label = "TabContent"
                                        ) { tab ->
                                        when (tab) {
                                            0 -> {
                                                val config by viewModel.aiParsingConfig.collectAsStateWithLifecycle()
                                                DashboardScreen(
                                                    profile = activeProfile,
                                                    products = productsWithDetails,
                                                    recordedOrders = recordedOrders,
                                                    batchCount = batchCount,
                                                    parsingConfig = config,
                                                    onNavigateToProducts = { viewModel.selectTab(1) },
                                                    onNavigateToBatchCalc = { viewModel.selectTab(3) },
                                                    onSelectProduct = { id -> viewModel.selectProductForDetail(id) },
                                                    onAddNewProduct = { isAddingNewProduct = true },
                                                    onRecordNewOrder = { name, prod, qty, rev, cost, status, delivery ->
                                                        viewModel.recordNewOrder(name, prod, qty, rev, cost, status, delivery)
                                                    },
                                                    onIncrementBatchCount = {
                                                        viewModel.incrementBatchCount()
                                                    },
                                                    onOpenWhatsApp = { showWhatsAppLink = true },
                                                    onOpenVisualScanner = { showVisualScanner = true },
                                                    onOpenOrderLinkGen = { showOrderLinkGen = true }
                                                )
                                            }
                                            1 -> ProductsScreen(
                                                products = productsWithDetails,
                                                currencySymbol = activeProfile.currencySymbol,
                                                onSelectProduct = { id -> viewModel.selectProductForDetail(id) },
                                                onAddNewProduct = { isAddingNewProduct = true }
                                            )
                                            2 -> OrdersScreen(
                                                orders = recordedOrders,
                                                products = productsWithDetails,
                                                productAliases = productAliases,
                                                currencySymbol = activeProfile.currencySymbol,
                                                onAddOrder = { order -> viewModel.recordNewOrder(order.customerName, order.productName, order.quantity, order.totalRevenue, order.totalCost, order.status, order.deliveryDate) },
                                                onUpdateStatus = { id, status -> viewModel.updateOrderStatus(id, status) },
                                                onDeleteOrder = { id -> viewModel.deleteOrder(id) },
                                                onSaveAlias = { alias, name -> viewModel.saveProductAlias(alias, name) }
                                            )
                                            3 -> LabelGeneratorScreen(
                                                profile = activeProfile,
                                                products = productsWithDetails,
                                                currencySymbol = activeProfile.currencySymbol
                                            )
                                            4 -> IngredientsPantryScreen(
                                                masterIngredients = masterIngredients,
                                                currencySymbol = activeProfile.currencySymbol,
                                                onAddIngredient = { ing -> viewModel.addMasterIngredient(ing) },
                                                onUpdateIngredient = { ing -> viewModel.updateMasterIngredient(ing) },
                                                onDeleteIngredient = { ing -> viewModel.deleteMasterIngredient(ing) }
                                            )
                                            5 -> BatchCalculatorScreen(
                                                products = productsWithDetails,
                                                currencySymbol = activeProfile.currencySymbol,
                                                savedBatches = savedBatches,
                                                onSaveBatchRecord = { name, units, cost, price, notes ->
                                                    viewModel.saveNewBatchRecord(name, units, cost, price, notes)
                                                },
                                                onDuplicateBatchRecord = { id, newUnits ->
                                                    viewModel.duplicateBatchRecord(id, newUnits)
                                                },
                                                onDeleteBatchRecord = { id ->
                                                    viewModel.deleteBatchRecord(id)
                                                },
                                                onSaveBatch = {
                                                    viewModel.incrementBatchCount()
                                                }
                                            )
                                            6 -> com.example.ui.screens.settings.BusinessSettingsScreen(
                                                profile = activeProfile,
                                                onSaveProfile = { updated -> viewModel.updateProfile(updated) },
                                                onResetData = { viewModel.resetAppToOnboarding() }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                            if (showPriceListModal) {
                                com.example.ui.components.PriceListGeneratorDialog(
                                    products = productsWithDetails,
                                    profile = activeProfile,
                                    onDismiss = { showPriceListModal = false }
                                )
                            }
                            if (showCrmSheet) {
                                com.example.ui.components.CustomerCrmSheet(
                                    recordedOrders = recordedOrders,
                                    currencySymbol = activeProfile.currencySymbol,
                                    onDismiss = { showCrmSheet = false },
                                    onCreateRepeatOrder = { customer ->
                                        showCrmSheet = false
                                        viewModel.recordNewOrder(customer.name, customer.lastProduct, customer.lastQuantity, 400.0, 200.0)
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
                                    currentBusinessName = activeProfile.businessName,
                                    onDismiss = { showMultiBusinessModal = false },
                                    onSelectBusiness = { bus -> showMultiBusinessModal = false }
                                )
                            }
                            if (showTeamModal) {
                                com.example.ui.components.TeamManagementSheet(
                                    onDismiss = { showTeamModal = false }
                                )
                            }
                            if (showAuthModal) {
                                com.example.ui.components.FirebaseAuthDialog(
                                    currentUserEmail = activeProfile.email ?: "",
                                    onDismiss = { showAuthModal = false },
                                    onLoginSuccess = { email -> 
                                        showAuthModal = false
                                        viewModel.loadDataFromFirebase()
                                        viewModel.syncDataToFirebase()
                                    }
                                )
                            }
                            if (showAiAssistantSheet) {
                                com.example.ui.components.AIBusinessAssistantSheet(
                                    products = productsWithDetails,
                                    masterIngredients = masterIngredients,
                                    recordedOrders = recordedOrders,
                                    currencySymbol = activeProfile.currencySymbol,
                                    onDismiss = { showAiAssistantSheet = false }
                                )
                            }
                            if (showMonetizationModal) {
                                com.example.ui.components.MonetizationPlansDialog(
                                    currentPlan = "PRO",
                                    onDismiss = { showMonetizationModal = false },
                                    onSelectPlan = { plan -> showMonetizationModal = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
