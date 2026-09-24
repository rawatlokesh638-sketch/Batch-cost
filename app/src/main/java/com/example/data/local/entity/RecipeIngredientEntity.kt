package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long = 0L,
    val ingredientName: String = "",
    val purchaseQty: Double = 0.0,
    val purchaseUnit: String = "kg",
    val purchasePrice: Double = 0.0,
    val usedQty: Double = 0.0,
    val usedUnit: String = "g"
)
