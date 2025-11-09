package com.skyzonebd.android.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.Cart
import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    // We'll add CartRepository later when implementing backend cart sync
) : ViewModel() {
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()
    
    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()
    
    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            val existingItems = _cartItems.value.toMutableList()
            val existingItemIndex = existingItems.indexOfFirst { it.productId == product.id }
            
            if (existingItemIndex >= 0) {
                // Update quantity of existing item
                val existingItem = existingItems[existingItemIndex]
                existingItems[existingItemIndex] = existingItem.copy(
                    quantity = existingItem.quantity + quantity
                )
            } else {
                // Add new item
                val newItem = CartItem(
                    id = "${product.id}_${System.currentTimeMillis()}",
                    productId = product.id,
                    product = product,
                    quantity = quantity,
                    price = product.retailPrice // Will be adjusted based on user type
                )
                existingItems.add(newItem)
            }
            
            _cartItems.value = existingItems
            calculateTotals()
        }
    }
    
    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            _cartItems.value = _cartItems.value.filter { it.id != cartItemId }
            calculateTotals()
        }
    }
    
    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItems = _cartItems.value.map { item ->
                if (item.id == cartItemId) {
                    val moq = item.product.moq ?: 1
                    val validQuantity = newQuantity.coerceAtLeast(moq)
                    item.copy(quantity = validQuantity)
                } else {
                    item
                }
            }
            _cartItems.value = updatedItems
            calculateTotals()
        }
    }
    
    fun incrementQuantity(cartItemId: String) {
        viewModelScope.launch {
            val updatedItems = _cartItems.value.map { item ->
                if (item.id == cartItemId) {
                    val step = item.product.moq ?: 1
                    item.copy(quantity = item.quantity + step)
                } else {
                    item
                }
            }
            _cartItems.value = updatedItems
            calculateTotals()
        }
    }
    
    fun decrementQuantity(cartItemId: String) {
        viewModelScope.launch {
            val updatedItems = _cartItems.value.map { item ->
                if (item.id == cartItemId) {
                    val step = item.product.moq ?: 1
                    val newQuantity = item.quantity - step
                    val moq = item.product.moq ?: 1
                    if (newQuantity >= moq) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                } else {
                    item
                }
            }
            _cartItems.value = updatedItems
            calculateTotals()
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            _cartItems.value = emptyList()
            calculateTotals()
        }
    }
    
    private fun calculateTotals() {
        var total = 0.0
        var count = 0
        
        _cartItems.value.forEach { item ->
            total += item.price * item.quantity
            count += item.quantity
        }
        
        _totalAmount.value = total
        _itemCount.value = count
    }
    
    fun updatePricesForUserType(userType: UserType) {
        viewModelScope.launch {
            val updatedItems = _cartItems.value.map { item ->
                val newPrice = item.product.getDisplayPrice(userType)
                item.copy(price = newPrice)
            }
            _cartItems.value = updatedItems
            calculateTotals()
        }
    }
    
    fun getTotalSavings(userType: UserType): Double {
        if (userType != UserType.WHOLESALE) return 0.0
        
        var savings = 0.0
        _cartItems.value.forEach { item ->
            val retailPrice = item.product.retailPrice
            val wholesalePrice = item.product.wholesalePrice ?: retailPrice
            savings += (retailPrice - wholesalePrice) * item.quantity
        }
        return savings
    }
}
