package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orderNumber: String = "ORD-${System.currentTimeMillis() % 10000}",
    val customerName: String = "Customer",
    val customerPhone: String = "",
    val productName: String,
    val quantity: Double = 1.0,
    val unit: String = "kg",
    val totalRevenue: Double = 0.0,
    val calculatedCost: Double = 0.0,
    val status: String = "Pending", // Pending, Baking, Ready, Delivered, Cancelled
    val deliveryDate: String = "",
    val isEggless: Boolean = true,
    val notes: String = "",
    val source: String = "WhatsApp Auto-Capture",
    val createdAt: Long = System.currentTimeMillis()
)
