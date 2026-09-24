package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.MasterIngredientDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.RecipeIngredientDao
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.MasterIngredientEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.RecipeIngredientEntity
import com.example.ui.AIParsingConfig
import com.example.ui.RecordedOrder
import com.example.ui.SavedBatchRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

class FirebaseSyncManager(
    private val profileDao: BusinessProfileDao,
    private val productDao: ProductDao,
    private val masterIngredientDao: MasterIngredientDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val scope: CoroutineScope
) {
    private val TAG = "FirebaseSyncManager"

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e(TAG, "FirebaseAuth init failed", e)
        null
    }

    private val firestore: FirebaseFirestore? = try {
        val app = FirebaseApp.getInstance()
        try {
            // Target specific firestore database ID
            FirebaseFirestore.getInstance(app, "ai-studio-batchcost-769eeb29-ae9d-4653-95cf-9e7e5c2f52ea")
        } catch (e: Exception) {
            FirebaseFirestore.getInstance(app)
        }
    } catch (e: Exception) {
        Log.e(TAG, "FirebaseFirestore init failed", e)
        null
    }

    private val rtdb: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance()
    } catch (e: Exception) {
        Log.e(TAG, "FirebaseDatabase init failed", e)
        null
    }

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.IDLE)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    init {
        ensureAuthenticated {
            // On startup, check and sync from cloud
            scope.launch {
                loadFromCloudInternal()
            }
        }
    }

    fun ensureAuthenticated(onAuthenticated: ((String) -> Unit)? = null) {
        val currentUid = auth?.currentUser?.uid
        if (currentUid != null) {
            onAuthenticated?.invoke(currentUid)
            return
        }

        // Seamless anonymous authentication so every device automatically has a cloud sync UID
        auth?.signInAnonymously()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newUid = auth.currentUser?.uid
                Log.d(TAG, "Firebase anonymous auth successful. UID: $newUid")
                if (newUid != null) {
                    onAuthenticated?.invoke(newUid)
                }
            } else {
                Log.w(TAG, "Firebase anonymous auth failed", task.exception)
            }
        }
    }

    fun getUserId(): String? {
        return auth?.currentUser?.uid
    }

    /**
     * Push all local data (Room DB + StateFlows) to Firebase Firestore & RTDB
     */
    fun syncAllToCloud(
        orders: List<RecordedOrder>,
        batches: List<SavedBatchRecord>,
        aliases: Map<String, String>,
        batchCount: Int,
        parsingConfig: AIParsingConfig,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val uid = getUserId()
        if (uid == null) {
            ensureAuthenticated { validUid ->
                performSync(validUid, orders, batches, aliases, batchCount, parsingConfig, onComplete)
            }
            return
        }

        performSync(uid, orders, batches, aliases, batchCount, parsingConfig, onComplete)
    }

    private fun performSync(
        uid: String,
        orders: List<RecordedOrder>,
        batches: List<SavedBatchRecord>,
        aliases: Map<String, String>,
        batchCount: Int,
        parsingConfig: AIParsingConfig,
        onComplete: ((Boolean) -> Unit)?
    ) {
        scope.launch(Dispatchers.IO) {
            _syncStatus.value = CloudSyncStatus.SYNCING
            try {
                val profile = profileDao.getBusinessProfileOnce()
                val products = productDao.getAllProductsSync()
                val recipeIngredients = recipeIngredientDao.getAllRecipeIngredientsSync()
                val masterIngredients = masterIngredientDao.getAllMasterIngredientsSync()

                val syncPayload = hashMapOf<String, Any>(
                    "updatedAt" to System.currentTimeMillis(),
                    "batchCount" to batchCount,
                    "productAliases" to aliases,
                    "parsingConfig" to mapOf(
                        "productNameRule" to parsingConfig.productNameRule,
                        "quantityRule" to parsingConfig.quantityRule,
                        "priceRule" to parsingConfig.priceRule,
                        "customContext" to parsingConfig.customContext
                    ),
                    "ordersCount" to orders.size,
                    "productsCount" to products.size,
                    "batchesCount" to batches.size
                )

                // 1. Sync to Firebase Firestore
                firestore?.let { db ->
                    val userDoc = db.collection("users").document(uid)
                    userDoc.set(syncPayload, SetOptions.merge()).await()

                    profile?.let {
                        userDoc.collection("profile").document("current").set(it).await()
                    }

                    // Save products
                    val productsCol = userDoc.collection("products")
                    for (p in products) {
                        productsCol.document(p.id.toString()).set(p).await()
                    }

                    // Save recipe ingredients
                    val recipeIngCol = userDoc.collection("recipe_ingredients")
                    for (ri in recipeIngredients) {
                        recipeIngCol.document(ri.id.toString()).set(ri).await()
                    }

                    // Save master ingredients
                    val masterIngCol = userDoc.collection("master_ingredients")
                    for (mi in masterIngredients) {
                        masterIngCol.document(mi.id.toString()).set(mi).await()
                    }

                    // Save orders
                    val ordersCol = userDoc.collection("orders")
                    for (o in orders) {
                        ordersCol.document(o.id).set(o).await()
                    }

                    // Save batches
                    val batchesCol = userDoc.collection("batches")
                    for (b in batches) {
                        batchesCol.document(b.id).set(b).await()
                    }
                }

                // 2. Also Mirror to Firebase Realtime Database
                rtdb?.let { db ->
                    val userRef = db.reference.child("users").child(uid)
                    val rtdbPayload = mapOf(
                        "profile" to profile,
                        "products" to products,
                        "recipeIngredients" to recipeIngredients,
                        "masterIngredients" to masterIngredients,
                        "orders" to orders,
                        "batches" to batches,
                        "batchCount" to batchCount,
                        "productAliases" to aliases,
                        "aiParsingConfig" to parsingConfig,
                        "lastSync" to System.currentTimeMillis()
                    )
                    userRef.setValue(rtdbPayload).await()
                }

                _lastSyncTimestamp.value = System.currentTimeMillis()
                _syncStatus.value = CloudSyncStatus.SUCCESS
                Log.d(TAG, "Full Firebase sync successful for UID: $uid")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sync failed", e)
                _syncStatus.value = CloudSyncStatus.ERROR
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    /**
     * Pull data from Firebase (Firestore / RTDB) and restore into local Room and callback
     */
    suspend fun loadFromCloud(
        onOrdersLoaded: (List<RecordedOrder>) -> Unit,
        onBatchesLoaded: (List<SavedBatchRecord>) -> Unit,
        onAliasesLoaded: (Map<String, String>) -> Unit,
        onBatchCountLoaded: (Int) -> Unit,
        onConfigLoaded: (AIParsingConfig) -> Unit
    ) = withContext(Dispatchers.IO) {
        val uid = getUserId() ?: return@withContext
        _syncStatus.value = CloudSyncStatus.SYNCING
        try {
            var restored = false

            // Try Firestore first
            firestore?.let { db ->
                val userDoc = db.collection("users").document(uid)
                val snapshot = userDoc.get().await()

                if (snapshot.exists()) {
                    // Profile
                    val profileSnap = userDoc.collection("profile").document("current").get().await()
                    if (profileSnap.exists()) {
                        profileSnap.toObject(BusinessProfileEntity::class.java)?.let {
                            profileDao.saveBusinessProfile(it)
                        }
                    }

                    // Products
                    val productsSnap = userDoc.collection("products").get().await()
                    val cloudProducts = productsSnap.documents.mapNotNull { it.toObject(ProductEntity::class.java) }
                    if (cloudProducts.isNotEmpty()) {
                        for (cp in cloudProducts) {
                            productDao.insertProduct(cp)
                        }
                    }

                    // Recipe Ingredients
                    val recipeSnap = userDoc.collection("recipe_ingredients").get().await()
                    val cloudRecipeIngs = recipeSnap.documents.mapNotNull { it.toObject(RecipeIngredientEntity::class.java) }
                    if (cloudRecipeIngs.isNotEmpty()) {
                        recipeIngredientDao.insertRecipeIngredients(cloudRecipeIngs)
                    }

                    // Master Ingredients
                    val masterSnap = userDoc.collection("master_ingredients").get().await()
                    val cloudMasterIngs = masterSnap.documents.mapNotNull { it.toObject(MasterIngredientEntity::class.java) }
                    if (cloudMasterIngs.isNotEmpty()) {
                        for (mi in cloudMasterIngs) {
                            masterIngredientDao.insertMasterIngredient(mi)
                        }
                    }

                    // Orders
                    val ordersSnap = userDoc.collection("orders").get().await()
                    val cloudOrders = ordersSnap.documents.mapNotNull { it.toObject(RecordedOrder::class.java) }
                    if (cloudOrders.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            onOrdersLoaded(cloudOrders)
                        }
                    }

                    // Batches
                    val batchesSnap = userDoc.collection("batches").get().await()
                    val cloudBatches = batchesSnap.documents.mapNotNull { it.toObject(SavedBatchRecord::class.java) }
                    if (cloudBatches.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            onBatchesLoaded(cloudBatches)
                        }
                    }

                    // Config & Aliases
                    val aliases = snapshot.get("productAliases") as? Map<String, String> ?: emptyMap()
                    val count = (snapshot.get("batchCount") as? Number)?.toInt() ?: 0
                    val configMap = snapshot.get("parsingConfig") as? Map<String, Any>
                    val config = if (configMap != null) {
                        AIParsingConfig(
                            productNameRule = configMap["productNameRule"] as? String ?: "",
                            quantityRule = configMap["quantityRule"] as? String ?: "",
                            priceRule = configMap["priceRule"] as? String ?: "",
                            customContext = configMap["customContext"] as? String ?: ""
                        )
                    } else {
                        AIParsingConfig()
                    }

                    withContext(Dispatchers.Main) {
                        if (aliases.isNotEmpty()) onAliasesLoaded(aliases)
                        onBatchCountLoaded(count)
                        onConfigLoaded(config)
                    }

                    restored = true
                }
            }

            // If not restored from Firestore, fallback to RTDB
            if (!restored) {
                rtdb?.let { db ->
                    val snapshot = db.reference.child("users").child(uid).get().await()
                    if (snapshot.exists()) {
                        val cloudOrders = snapshot.child("orders").children.mapNotNull { it.getValue(RecordedOrder::class.java) }
                        val cloudBatches = snapshot.child("batches").children.mapNotNull { it.getValue(SavedBatchRecord::class.java) }
                        val aliases = snapshot.child("productAliases").getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, String>>() {}) ?: emptyMap()
                        val count = snapshot.child("batchCount").getValue(Int::class.java) ?: 0
                        val config = snapshot.child("aiParsingConfig").getValue(AIParsingConfig::class.java) ?: AIParsingConfig()

                        withContext(Dispatchers.Main) {
                            if (cloudOrders.isNotEmpty()) onOrdersLoaded(cloudOrders)
                            if (cloudBatches.isNotEmpty()) onBatchesLoaded(cloudBatches)
                            if (aliases.isNotEmpty()) onAliasesLoaded(aliases)
                            onBatchCountLoaded(count)
                            onConfigLoaded(config)
                        }
                    }
                }
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncStatus.value = CloudSyncStatus.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from cloud", e)
            _syncStatus.value = CloudSyncStatus.ERROR
        }
    }

    private suspend fun loadFromCloudInternal() {
        // Internal silent check on launch
        val uid = getUserId() ?: return
        try {
            firestore?.let { db ->
                val snapshot = db.collection("users").document(uid).get().await()
                if (snapshot.exists()) {
                    val profileSnap = db.collection("users").document(uid).collection("profile").document("current").get().await()
                    if (profileSnap.exists()) {
                        profileSnap.toObject(BusinessProfileEntity::class.java)?.let {
                            if (profileDao.getBusinessProfileOnce() == null) {
                                profileDao.saveBusinessProfile(it)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore background init error
        }
    }
}
