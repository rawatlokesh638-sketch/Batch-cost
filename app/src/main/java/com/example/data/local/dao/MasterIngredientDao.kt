package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MasterIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterIngredientDao {
    @Query("SELECT * FROM master_ingredients ORDER BY name ASC")
    fun getAllMasterIngredients(): Flow<List<MasterIngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterIngredient(ingredient: MasterIngredientEntity): Long

    @Update
    suspend fun updateMasterIngredient(ingredient: MasterIngredientEntity)

    @Delete
    suspend fun deleteMasterIngredient(ingredient: MasterIngredientEntity)

    @Query("SELECT * FROM master_ingredients ORDER BY name ASC")
    suspend fun getAllMasterIngredientsSync(): List<MasterIngredientEntity>

    @Query("DELETE FROM master_ingredients")
    suspend fun clearMasterIngredients()
}
