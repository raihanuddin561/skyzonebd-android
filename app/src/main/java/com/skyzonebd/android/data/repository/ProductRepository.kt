package com.skyzonebd.android.data.repository

import android.util.Log
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.ProductsResponse
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    private val TAG = "ProductRepository"
    
    fun getProducts(
        page: Int = 1,
        limit: Int = 20, // Reduced from 100 for faster loading
        categorySlug: String? = null,
        search: String? = null,
        isFeatured: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        brand: String? = null,
        sortBy: String? = null
    ): Flow<Resource<ProductsResponse>> = flow {
        try {
            Log.d(TAG, "getProducts - Starting request: page=$page, limit=$limit, categorySlug=$categorySlug, search=$search, isFeatured=$isFeatured")
            emit(Resource.Loading())
            
            val response = apiService.getProducts(
                page = page,
                limit = limit,
                categorySlug = categorySlug,
                search = search,
                featured = if (isFeatured == true) "true" else null,
                minPrice = minPrice,
                maxPrice = maxPrice,
                brand = brand,
                sortBy = sortBy
            )
            
            Log.d(TAG, "getProducts - Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "getProducts - Response body: success=${apiResponse?.success}, data null=${apiResponse?.data == null}, products count=${apiResponse?.data?.products?.size}")
                
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    Log.d(TAG, "getProducts - Success! Products: ${apiResponse.data.products.size}")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    val errorMsg = apiResponse?.message ?: apiResponse?.error ?: "Failed to load products"
                    Log.e(TAG, "getProducts - Error: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Failed to load products: ${response.message()}"
                Log.e(TAG, "getProducts - HTTP Error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error occurred"
            Log.e(TAG, "getProducts - Exception: $errorMsg", e)
            emit(Resource.Error(errorMsg))
        }
    }
    
    fun getProduct(id: String): Flow<Resource<Product>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getProduct(id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // Check if data is null
                    if (apiResponse.data == null) {
                        emit(Resource.Error("Product not found"))
                        return@flow
                    }
                    
                    try {
                        // Extract product from the nested response
                        emit(Resource.Success(apiResponse.data.product))
                    } catch (e: Exception) {
                        emit(Resource.Error("Invalid product data format: ${e.message}"))
                    }
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load product"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Product not found"
                    500 -> "Server error"
                    else -> "Failed to load product: ${response.code()}"
                }
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    // Alias for consistency
    fun getProductById(id: String): Flow<Resource<Product>> = getProduct(id)
    
    fun getFeaturedProducts(limit: Int = 10): Flow<Resource<ProductsResponse>> = flow {
        try {
            Log.d(TAG, "getFeaturedProducts - Starting request: limit=$limit")
            emit(Resource.Loading())
            
            // Use the dedicated featured products endpoint
            val response = apiService.getFeaturedProducts(limit = limit)
            
            Log.d(TAG, "getFeaturedProducts - Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "getFeaturedProducts - Response body: success=${apiResponse?.success}, data null=${apiResponse?.data == null}, products count=${apiResponse?.data?.products?.size}")
                
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    Log.d(TAG, "getFeaturedProducts - Success! Products: ${apiResponse.data.products.size}")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    val errorMsg = apiResponse?.message ?: apiResponse?.error ?: "Failed to load featured products"
                    Log.e(TAG, "getFeaturedProducts - Error: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Failed to load featured products: ${response.message()}"
                Log.e(TAG, "getFeaturedProducts - HTTP Error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error occurred"
            Log.e(TAG, "getFeaturedProducts - Exception: $errorMsg", e)
            emit(Resource.Error(errorMsg))
        }
    }
    
    fun searchProducts(
        query: String,
        page: Int = 1,
        limit: Int = 20
    ): Flow<Resource<ProductsResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.searchProducts(query, page, limit)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Search failed"))
                }
            } else {
                emit(Resource.Error("Search failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}
