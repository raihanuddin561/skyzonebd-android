package com.skyzonebd.android.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.Order
import com.skyzonebd.android.data.model.OrdersResponse
import com.skyzonebd.android.data.repository.OrderRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    private val _orders = MutableStateFlow<Resource<OrdersResponse>?>(null)
    val orders: StateFlow<Resource<OrdersResponse>?> = _orders.asStateFlow()
    
    private val _selectedOrder = MutableStateFlow<Resource<Order>?>(null)
    val selectedOrder: StateFlow<Resource<Order>?> = _selectedOrder.asStateFlow()
    
    init {
        loadOrders()
    }
    
    fun loadOrders(page: Int = 1) {
        viewModelScope.launch {
            orderRepository.getOrders(page).collect { resource ->
                _orders.value = resource
            }
        }
    }
    
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrder(orderId).collect { resource ->
                _selectedOrder.value = resource
            }
        }
    }
    
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.cancelOrder(orderId).collect { resource ->
                if (resource is Resource.Success) {
                    loadOrders()
                }
            }
        }
    }
    
    fun refresh() {
        loadOrders()
    }
}
