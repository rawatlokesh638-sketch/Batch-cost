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
import java.util.UUID

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

class FirebaseSyncManager(
    private val context: Context,
    private val profileDao: BusinessProfileDao,
    private val productDao: ProductDao,
    private val masterIngredientDao: MasterIngredientDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val scope: CoroutineScope
) {
    private val TAG = "FirebaseSyncManager"
    private val RTDB_URL = "https://gen-lang-client-0661181674-default-rtdb.firebaseio.com"
    private val FIRESTORE_DB_ID = "ai-studio-batchcost-769eeb29-ae9d-4653-95cf-9e7e5c2f52ea"

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth init failed", e)
            null
        }
    }

    private val firestoreNamed: FirebaseFirestore? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            FirebaseFirestore.getInstance(app, FIRESTORE_DB_ID)
        } catch (e: Exception) {
            Log.w(TAG, "Named Firestore init failed: ${e.message}")
            null
        }
    }

    private val firestoreDefault: FirebaseFirestore? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            FirebaseFirestore.getInstance(app)
        } catch (e: Exception) {
            Log.w(TAG, "Default Firestore init failed: ${e.message}")
            null
        }
    }

    private val rtdbNamed: FirebaseDatabase? by lazy {
        try {
            val db = FirebaseDatabase.getInstance(RTDB_URL)
            db.setPersistenceEnabled(true)
            db
        } catch (e: Exception) {
            try {
                FirebaseDatabase.getInstance(RTDB_URL)
            } catch (e2: Exception) {
                Log.w(TAG, "Named RTDB init failed: ${e2.message}")
                null
            }
        }
    }

    private val rtdbDefault: FirebaseDatabase? by lazy {
        try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Default RTDB init failed: ${e.message}")
            null
        }
    }

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.IDLE)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncMessage = MutableStateFlow("Firebase Ready")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    init {
        // Try background anonymous auth if not logged in
        try {
            if (auth?.currentUser == null) {
                auth?.signInAnonymously()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Anonymous sign-in success: ${task.result?.user?.uid}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous auth attempt caught: ${e.message}")
        }
    }

    /**
     * Resolves a guaranteed non-null User ID.
     * Uses FirebaseAuth UID if available; otherwise uses a persistent device ID.
     */
    fun resolveUserId(): String {
        val authUid = auth?.currentUser?.uid
        if (!authUid.isNullOrBlank()) {
            return authUid
        }
        val prefs = context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE)
        var devId = prefs.getString("persistent_device_uid", "") ?: ""
        if (devId.isBlank()) {
            devId = "device_" + UUID.randomUUID().toString().replace("-", "").take(12)
            prefs.edit().putString("persistent_device_uid", devId).apply()
        }
        return devId
    }

    /**
     * Pushes all local Room entities and in-memory orders/batches to Firebase Firestore & RTDB.
     */
    fun syncAllToCloud(
        orders: List<RecordedOrder>,
        batches: List<SavedBatchRecord>,
        aliases: Map<String, String>,
        batchCount: Int,
        parsingConfig: AIParsingConfig,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val uid = resolveUserId()
        scope.launch(Dispatchers.IO) {
            _syncStatus.value = CloudSyncStatus.SYNCING
            _syncMessage.value = "Syncing with Firebase Cloud..."
            var firestoreSuccess = false
            var rtdbSuccess = false
            var errorDetails = ""

            val profile = profileDao.getBusinessProfileOnce()
            val products = productDao.getAllProductsSync()
            val recipeIngredients = recipeIngredientDao.getAllRecipeIngredientsSync()
            val masterIngredients = masterIngredientDao.getAllMasterIngredientsSync()

            val timestamp = System.currentTimeMillis()

            // 1. Sync to Firebase Firestore
            val fsTargets = listOfNotNull(firestoreNamed, firestoreDefault)
            for (db in fsTargets) {
                try {
                    val userDoc = db.collection("users").document(uid)
                    val metadata = hashMapOf<String, Any>(
                        "updatedAt" to timestamp,
                        "batchCount" to batchCount,
                        "productAliases" to aliases,
                        "parsingConfig" to mapOf(
                            "productNameRule" to parsingConfig.productNameRule,
                            "quantityRule" to parsingConfig.quantityRule,
                            "priceRule" to parsingConfig.priceRule,
                            "customContext" to parsingConfig.customContext
                        ),
                        "ordersCount" to orders.size,
                        "productsCount" to products.size
                    )
                    userDoc.set(metadata, SetOptions.merge()).await()

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

                    firestoreSuccess = true
                    Log.d(TAG, "Firestore sync SUCCESS on ${db.firestoreSettings.host}")
                    break // Succeeded with primary target
                } catch (e: Exception) {
                    errorDetails += " [Firestore: ${e.localizedMessage}]"
                    Log.w(TAG, "Firestore sync attempt failed: ${e.message}")
                }
            }

            // 2. Sync to Firebase Realtime Database
            val rtdbTargets = listOfNotNull(rtdbNamed, rtdbDefault)
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
                "lastSync" to timestamp
            )

            for (db in rtdbTargets) {
                try {
                    val userRef = db.reference.child("users").child(uid)
                    userRef.setValue(rtdbPayload).await()

                    // Also mirror into a device backup node
                    db.reference.child("device_backups").child(uid).setValue(rtdbPayload).await()

                    rtdbSuccess = true
                    Log.d(TAG, "RTDB sync SUCCESS on ${db.reference}")
                    break
                } catch (e: Exception) {
                    errorDetails += " [RTDB: ${e.localizedMessage}]"
                    Log.w(TAG, "RTDB sync attempt failed: ${e.message}")
                }
            }

            val overallSuccess = firestoreSuccess || rtdbSuccess
            withContext(Dispatchers.Main) {
                if (overallSuccess) {
                    _syncStatus.value = CloudSyncStatus.SUCCESS
                    _lastSyncTimestamp.value = timestamp
                    val channel = if (firestoreSuccess && rtdbSuccess) "Firestore + RTDB" 
                                  else if (firestoreSuccess) "Firestore" else "RTDB"
                    _syncMessage.value = "Synced successfully to $channel"
                    Log.i(TAG, "Cloud sync complete for UID $uid ($channel)")
                    onComplete?.invoke(true)
                } else {
                    _syncStatus.value = CloudSyncStatus.ERROR
                    _syncMessage.value = "Sync error: $errorDetails"
                    Log.e(TAG, "Cloud sync failed for UID $uid: $errorDetails")
                    onComplete?.invoke(false)
                }
            }
        }
    }

    /**
     * Pulls data from Firebase (RTDB or Firestore) and restores into local state.
     */
    suspend fun loadFromCloud(
        onOrdersLoaded: (List<RecordedOrder>) -> Unit,
        onBatchesLoaded: (List<SavedBatchRecord>) -> Unit,
        onAliasesLoaded: (Map<String, String>) -> Unit,
        onBatchCountLoaded: (Int) -> Unit,
        onConfigLoaded: (AIParsingConfig) -> Unit
    ) = withContext(Dispatchers.IO) {
        val uid = resolveUserId()
        var restored = false

        // 1. Try RTDB first
        val rtdbTargets = listOfNotNull(rtdbNamed, rtdbDefault)
        for (db in rtdbTargets) {
            try {
                val snapshot = db.reference.child("users").child(uid).get().await()
                if (snapshot.exists()) {
                    val orders = snapshot.child("orders").children.mapNotNull { it.getValue(RecordedOrder::class.java) }
                    val batches = snapshot.child("batches").children.mapNotNull { it.getValue(SavedBatchRecord::class.java) }
                    val aliases = snapshot.child("productAliases").getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, String>>() {}) ?: emptyMap()
                    val count = snapshot.child("batchCount").getValue(Int::class.java) ?: 0
                    val config = snapshot.child("aiParsingConfig").getValue(AIParsingConfig::class.java) ?: AIParsingConfig()

                    withContext(Dispatchers.Main) {
                        if (orders.isNotEmpty()) onOrdersLoaded(orders)
                        if (batches.isNotEmpty()) onBatchesLoaded(batches)
                        if (aliases.isNotEmpty()) onAliasesLoaded(aliases)
                        onBatchCountLoaded(count)
                        onConfigLoaded(config)
                    }

                    // Restore room profile if empty locally
                    val localProfile = profileDao.getBusinessProfileOnce()
                    if (localProfile == null) {
                        val cloudProfile = snapshot.child("profile").getValue(BusinessProfileEntity::class.java)
                        if (cloudProfile != null) {
                            profileDao.saveBusinessProfile(cloudProfile)
                        }
                    }

                    // Restore master ingredients if empty locally
                    val localIngredients = masterIngredientDao.getAllMasterIngredientsSync()
                    if (localIngredients.isEmpty()) {
                        val cloudIngredients = snapshot.child("masterIngredients").children.mapNotNull { it.getValue(MasterIngredientEntity::class.java) }
                        for (ing in cloudIngredients) {
                            masterIngredientDao.insertMasterIngredient(ing)
                        }
                    }

                    // Restore products if empty locally
                    val localProducts = productDao.getAllProductsSync()
                    if (localProducts.isEmpty()) {
                        val cloudProducts = snapshot.child("products").children.mapNotNull { it.getValue(ProductEntity::class.java) }
                        for (p in cloudProducts) {
                            productDao.insertProduct(p)
                        }
                        val cloudRecipeIngs = snapshot.child("recipeIngredients").children.mapNotNull { it.getValue(RecipeIngredientEntity::class.java) }
                        if (cloudRecipeIngs.isNotEmpty()) {
                            recipeIngredientDao.insertRecipeIngredients(cloudRecipeIngs)
                        }
                    }

                    restored = true
                    Log.d(TAG, "Restored data from RTDB successfully")
                    break
                }
            } catch (e: Exception) {
                Log.w(TAG, "RTDB load attempt failed: ${e.message}")
            }
        }

        // 2. Try Firestore if not restored
        if (!restored) {
            val fsTargets = listOfNotNull(firestoreNamed, firestoreDefault)
            for (db in fsTargets) {
                try {
                    val userDoc = db.collection("users").document(uid)
                    val snapshot = userDoc.get().await()
                    if (snapshot.exists()) {
                        val ordersSnap = userDoc.collection("orders").get().await()
                        val orders = ordersSnap.documents.mapNotNull { it.toObject(RecordedOrder::class.java) }
                        val batchesSnap = userDoc.collection("batches").get().await()
                        val batches = batchesSnap.documents.mapNotNull { it.toObject(SavedBatchRecord::class.java) }

                        val aliases = (snapshot.get("productAliases") as? Map<*, *>)?.entries?.associate { 
                            it.key.toString() to it.value.toString() 
                        } ?: emptyMap()
                        val count = (snapshot.getLong("batchCount") ?: 0L).toInt()

                        withContext(Dispatchers.Main) {
                            if (orders.isNotEmpty()) onOrdersLoaded(orders)
                            if (batches.isNotEmpty()) onBatchesLoaded(batches)
                            if (aliases.isNotEmpty()) onAliasesLoaded(aliases)
                            onBatchCountLoaded(count)
                        }

                        // Restore profile
                        val profileDoc = userDoc.collection("profile").document("current").get().await()
                        val cloudProfile = profileDoc.toObject(BusinessProfileEntity::class.java)
                        if (cloudProfile != null && profileDao.getBusinessProfileOnce() == null) {
                            profileDao.saveBusinessProfile(cloudProfile)
                        }

                        // Restore products
                        val productsSnap = userDoc.collection("products").get().await()
                        val cloudProducts = productsSnap.documents.mapNotNull { it.toObject(ProductEntity::class.java) }
                        if (cloudProducts.isNotEmpty() && productDao.getAllProductsSync().isEmpty()) {
                            for (p in cloudProducts) {
                                productDao.insertProduct(p)
                            }
                            val recipeSnap = userDoc.collection("recipe_ingredients").get().await()
                            val recipeIngs = recipeSnap.documents.mapNotNull { it.toObject(RecipeIngredientEntity::class.java) }
                            if (recipeIngs.isNotEmpty()) {
                                recipeIngredientDao.insertRecipeIngredients(recipeIngs)
                            }
                        }

                        // Restore master ingredients
                        val masterSnap = userDoc.collection("master_ingredients").get().await()
                        val cloudMasterIngs = masterSnap.documents.mapNotNull { it.toObject(MasterIngredientEntity::class.java) }
                        if (cloudMasterIngs.isNotEmpty() && masterIngredientDao.getAllMasterIngredientsSync().isEmpty()) {
                            for (mi in cloudMasterIngs) {
                                masterIngredientDao.insertMasterIngredient(mi)
                            }
                        }

                        restored = true
                        Log.d(TAG, "Restored data from Firestore successfully")
                        break
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore load attempt failed: ${e.message}")
                }
            }
        }
    }
}
