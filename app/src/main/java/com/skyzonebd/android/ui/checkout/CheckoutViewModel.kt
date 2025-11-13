package com.skyzonebd.android.ui.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.*
import com.skyzonebd.android.data.repository.OrderRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    private val _shippingAddress = MutableStateFlow<Address?>(null)
    val shippingAddress: StateFlow<Address?> = _shippingAddress.asStateFlow()
    
    private val _paymentMethod = MutableStateFlow<PaymentMethod>(PaymentMethod.CASH_ON_DELIVERY)
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()
    
    private val _orderState = MutableStateFlow<Resource<Order>?>(null)
    val orderState: StateFlow<Resource<Order>?> = _orderState.asStateFlow()
    
    private val _lastOrder = MutableStateFlow<Order?>(null)
    val lastOrder: StateFlow<Order?> = _lastOrder.asStateFlow()
    
    fun setShippingAddress(address: Address) {
        _shippingAddress.value = address
    }
    
    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }
    
    fun setLastOrder(order: Order) {
        _lastOrder.value = order
    }
    
    fun clearLastOrder() {
        _lastOrder.value = null
    }
    
    fun placeOrder(
        items: List<CartItem>,
        totalAmount: Double,
        note: String? = null,
        shippingAddress: String,
        billingAddress: String
    ) {
        // Addresses are already validated and filled in CheckoutScreen
        if (shippingAddress.isBlank() || billingAddress.isBlank()) {
            _orderState.value = Resource.Error("Please provide at least one address")
            return
        }
        
        // Set loading state immediately
        _orderState.value = Resource.Loading()
        
        viewModelScope.launch {
            val orderItems = items.map { cartItem ->
                CreateOrderItem(
                    productId = cartItem.productId,
                    quantity = cartItem.quantity,
                    price = cartItem.price,
                    name = cartItem.product.name
                )
            }
            
            val request = CreateOrderRequest(
                items = orderItems,
                shippingAddress = shippingAddress,
                billingAddress = billingAddress,
                paymentMethod = _paymentMethod.value.name.lowercase(),
                notes = note
            )
            
            try {
                orderRepository.createOrder(request).collect { resource ->
                    _orderState.value = resource
                }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Error placing order: ${e.message}", e)
                _orderState.value = Resource.Error(e.message ?: "Failed to place order")
            }
        }
    }
    
    fun placeGuestOrder(
        items: List<CartItem>,
        totalAmount: Double,
        note: String? = null,
        guestName: String,
        guestEmail: String,
        guestMobile: String,
        guestCompany: String? = null,
        shippingAddress: String,
        billingAddress: String
    ) {
        // Addresses are already validated and filled in CheckoutScreen
        if (shippingAddress.isBlank() || billingAddress.isBlank()) {
            _orderState.value = Resource.Error("Please provide at least one address")
            return
        }
        
        if (guestName.isBlank() || guestMobile.isBlank()) {
            _orderState.value = Resource.Error("Please fill in name and mobile number")
            return
        }
        
        // Set loading state immediately
        _orderState.value = Resource.Loading()
        
        viewModelScope.launch {
            val orderItems = items.map { cartItem ->
                CreateOrderItem(
                    productId = cartItem.productId,
                    quantity = cartItem.quantity,
                    price = cartItem.price,
                    name = cartItem.product.name
                )
            }
            
            // Create guestInfo object to match web API structure
            val guestInfo = GuestInfo(
                name = guestName,
                mobile = guestMobile,
                email = guestEmail.takeIf { it.isNotBlank() },
                companyName = guestCompany?.takeIf { it.isNotBlank() }
            )
            
            val request = CreateOrderRequest(
                items = orderItems,
                shippingAddress = shippingAddress,
                billingAddress = billingAddress,
                paymentMethod = _paymentMethod.value.name.lowercase(),
                notes = note,
                guestInfo = guestInfo
            )
            
            try {
                orderRepository.createOrder(request).collect { resource ->
                    _orderState.value = resource
                }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Error placing guest order: ${e.message}", e)
                _orderState.value = Resource.Error(e.message ?: "Failed to place order")
            }
        }
    }
    
    fun resetOrderState() {
        _orderState.value = null
    }
}
