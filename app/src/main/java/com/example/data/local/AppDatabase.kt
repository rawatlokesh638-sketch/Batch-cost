package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.MasterIngredientDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.RecipeIngredientDao
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.MasterIngredientEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        BusinessProfileEntity::class,
        ProductEntity::class,
        MasterIngredientEntity::class,
        RecipeIngredientEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun productDao(): ProductDao
    abstract fun masterIngredientDao(): MasterIngredientDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "batchcost_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
