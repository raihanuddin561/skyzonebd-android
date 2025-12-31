package com.skyzonebd.android.util

import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.data.model.OrderItem
import com.skyzonebd.android.data.model.Product

/**
 * Extension functions for price formatting with unit support
 * Added: December 25, 2025 - Unit Management System
 */

/**
 * Format price with unit display
 * Example: 500.0.formatPriceWithUnit("kg") -> "৳500/kg"
 * Example: 500.0.formatPriceWithUnit("") -> "৳500"
 */
fun Double.formatPriceWithUnit(unit: String): String {
    return if (unit.isNotEmpty()) "৳${this.formatWithComma()}/$unit" else "৳${this.formatWithComma()}"
}

/**
 * Format number with commas for better readability
 * Example: 1000.0.formatWithComma() -> "1,000"
 */
fun Double.formatWithComma(): String {
    return String.format("%,.0f", this)
}

/**
 * Format number with 2 decimal places and commas
 * Example: 1234.56.formatWithCommaDecimal() -> "1,234.56"
 */
fun Double.formatWithCommaDecimal(): String {
    return String.format("%,.2f", this)
}

/**
 * Get display string for cart item with unit calculation
 * Example: "৳500/kg × 10kg = ৳5,000" or "Qty: 10 × ৳500 = ৳5,000" if no unit
 */
fun CartItem.getDisplayString(): String {
    val unitPrice = product.getDisplayPrice(
        userType = com.skyzonebd.android.data.model.UserType.RETAIL,
        quantity = quantity
    )
    val total = unitPrice * quantity
    return if (product.displayUnit.isNotEmpty()) {
        "৳${unitPrice.formatWithComma()}/${product.displayUnit} × $quantity${product.displayUnit} = ৳${total.formatWithComma()}"
    } else {
        "Qty: $quantity × ৳${unitPrice.formatWithComma()} = ৳${total.formatWithComma()}"
    }
}

/**
 * Get display string for order item with unit calculation
 * Example: "৳500/kg × 10kg"
 */
fun OrderItem.getDisplayString(): String {
    return "৳${price.formatWithComma()}/$unit × $quantity$unit"
}

/**
 * Get formatted price with unit for Product
 * Takes into account retail/sale price
 */
fun Product.getFormattedPrice(): String {
    val displayPrice = salePrice ?: retailPrice
    return displayPrice.formatPriceWithUnit(displayUnit)
}

/**
 * Get formatted price with unit and user type consideration
 */
fun Product.getFormattedPriceForUser(userType: com.skyzonebd.android.data.model.UserType, quantity: Int = 1): String {
    val displayPrice = getDisplayPrice(userType, quantity)
    return displayPrice.formatPriceWithUnit(displayUnit)
}

/**
 * Get short unit display (for compact views)
 * Example: "piece" -> "pc", "kilogram" -> "kg"
 */
fun String.toShortUnit(): String {
    return when (this.lowercase()) {
        "piece", "pieces" -> "pc"
        "kilogram", "kilograms" -> "kg"
        "gram", "grams" -> "g"
        "liter", "liters", "litre", "litres" -> "L"
        "milliliter", "milliliters", "millilitre", "millilitres" -> "mL"
        "box", "boxes" -> "box"
        "packet", "packets" -> "pkt"
        "dozen" -> "dz"
        else -> this
    }
}
