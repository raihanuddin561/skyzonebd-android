package com.skyzonebd.android.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.HeroSlide
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.ProductsResponse
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.data.repository.ProductRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val apiService: ApiService
) : ViewModel() {
    
    private val TAG = "HomeViewModel"
    
    private val _heroSlides = MutableStateFlow<Resource<List<HeroSlide>>?>(null)
    val heroSlides: StateFlow<Resource<List<HeroSlide>>?> = _heroSlides.asStateFlow()
    
    private val _featuredProducts = MutableStateFlow<Resource<ProductsResponse>?>(null)
    val featuredProducts: StateFlow<Resource<ProductsResponse>?> = _featuredProducts.asStateFlow()
    
    private val _allProducts = MutableStateFlow<Resource<ProductsResponse>?>(null)
    val allProducts: StateFlow<Resource<ProductsResponse>?> = _allProducts.asStateFlow()
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadHeroSlides()
        loadFeaturedProducts()
        loadAllProducts()
    }
    
    fun loadHeroSlides() {
        Log.d(TAG, "loadHeroSlides - Starting")
        viewModelScope.launch {
            try {
                _heroSlides.value = Resource.Loading()
                val response = apiService.getHeroSlides()
                Log.d(TAG, "loadHeroSlides - Response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d(TAG, "loadHeroSlides - Body: success=${apiResponse?.success}, data size=${apiResponse?.data?.size}")
                    
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        Log.d(TAG, "loadHeroSlides - Success! Slides: ${apiResponse.data.size}")
                        _heroSlides.value = Resource.Success(apiResponse.data)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Failed to load hero slides"
                        Log.e(TAG, "loadHeroSlides - Error: $errorMsg")
                        _heroSlides.value = Resource.Error(errorMsg)
                    }
                } else {
                    val errorMsg = "Failed to load hero slides: ${response.code()}"
                    Log.e(TAG, "loadHeroSlides - HTTP Error: $errorMsg")
                    _heroSlides.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Network error"
                Log.e(TAG, "loadHeroSlides - Exception: $errorMsg", e)
                _heroSlides.value = Resource.Error(errorMsg)
            }
        }
    }
    
    fun loadFeaturedProducts() {
        Log.d(TAG, "loadFeaturedProducts - Starting")
        viewModelScope.launch {
            productRepository.getFeaturedProducts(limit = 10).collect { resource ->
                Log.d(TAG, "loadFeaturedProducts - Resource: ${resource::class.simpleName}")
                _featuredProducts.value = resource
            }
        }
    }
    
    fun loadAllProducts(
        page: Int = 1,
        categoryId: String? = null,
        search: String? = null
    ) {
        Log.d(TAG, "loadAllProducts - Starting: page=$page")
        viewModelScope.launch {
            productRepository.getProducts(
                page = page,
                limit = 20,
                categoryId = categoryId,
                search = search
            ).collect { resource ->
                Log.d(TAG, "loadAllProducts - Resource: ${resource::class.simpleName}")
                _allProducts.value = resource
            }
        }
    }
    
    fun refresh() {
        loadHeroSlides()
        loadFeaturedProducts()
        loadAllProducts()
    }
}
