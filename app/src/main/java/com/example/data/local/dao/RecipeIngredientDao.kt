package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE productId = :productId")
    fun getIngredientsForProduct(productId: Long): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE productId = :productId")
    suspend fun getIngredientsForProductOnce(productId: Long): List<RecipeIngredientEntity>

    @Query("SELECT * FROM recipe_ingredients")
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE productId = :productId")
    suspend fun deleteIngredientsForProduct(productId: Long)

    @Query("SELECT * FROM recipe_ingredients")
    suspend fun getAllRecipeIngredientsSync(): List<RecipeIngredientEntity>

    @Query("DELETE FROM recipe_ingredients")
    suspend fun clearRecipeIngredients()
}
