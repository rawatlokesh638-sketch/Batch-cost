package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_ingredients")
data class MasterIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchaseQty: Double,
    val purchaseUnit: String,
    val purchasePrice: Double,
    val category: String? = "General",
    val supplier: String? = null,
    val minimumStock: Double = 0.0,
    val currentStock: Double = 0.0,
    val expiryDate: String? = null,
    val priceHistoryJson: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
