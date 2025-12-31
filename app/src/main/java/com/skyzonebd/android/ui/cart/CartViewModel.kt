package com.skyzonebd.android.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.local.CartPreferences
import com.skyzonebd.android.data.model.Cart
import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartPreferences: CartPreferences
) : ViewModel() {
    
    companion object {
        private const val TAG = "CartViewModel"
    }
    
    // Directly observe DataStore as StateFlow - always active
    // Using Eagerly ensures the Flow is always collecting even when screens change
    val cartItems: StateFlow<List<CartItem>> = cartPreferences.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()
    
    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()
    
    init {
        Log.d(TAG, "CartViewModel initialized")
        // Observe cart items changes and update totals
        viewModelScope.launch {
            cartItems.collect { items ->
                Log.d(TAG, "Cart items changed: ${items.size} items")
                calculateTotals(items)
            }
        }
    }
    
    private suspend fun saveCartToPreferences(items: List<CartItem>) {
        Log.d(TAG, "Saving ${items.size} items to preferences")
        cartPreferences.saveCartItems(items)
    }
    
    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== ADD TO CART START ===")
                Log.d(TAG, "addToCart - Product: ${product.name}, Quantity: $quantity")
                Log.d(TAG, "Current cart items before add: ${cartItems.value.size}")
                
                val existingItems = cartItems.value.toMutableList()
                val existingItemIndex = existingItems.indexOfFirst { it.productId == product.id }
                
                if (existingItemIndex >= 0) {
                    // Update quantity of existing item
                    val existingItem = existingItems[existingItemIndex]
                    val newQuantity = existingItem.quantity + quantity
                    Log.d(TAG, "Updating existing item quantity: ${existingItem.quantity} -> $newQuantity")
                    existingItems[existingItemIndex] = existingItem.copy(
                        quantity = newQuantity,
                        total = product.retailPrice * newQuantity
                    )
                } else {
                    // Add new item
                    Log.d(TAG, "Adding new item to cart")
                    val newItem = CartItem(
                        id = "${product.id}_${System.currentTimeMillis()}",
                        productId = product.id,
                        product = product,
                        quantity = quantity,
                        price = product.retailPrice,
                        total = product.retailPrice * quantity
                    )
                    existingItems.add(newItem)
                    Log.d(TAG, "New item created with ID: ${newItem.id}")
                }
                
                Log.d(TAG, "About to save ${existingItems.size} items")
                saveCartToPreferences(existingItems)
                Log.d(TAG, "Save completed")
                
                // Wait for DataStore to propagate changes
                kotlinx.coroutines.delay(200)
                Log.d(TAG, "After save, cartItems.value has: ${cartItems.value.size} items")
                Log.d(TAG, "Item count: ${_itemCount.value}")
                Log.d(TAG, "=== ADD TO CART END ===")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to cart", e)
                e.printStackTrace()
            }
        }
    }
    
    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            val updatedItems = cartItems.value.filter { it.id != cartItemId }
            saveCartToPreferences(updatedItems)
        }
    }
    
    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        viewModelScope.launch {
            val updatedItems = cartItems.value.map { item ->
                if (item.id == cartItemId) {
                    val moq = item.product.moq ?: 1
                    val validQuantity = newQuantity.coerceAtLeast(moq)
                    item.copy(
                        quantity = validQuantity,
                        total = item.price * validQuantity
                    )
                } else {
                    item
                }
            }
            saveCartToPreferences(updatedItems)
        }
    }
    
    fun incrementQuantity(cartItemId: String) {
        viewModelScope.launch {
            val currentItems = cartItems.value.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.id == cartItemId }
            
            if (itemIndex >= 0) {
                val item = currentItems[itemIndex]
                val newQuantity = item.quantity + 1
                
                if (newQuantity <= item.product.stock) {
                    currentItems[itemIndex] = item.copy(
                        quantity = newQuantity,
                        total = item.price * newQuantity
                    )
                    saveCartToPreferences(currentItems)
                }
            }
        }
    }
    
    fun decrementQuantity(cartItemId: String) {
        viewModelScope.launch {
            val currentItems = cartItems.value.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.id == cartItemId }
            
            if (itemIndex >= 0) {
                val item = currentItems[itemIndex]
                val moq = item.product.moq ?: 1
                val newQuantity = item.quantity - 1
                
                if (newQuantity >= moq) {
                    currentItems[itemIndex] = item.copy(
                        quantity = newQuantity,
                        total = item.price * newQuantity
                    )
                    saveCartToPreferences(currentItems)
                }
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartPreferences.clearCart()
        }
    }
    
    private fun calculateTotals(items: List<CartItem>) {
        _totalAmount.value = items.sumOf { it.total }
        _itemCount.value = items.size
        Log.d(TAG, "Totals calculated - Items: ${items.size}, Amount: ${_totalAmount.value}")
    }
    
    fun updatePricesForUserType(userType: UserType) {
        viewModelScope.launch {
            val updatedItems = cartItems.value.map { item ->
                val newPrice = when (userType) {
                    UserType.RETAIL -> item.product.retailPrice
                    UserType.WHOLESALE -> item.product.wholesalePrice ?: item.product.retailPrice
                    UserType.GUEST -> item.product.retailPrice
                }
                item.copy(
                    price = newPrice,
                    total = newPrice * item.quantity
                )
            }
            saveCartToPreferences(updatedItems)
        }
    }
    
    fun getTotalSavings(): Double {
        return cartItems.value.sumOf { item ->
            (item.product.retailPrice - item.price) * item.quantity
        }
    }
}
