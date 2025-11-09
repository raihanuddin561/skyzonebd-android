package com.skyzonebd.android.ui.checkout

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
    
    fun setShippingAddress(address: Address) {
        _shippingAddress.value = address
    }
    
    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }
    
    fun placeOrder(
        items: List<CartItem>,
        totalAmount: Double,
        note: String? = null
    ) {
        val address = _shippingAddress.value
        if (address == null) {
            _orderState.value = Resource.Error("Please select a shipping address")
            return
        }
        
        viewModelScope.launch {
            val orderItems = items.map { cartItem ->
                CreateOrderItem(
                    productId = cartItem.productId,
                    quantity = cartItem.quantity,
                    price = cartItem.price
                )
            }
            
            // Convert address to JSON string or address ID
            val addressStr = "${address.street}, ${address.city}, ${address.state} ${address.postalCode}"
            
            val request = CreateOrderRequest(
                items = orderItems,
                shippingAddress = addressStr,
                billingAddress = addressStr,
                paymentMethod = _paymentMethod.value.name,
                notes = note
            )
            
            orderRepository.createOrder(request).collect { resource ->
                _orderState.value = resource
            }
        }
    }
    
    fun resetOrderState() {
        _orderState.value = null
    }
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    MOBILE_BANKING,
    CREDIT_CARD
}
