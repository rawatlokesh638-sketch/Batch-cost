package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val ingredientName: String,
    val purchaseQty: Double,
    val purchaseUnit: String,
    val purchasePrice: Double,
    val usedQty: Double,
    val usedUnit: String
)
