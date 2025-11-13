package com.skyzonebd.android.data.repository

import android.util.Log
import com.skyzonebd.android.data.model.CreateOrderRequest
import com.skyzonebd.android.data.model.Order
import com.skyzonebd.android.data.model.OrdersResponse
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    companion object {
        private const val TAG = "OrderRepository"
    }
    
    fun getOrders(
        page: Int = 1,
        limit: Int = 20
    ): Flow<Resource<OrdersResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getOrders(page, limit)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load orders"))
                }
            } else {
                emit(Resource.Error("Failed to load orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun getOrder(id: String): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getOrder(id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load order"))
                }
            } else {
                emit(Resource.Error("Failed to load order: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun createOrder(request: CreateOrderRequest): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.createOrder(request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                
                if (apiResponse != null) {
                    val orderResponse = apiResponse.data
                    val order = orderResponse?.order
                    
                    if (apiResponse.success && order != null) {
                        emit(Resource.Success(order))
                    } else {
                        val errorMsg = apiResponse.message ?: apiResponse.error ?: "Failed to create order"
                        emit(Resource.Error(errorMsg))
                    }
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Failed to create order: ${response.message()}, Body: $errorBody"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error occurred"
            emit(Resource.Error(errorMsg))
        }
    }
    
    fun cancelOrder(id: String): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.cancelOrder(id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to cancel order"))
                }
            } else {
                emit(Resource.Error("Failed to cancel order: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}
