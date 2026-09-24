package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.screens.label.LabelGeneratorScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.theme.BatchCostTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: BatchCostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
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
                var showVisualScanner by remember { mutableStateOf(false) }
                var showOrderLinkGen by remember { mutableStateOf(false) }

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
                                onBack = { showWhatsAppLink = false },
                                parsingConfig = config,
                                onImportOrder = { customer, product, qty, rev, cost, status, delivery, notes ->
                                    viewModel.recordNewOrder(customer, product, qty, rev, cost, status, delivery)
                                    showWhatsAppLink = false
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
                                topBar = {
                                    androidx.compose.material3.TopAppBar(
                                        title = { Text(activeProfile.businessName.ifBlank { "Bakery Cost & Profit Pro" }, fontWeight = FontWeight.Bold) },
                                        actions = {
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
                                                onOpenAIParsingConfig = { showAIParsingConfig = true }
                                            )
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
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
                                    onLoginSuccess = { email -> showAuthModal = false }
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
