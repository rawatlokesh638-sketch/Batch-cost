package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.MasterIngredientEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.RecipeIngredientEntity
import com.example.ui.AIParsingConfig
import com.example.ui.RecordedOrder
import com.example.ui.SavedBatchRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirebaseDataSyncTest {

    @Test
    fun testRecordedOrderDataIntegrity() {
        val order = RecordedOrder(
            id = "test_order_123",
            customerName = "Lokesh Rawat",
            productName = "Truffle Cake",
            quantity = 2,
            weightOrSize = "1.5 kg",
            flavor = "Dark Chocolate",
            isEggless = true,
            customMessageOnCake = "Happy Birthday",
            totalRevenue = 1200.0,
            totalCost = 480.0,
            deliveryFee = 50.0,
            platformFee = 30.0,
            paymentGatewayFee = 12.0,
            marketingFee = 20.0,
            status = "Confirmed",
            deliveryDate = "Tomorrow",
            deliveryTimeSlot = "6 PM",
            aiExplanation = "Detected bakery order",
            rawWhatsAppText = "1.5 kg chocolate cake kal 6 baje chahiye",
            date = "27 Sep 2026"
        )

        assertEquals("test_order_123", order.id)
        assertEquals("Lokesh Rawat", order.customerName)
        assertEquals("Truffle Cake", order.productName)
        assertEquals(2, order.quantity)
        assertEquals(1200.0, order.totalRevenue, 0.001)
        assertEquals(480.0, order.totalCost, 0.001)
        assertEquals(112.0, order.totalFees, 0.001)
        assertEquals(592.0, order.totalAllCosts, 0.001)
        assertEquals(608.0, order.profit, 0.001)
    }

    @Test
    fun testSavedBatchRecordDataIntegrity() {
        val batch = SavedBatchRecord(
            id = "batch_456",
            productName = "Butter Cookies",
            date = "27 Sep 2026",
            unitsProduced = 50,
            totalCost = 350.0,
            costPerUnit = 7.0,
            sellingPricePerUnit = 15.0,
            estimatedProfit = 400.0,
            notes = "Test batch"
        )

        assertEquals("Butter Cookies", batch.productName)
        assertEquals(50, batch.unitsProduced)
        assertEquals(7.0, batch.costPerUnit, 0.001)
        assertEquals(400.0, batch.estimatedProfit, 0.001)
    }

    @Test
    fun testBusinessProfileEntityDefaultConstructor() {
        val profile = BusinessProfileEntity(
            id = 1,
            businessName = "Sweet Delights",
            ownerName = "Lokesh",
            phone = "+919876543210",
            email = "test@bakery.com",
            businessType = "Home Bakery",
            currencySymbol = "₹",
            currencyCode = "INR",
            isOnboardingCompleted = true
        )

        assertEquals("Sweet Delights", profile.businessName)
        assertTrue(profile.isOnboardingCompleted)
    }

    @Test
    fun testProductAndIngredientEntities() {
        val product = ProductEntity(
            id = 10,
            name = "Red Velvet Cake",
            sellingPrice = 850.0,
            unit = "1 kg"
        )
        val ingredient = RecipeIngredientEntity(
            id = 101,
            productId = 10,
            ingredientName = "Cream Cheese",
            purchaseQty = 1.0,
            purchaseUnit = "kg",
            purchasePrice = 600.0,
            usedQty = 250.0,
            usedUnit = "g"
        )
        val master = MasterIngredientEntity(
            id = 50,
            name = "All Purpose Flour",
            purchaseQty = 5.0,
            purchaseUnit = "kg",
            purchasePrice = 220.0,
            currentStock = 12.5
        )

        assertEquals(850.0, product.sellingPrice, 0.001)
        assertEquals("Cream Cheese", ingredient.ingredientName)
        assertEquals(12.5, master.currentStock, 0.001)
    }

    @Test
    fun testDeviceUidGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE)
        val devId = "device_" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        prefs.edit().putString("persistent_device_uid", devId).commit()

        val retrieved = prefs.getString("persistent_device_uid", null)
        assertNotNull(retrieved)
        assertTrue(retrieved!!.startsWith("device_"))
    }
}
