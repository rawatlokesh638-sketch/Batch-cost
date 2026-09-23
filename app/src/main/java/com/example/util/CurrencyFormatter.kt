package com.example.util

import java.text.DecimalFormat

object CurrencyFormatter {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val compactFormat = DecimalFormat("#,##0")

    fun format(amount: Double, symbol: String = "₹"): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return "$symbol${decimalFormat.format(safeAmount)}"
    }

    fun formatCompact(amount: Double, symbol: String = "₹"): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return "$symbol${compactFormat.format(safeAmount)}"
    }

    fun formatPercent(value: Double): String {
        val safeValue = if (value.isNaN() || value.isInfinite()) 0.0 else value
        return String.format("%.1f%%", safeValue)
    }

    fun formatDoubleRaw(amount: Double): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return decimalFormat.format(safeAmount)
    }

    val SUPPORTED_CURRENCIES = listOf(
        CurrencyOption("INR", "₹", "Indian Rupee"),
        CurrencyOption("USD", "$", "US Dollar"),
        CurrencyOption("EUR", "€", "Euro"),
        CurrencyOption("GBP", "£", "British Pound"),
        CurrencyOption("AED", "AED ", "UAE Dirham"),
        CurrencyOption("PKR", "Rs ", "Pakistani Rupee"),
        CurrencyOption("BDT", "৳", "Bangladeshi Taka"),
        CurrencyOption("CAD", "C$", "Canadian Dollar"),
        CurrencyOption("AUD", "A$", "Australian Dollar")
    )
}

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)
