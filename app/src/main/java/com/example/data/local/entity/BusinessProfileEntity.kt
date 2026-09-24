package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String? = null,
    val businessType: String = "Bakery",
    val customBusinessType: String? = null,
    val currencySymbol: String = "₹",
    val currencyCode: String = "INR",
    val logoIdentifier: String = "bakery",
    val address: String? = null,
    val isOnboardingCompleted: Boolean = false
)
