package com.example.util

import java.util.Locale

object UnitConverter {

    enum class UnitCategory {
        MASS, VOLUME, COUNT, OTHER
    }

    private fun normalizeUnitString(unit: String): String {
        return unit.trim().lowercase(Locale.ROOT)
    }

    fun getUnitCategory(unit: String): UnitCategory {
        val norm = normalizeUnitString(unit)
        return when {
            norm in listOf("kg", "kilogram", "kilograms", "g", "gram", "grams", "mg", "milligram", "milligrams", "lb", "lbs", "pound", "pounds", "oz", "ounce", "ounces") -> UnitCategory.MASS
            norm in listOf("l", "liter", "liters", "litre", "litres", "ml", "milliliter", "milliliters", "tsp", "teaspoon", "teaspoons", "tbsp", "tablespoon", "tablespoons", "cup", "cups", "fl oz") -> UnitCategory.VOLUME
            norm in listOf("pc", "piece", "pieces", "unit", "units", "item", "items", "doz", "dozen", "pack", "box", "packet", "packets") -> UnitCategory.COUNT
            else -> UnitCategory.OTHER
        }
    }

    /**
     * Converts a quantity from given unit to the base unit of its category.
     * Base units:
     * - MASS: grams (g)
     * - VOLUME: milliliters (ml)
     * - COUNT: pieces (pc)
     */
    fun toBaseUnitValue(qty: Double, unit: String): Double {
        val norm = normalizeUnitString(unit)
        return when (norm) {
            // Mass -> Base: Grams
            "kg", "kilogram", "kilograms" -> qty * 1000.0
            "g", "gram", "grams" -> qty
            "mg", "milligram", "milligrams" -> qty / 1000.0
            "lb", "lbs", "pound", "pounds" -> qty * 453.59237
            "oz", "ounce", "ounces" -> qty * 28.34952

            // Volume -> Base: Milliliters
            "l", "liter", "liters", "litre", "litres" -> qty * 1000.0
            "ml", "milliliter", "milliliters" -> qty
            "tsp", "teaspoon", "teaspoons" -> qty * 5.0
            "tbsp", "tablespoon", "tablespoons" -> qty * 15.0
            "cup", "cups" -> qty * 240.0
            "fl oz" -> qty * 29.5735

            // Count -> Base: Pieces
            "pc", "piece", "pieces", "unit", "units", "item", "items", "pack", "box", "packet", "packets" -> qty
            "doz", "dozen" -> qty * 12.0

            else -> qty
        }
    }

    /**
     * Calculates the calculated cost for the used quantity based on purchase quantity & price.
     */
    fun calculateCost(
        purchaseQty: Double,
        purchaseUnit: String,
        purchasePrice: Double,
        usedQty: Double,
        usedUnit: String
    ): Double {
        if (purchaseQty <= 0.0 || purchasePrice <= 0.0 || usedQty <= 0.0) return 0.0

        val purchaseCategory = getUnitCategory(purchaseUnit)
        val usedCategory = getUnitCategory(usedUnit)

        return if (purchaseCategory != UnitCategory.OTHER && purchaseCategory == usedCategory) {
            val purchaseInBase = toBaseUnitValue(purchaseQty, purchaseUnit)
            val usedInBase = toBaseUnitValue(usedQty, usedUnit)
            if (purchaseInBase > 0.0) {
                (usedInBase / purchaseInBase) * purchasePrice
            } else 0.0
        } else {
            // Direct ratio if units match or non-standard
            (usedQty / purchaseQty) * purchasePrice
        }
    }

    val COMMON_UNITS = listOf(
        "g", "kg", "mg", "ml", "L", "piece", "dozen", "packet", "box", "cup", "tbsp", "tsp", "jar", "slice"
    )

    /**
     * Returns a clear, human-readable transparent math string for recipe ingredient costing.
     * E.g. "250 g × ₹80.00 / 1 kg = ₹20.00"
     */
    fun formatCalculationSteps(
        purchaseQty: Double,
        purchaseUnit: String,
        purchasePrice: Double,
        usedQty: Double,
        usedUnit: String,
        currencySymbol: String
    ): String {
        val cost = calculateCost(purchaseQty, purchaseUnit, purchasePrice, usedQty, usedUnit)
        val df = java.text.DecimalFormat("#,##0.##")
        val costStr = CurrencyFormatter.format(cost, currencySymbol)
        val pPriceStr = CurrencyFormatter.format(purchasePrice, currencySymbol)
        val pQtyStr = df.format(purchaseQty)
        val uQtyStr = df.format(usedQty)

        return "$uQtyStr $usedUnit × $pPriceStr / $pQtyStr $purchaseUnit = $costStr"
    }
}
