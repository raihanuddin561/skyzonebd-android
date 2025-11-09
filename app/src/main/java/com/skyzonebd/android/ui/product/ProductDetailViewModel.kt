package com.skyzonebd.android.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.repository.ProductRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _product = MutableStateFlow<Resource<Product>?>(null)
    val product: StateFlow<Resource<Product>?> = _product.asStateFlow()
    
    private val _relatedProducts = MutableStateFlow<List<Product>>(emptyList())
    val relatedProducts: StateFlow<List<Product>> = _relatedProducts.asStateFlow()
    
    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()
    
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                _product.value = resource
                
                // Load related products if product loaded successfully
                if (resource is Resource.Success && resource.data != null) {
                    loadRelatedProducts(resource.data.categoryId)
                }
            }
        }
    }
    
    private fun loadRelatedProducts(categoryId: String?) {
        if (categoryId == null) return
        
        viewModelScope.launch {
            productRepository.getProducts(
                categoryId = categoryId,
                limit = 6
            ).collect { resource ->
                if (resource is Resource.Success) {
                    _relatedProducts.value = resource.data?.products ?: emptyList()
                }
            }
        }
    }
    
    fun incrementQuantity(moq: Int?) {
        val step = moq ?: 1
        _quantity.value += step
    }
    
    fun decrementQuantity(moq: Int?) {
        val step = moq ?: 1
        val newQuantity = _quantity.value - step
        if (newQuantity >= (moq ?: 1)) {
            _quantity.value = newQuantity
        }
    }
    
    fun setQuantity(value: Int, moq: Int?) {
        val minQty = moq ?: 1
        _quantity.value = value.coerceAtLeast(minQty)
    }
    
    fun validateQuantity(moq: Int?): Boolean {
        val minQty = moq ?: 1
        return _quantity.value >= minQty
    }
}
