package com.skyzonebd.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Cart item for local storage and API
 */
data class CartItem(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("product")
    val product: Product,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("total")
    val total: Double = quantity * price
) {
    fun canAddMore(userType: UserType): Boolean {
        val moq = if (userType == UserType.WHOLESALE && product.wholesaleEnabled) {
            product.wholesaleMOQ
        } else {
            product.retailMOQ
        }
        
        return quantity + 1 <= product.stockQuantity
    }
    
    fun meetsMinimumQuantity(userType: UserType): Boolean {
        val moq = if (userType == UserType.WHOLESALE && product.wholesaleEnabled) {
            product.wholesaleMOQ
        } else {
            product.retailMOQ
        }
        
        return quantity >= moq
    }
}

data class Cart(
    val items: List<CartItem>,
    val subtotal: Double,
    val tax: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = subtotal + tax + shipping
) {
    companion object {
        fun empty() = Cart(
            items = emptyList(),
            subtotal = 0.0
        )
    }
}

data class Address(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("userId")
    val userId: String = "",
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("type")
    val type: AddressType = AddressType.SHIPPING,
    
    @SerializedName("street")
    val street: String,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String = "",
    
    @SerializedName("postalCode")
    val postalCode: String = "",
    
    @SerializedName("country")
    val country: String = "Bangladesh",
    
    @SerializedName("isDefault")
    val isDefault: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun toDisplayString(): String {
        return buildString {
            append(street)
            append(", ")
            append(city)
            if (!state.isNullOrEmpty()) {
                append(", ")
                append(state)
            }
            if (!postalCode.isNullOrEmpty()) {
                append(" ")
                append(postalCode)
            }
            append(", ")
            append(country)
        }
    }
}

enum class AddressType {
    @SerializedName("SHIPPING")
    SHIPPING,
    
    @SerializedName("BILLING")
    BILLING
}
