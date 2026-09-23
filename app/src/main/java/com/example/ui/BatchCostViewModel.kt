package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.MasterIngredientEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.RecipeIngredientEntity
import com.example.util.CurrencyFormatter
import com.example.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TempIngredientItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val purchaseQty: String = "1",
    val purchaseUnit: String = "kg",
    val purchasePrice: String = "",
    val usedQty: String = "",
    val usedUnit: String = "g",
    val wastageType: String = "No wastage", // "No wastage", "Fixed %", "Actual waste"
    val wastagePercent: String = "0",
    val actualWasteQty: String = "0"
) {
    fun calculatedCost(): Double {
        val pQty = purchaseQty.toDoubleOrNull() ?: 0.0
        val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
        val uQty = usedQty.toDoubleOrNull() ?: 0.0
        val baseCost = UnitConverter.calculateCost(pQty, purchaseUnit, pPrice, uQty, usedUnit)

        return when (wastageType) {
            "Fixed %" -> {
                val pct = wastagePercent.toDoubleOrNull() ?: 0.0
                baseCost * (1.0 + (pct / 100.0))
            }
            "Actual waste" -> {
                val wasteVal = actualWasteQty.toDoubleOrNull() ?: 0.0
                val totalConsumedQty = uQty + wasteVal
                UnitConverter.calculateCost(pQty, purchaseUnit, pPrice, totalConsumedQty, usedUnit)
            }
            else -> baseCost
        }
    }
}

data class ProductWithDetails(
    val product: ProductEntity,
    val recipeIngredients: List<RecipeIngredientEntity>
) {
    val rawIngredientsCost: Double = recipeIngredients.sumOf {
        UnitConverter.calculateCost(
            it.purchaseQty,
            it.purchaseUnit,
            it.purchasePrice,
            it.usedQty,
            it.usedUnit
        )
    }

    val wastageCost: Double = rawIngredientsCost * (product.wastagePercent / 100.0)
    val packagingCost: Double = product.packagingCost
    val labourCost: Double = product.labourCost
    val overheadCost: Double = product.electricityGasCost + product.otherOverhead

    val totalCost: Double = rawIngredientsCost + wastageCost + packagingCost + labourCost + overheadCost
    val netProfit: Double = product.sellingPrice - totalCost
    val profitMarginPercent: Double = if (product.sellingPrice > 0) (netProfit / product.sellingPrice) * 100.0 else 0.0
    val markupPercent: Double = if (totalCost > 0) (netProfit / totalCost) * 100.0 else 0.0
}

data class SavedBatchRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productName: String,
    val date: String,
    val unitsProduced: Int,
    val totalCost: Double,
    val costPerUnit: Double,
    val sellingPricePerUnit: Double,
    val estimatedProfit: Double,
    val ingredientsSummary: String = "Wheat Flour, Sugar, Butter, Cocoa, Eggs",
    val notes: String = "Standard commercial batch"
)

data class RecordedOrder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val customerName: String,
    val productName: String,
    val quantity: Int,
    val totalRevenue: Double,
    val totalCost: Double,
    val deliveryFee: Double = 50.0,
    val platformFee: Double = 30.0,
    val paymentGatewayFee: Double = 12.0,
    val marketingFee: Double = 20.0,
    val status: String = "New",
    val deliveryDate: String = "Tomorrow",
    val date: String = "Today"
) {
    val totalFees: Double get() = deliveryFee + platformFee + paymentGatewayFee + marketingFee
    val totalAllCosts: Double get() = totalCost + totalFees
    val profit: Double get() = totalRevenue - totalAllCosts
}

data class OnboardingState(
    val step: Int = 1,
    val businessType: String = "",
    val customBusinessType: String = "",
    val businessName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val currencySymbol: String = "₹",
    val currencyCode: String = "INR",
    val logoIdentifier: String = "bakery",
    val address: String = "",
    
    // First Product
    val productName: String = "",
    val sellingPrice: String = "",
    val productUnit: String = "Piece",
    val sku: String = "",
    val category: String = "",
    val description: String = "",
    val weight: String = "",
    
    // Initial Ingredients list (empty for zeroed out start)
    val ingredients: List<TempIngredientItem> = emptyList()
)

class BatchCostViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val profileDao = db.businessProfileDao()
    private val productDao = db.productDao()
    private val masterIngredientDao = db.masterIngredientDao()
    private val recipeIngredientDao = db.recipeIngredientDao()

    private val rtdbRef = try {
        com.google.firebase.database.FirebaseDatabase.getInstance().reference
    } catch (e: Exception) {
        null
    }

    val businessProfile: StateFlow<BusinessProfileEntity?> = profileDao.getBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProducts: StateFlow<List<ProductEntity>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterIngredients: StateFlow<List<MasterIngredientEntity>> = masterIngredientDao.getAllMasterIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipeIngredients: StateFlow<List<RecipeIngredientEntity>> = recipeIngredientDao.getAllRecipeIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fullProductsWithDetails: StateFlow<List<ProductWithDetails>> = combine(
        allProducts,
        allRecipeIngredients
    ) { products, recipes ->
        products.map { prod ->
            val ingredients = recipes.filter { it.productId == prod.id }
            ProductWithDetails(prod, ingredients)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Onboarding UI State
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

    // Navigation state
    private val _currentTab = MutableStateFlow(0) // 0: Dashboard, 1: Products, 2: Pantry, 3: Batch Calc, 4: Settings
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedProductId = MutableStateFlow<Long?>(null)
    val selectedProductId: StateFlow<Long?> = _selectedProductId.asStateFlow()

    // Dashboard Orders & Batches metrics (Zeroed out by default)
    private val _recordedOrders = MutableStateFlow<List<RecordedOrder>>(emptyList())
    val recordedOrders: StateFlow<List<RecordedOrder>> = _recordedOrders.asStateFlow()

    private val _batchCount = MutableStateFlow(0)
    val batchCount: StateFlow<Int> = _batchCount.asStateFlow()

    private val _savedBatches = MutableStateFlow<List<SavedBatchRecord>>(emptyList())
    val savedBatches: StateFlow<List<SavedBatchRecord>> = _savedBatches.asStateFlow()

    fun saveNewBatchRecord(productName: String, units: Int, totalCost: Double, sellingPrice: Double, notes: String = "") {
        val costUnit = if (units > 0) totalCost / units else 0.0
        val estProfit = (sellingPrice * units) - totalCost
        val newBatch = SavedBatchRecord(
            productName = productName,
            date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            unitsProduced = units,
            totalCost = totalCost,
            costPerUnit = costUnit,
            sellingPricePerUnit = sellingPrice,
            estimatedProfit = estProfit,
            notes = notes.ifBlank { "Recorded batch" }
        )
        _savedBatches.update { listOf(newBatch) + it }
        _batchCount.update { it + 1 }
        syncDataToFirebase()
    }

    fun duplicateBatchRecord(targetBatchId: String, newUnitsCount: Int) {
        val target = _savedBatches.value.find { it.id == targetBatchId } ?: return
        if (target.unitsProduced <= 0 || newUnitsCount <= 0) return

        val scaleRatio = newUnitsCount.toDouble() / target.unitsProduced.toDouble()
        val scaledTotalCost = target.totalCost * scaleRatio
        val scaledCostPerUnit = scaledTotalCost / newUnitsCount
        val scaledEstProfit = (target.sellingPricePerUnit * newUnitsCount) - scaledTotalCost

        val duplicated = SavedBatchRecord(
            productName = target.productName,
            date = java.text.SimpleDateFormat("dd MMM yyyy (Scaled)", java.util.Locale.getDefault()).format(java.util.Date()),
            unitsProduced = newUnitsCount,
            totalCost = scaledTotalCost,
            costPerUnit = scaledCostPerUnit,
            sellingPricePerUnit = target.sellingPricePerUnit,
            estimatedProfit = scaledEstProfit,
            ingredientsSummary = "Scaled ${String.format("%.1fx", scaleRatio)} from previous batch",
            notes = "Duplicated from batch (${target.unitsProduced} ➔ $newUnitsCount units)"
        )
        _savedBatches.update { listOf(duplicated) + it }
        _batchCount.update { it + 1 }
        syncDataToFirebase()
    }

    fun deleteBatchRecord(batchId: String) {
        _savedBatches.update { list -> list.filterNot { it.id == batchId } }
        syncDataToFirebase()
    }

    fun recordNewOrder(customerName: String, productName: String, qty: Int, revenue: Double, cost: Double, status: String = "Pending", deliveryDate: String = "Tomorrow") {
        val newOrder = RecordedOrder(
            customerName = customerName,
            productName = productName,
            quantity = qty,
            totalRevenue = revenue,
            totalCost = cost,
            status = status,
            deliveryDate = deliveryDate,
            date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        )
        _recordedOrders.update { listOf(newOrder) + it }
        syncDataToFirebase()
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _recordedOrders.update { list ->
            list.map { if (it.id == orderId) it.copy(status = newStatus) else it }
        }
        syncDataToFirebase()
    }

    // Smart Product Matching & Aliases Memory
    private val _productAliases = MutableStateFlow<Map<String, String>>(emptyMap())
    val productAliases: StateFlow<Map<String, String>> = _productAliases.asStateFlow()

    fun saveProductAlias(aliasTerm: String, matchedProductName: String) {
        val cleanKey = aliasTerm.lowercase().trim()
        if (cleanKey.isNotBlank() && matchedProductName.isNotBlank()) {
            _productAliases.update { it + (cleanKey to matchedProductName) }
            syncDataToFirebase()
        }
    }

    fun deleteOrder(orderId: String) {
        _recordedOrders.update { list -> list.filterNot { it.id == orderId } }
        syncDataToFirebase()
    }

    fun incrementBatchCount() {
        _batchCount.update { it + 1 }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun selectProductForDetail(productId: Long?) {
        _selectedProductId.value = productId
    }

    // Onboarding updates
    fun updateOnboardingStep(step: Int) {
        _onboardingState.update { it.copy(step = step) }
    }

    fun updateBusinessType(type: String) {
        _onboardingState.update { it.copy(businessType = type) }
    }

    fun updateCustomBusinessType(type: String) {
        _onboardingState.update { it.copy(customBusinessType = type) }
    }

    fun updateBusinessDetails(
        name: String,
        owner: String,
        phone: String,
        email: String,
        currencySymbol: String,
        currencyCode: String,
        logo: String,
        address: String
    ) {
        _onboardingState.update {
            it.copy(
                businessName = name,
                ownerName = owner,
                phone = phone,
                email = email,
                currencySymbol = currencySymbol,
                currencyCode = currencyCode,
                logoIdentifier = logo,
                address = address
            )
        }
    }

    fun updateFirstProduct(
        name: String,
        sellingPrice: String,
        unit: String,
        sku: String,
        category: String,
        description: String,
        weight: String
    ) {
        _onboardingState.update {
            it.copy(
                productName = name,
                sellingPrice = sellingPrice,
                productUnit = unit,
                sku = sku,
                category = category,
                description = description,
                weight = weight
            )
        }
    }

    fun addIngredientToOnboarding() {
        _onboardingState.update {
            it.copy(ingredients = it.ingredients + TempIngredientItem())
        }
    }

    fun updateIngredientInOnboarding(index: Int, updated: TempIngredientItem) {
        _onboardingState.update { current ->
            val list = current.ingredients.toMutableList()
            if (index in list.indices) {
                list[index] = updated
            }
            current.copy(ingredients = list)
        }
    }

    fun removeIngredientFromOnboarding(index: Int) {
        _onboardingState.update { current ->
            val list = current.ingredients.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            current.copy(ingredients = list)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _onboardingState.value

            val profile = BusinessProfileEntity(
                id = 1,
                businessName = state.businessName,
                ownerName = state.ownerName,
                phone = state.phone,
                email = state.email.ifBlank { null },
                businessType = state.businessType,
                customBusinessType = state.customBusinessType.ifBlank { null },
                currencySymbol = state.currencySymbol,
                currencyCode = state.currencyCode,
                logoIdentifier = state.logoIdentifier,
                address = state.address.ifBlank { null },
                isOnboardingCompleted = true
            )
            profileDao.saveBusinessProfile(profile)

            if (state.productName.isNotBlank()) {
                val product = ProductEntity(
                    name = state.productName,
                    sellingPrice = state.sellingPrice.toDoubleOrNull() ?: 0.0,
                    unit = state.productUnit.ifBlank { "Piece" },
                    sku = state.sku.ifBlank { null },
                    category = state.category.ifBlank { "General" },
                    description = state.description.ifBlank { null },
                    weight = state.weight.ifBlank { null }
                )
                val productId = productDao.insertProduct(product)

                val recipeEntities = state.ingredients.filter { it.name.isNotBlank() }.map { ing ->
                    val pQty = ing.purchaseQty.toDoubleOrNull() ?: 1.0
                    val pPrice = ing.purchasePrice.toDoubleOrNull() ?: 0.0
                    val uQty = ing.usedQty.toDoubleOrNull() ?: 0.0

                    // Also save to master ingredients pantry
                    masterIngredientDao.insertMasterIngredient(
                        MasterIngredientEntity(
                            name = ing.name,
                            purchaseQty = pQty,
                            purchaseUnit = ing.purchaseUnit,
                            purchasePrice = pPrice,
                            category = state.category
                        )
                    )

                    RecipeIngredientEntity(
                        productId = productId,
                        ingredientName = ing.name,
                        purchaseQty = pQty,
                        purchaseUnit = ing.purchaseUnit,
                        purchasePrice = pPrice,
                        usedQty = uQty,
                        usedUnit = ing.usedUnit
                    )
                }
                recipeIngredientDao.insertRecipeIngredients(recipeEntities)
            }
            syncDataToFirebase()
        }
    }

    private fun syncDataToFirebase() {
        val uid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        } ?: return
        
        val ref = rtdbRef?.child("users")?.child(uid) ?: return

        viewModelScope.launch {
            val profile = profileDao.getBusinessProfileOnce()
            val products = productDao.getAllProductsSync()
            val ingredients = recipeIngredientDao.getAllRecipeIngredientsSync()
            val masterIngs = masterIngredientDao.getAllMasterIngredientsSync()

            val data = mapOf(
                "profile" to profile,
                "products" to products,
                "recipeIngredients" to ingredients,
                "masterIngredients" to masterIngs,
                "orders" to _recordedOrders.value,
                "batches" to _savedBatches.value,
                "batchCount" to _batchCount.value,
                "productAliases" to _productAliases.value
            )
            ref.setValue(data)
        }
    }

    // Product operations
    fun createProductWithIngredients(
        product: ProductEntity,
        ingredients: List<TempIngredientItem>
    ) {
        viewModelScope.launch {
            val productId = productDao.insertProduct(product)
            val recipeEntities = ingredients.filter { it.name.isNotBlank() }.map { ing ->
                val pQty = ing.purchaseQty.toDoubleOrNull() ?: 1.0
                val pPrice = ing.purchasePrice.toDoubleOrNull() ?: 0.0
                val uQty = ing.usedQty.toDoubleOrNull() ?: 0.0

                RecipeIngredientEntity(
                    productId = productId,
                    ingredientName = ing.name,
                    purchaseQty = pQty,
                    purchaseUnit = ing.purchaseUnit,
                    purchasePrice = pPrice,
                    usedQty = uQty,
                    usedUnit = ing.usedUnit
                )
            }
            recipeIngredientDao.insertRecipeIngredients(recipeEntities)
            syncDataToFirebase()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            recipeIngredientDao.deleteIngredientsForProduct(product.id)
            productDao.deleteProduct(product)
            if (_selectedProductId.value == product.id) {
                _selectedProductId.value = null
            }
            syncDataToFirebase()
        }
    }

    fun updateProductAndIngredients(
        product: ProductEntity,
        ingredients: List<TempIngredientItem>
    ) {
        viewModelScope.launch {
            productDao.updateProduct(product)
            recipeIngredientDao.deleteIngredientsForProduct(product.id)
            val recipeEntities = ingredients.filter { it.name.isNotBlank() }.map { ing ->
                RecipeIngredientEntity(
                    productId = product.id,
                    ingredientName = ing.name,
                    purchaseQty = ing.purchaseQty.toDoubleOrNull() ?: 1.0,
                    purchaseUnit = ing.purchaseUnit,
                    purchasePrice = ing.purchasePrice.toDoubleOrNull() ?: 0.0,
                    usedQty = ing.usedQty.toDoubleOrNull() ?: 0.0,
                    usedUnit = ing.usedUnit
                )
            }
            recipeIngredientDao.insertRecipeIngredients(recipeEntities)
            syncDataToFirebase()
        }
    }

    // Master Ingredient operations
    fun addMasterIngredient(ingredient: MasterIngredientEntity) {
        viewModelScope.launch {
            masterIngredientDao.insertMasterIngredient(ingredient)
            syncDataToFirebase()
        }
    }

    fun updateMasterIngredient(ingredient: MasterIngredientEntity) {
        viewModelScope.launch {
            masterIngredientDao.updateMasterIngredient(ingredient)
            syncDataToFirebase()
        }
    }

    fun deleteMasterIngredient(ingredient: MasterIngredientEntity) {
        viewModelScope.launch {
            masterIngredientDao.deleteMasterIngredient(ingredient)
            syncDataToFirebase()
        }
    }

    fun updateProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            profileDao.saveBusinessProfile(profile)
            syncDataToFirebase()
        }
    }

    fun resetAppToOnboarding() {
        viewModelScope.launch {
            recipeIngredientDao.clearRecipeIngredients()
            productDao.clearProducts()
            masterIngredientDao.clearMasterIngredients()
            profileDao.clearProfile()
            _onboardingState.value = OnboardingState()
            _selectedProductId.value = null
            _currentTab.value = 0
            _recordedOrders.value = emptyList()
            _savedBatches.value = emptyList()
            _batchCount.value = 0
            _productAliases.value = emptyMap()
            syncDataToFirebase()
        }
    }
}
