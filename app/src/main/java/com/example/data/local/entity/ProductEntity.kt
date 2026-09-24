package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val sellingPrice: Double = 0.0,
    val unit: String = "Piece",
    val sku: String? = null,
    val category: String? = null,
    val description: String? = null,
    val weight: String? = null,
    val imageUri: String? = null,
    val packagingCost: Double = 0.0,
    val labourCost: Double = 0.0,
    val electricityGasCost: Double = 0.0,
    val otherOverhead: Double = 0.0,
    val wastagePercent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
