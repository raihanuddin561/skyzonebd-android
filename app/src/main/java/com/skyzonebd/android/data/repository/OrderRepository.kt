package com.skyzonebd.android.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
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
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "OrderRepository"
    }
    
    fun getOrders(
        page: Int = 1,
        limit: Int = 20
    ): Flow<Resource<OrdersResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            android.util.Log.d(TAG, "Fetching orders - page: $page, limit: $limit")
            val response = apiService.getOrders(page, limit)
            
            android.util.Log.d(TAG, "Orders response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val ordersResponse = response.body()
                android.util.Log.d(TAG, "Orders received: ${ordersResponse?.orders?.size ?: 0} orders")
                
                if (ordersResponse != null) {
                    emit(Resource.Success(ordersResponse))
                } else {
                    emit(Resource.Error("No orders data received"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Failed to load orders. Code: ${response.code()}, Body: $errorBody")
                
                val errorMessage = try {
                    val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                    jsonObject.get("error")?.asString 
                        ?: jsonObject.get("message")?.asString 
                        ?: "Failed to load orders"
                } catch (e: Exception) {
                    "Failed to load orders: ${response.message()}"
                }
                
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Exception loading orders", e)
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun getOrder(id: String): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())
            
            android.util.Log.d(TAG, "Fetching order details for ID: $id")
            val response = apiService.getOrder(id)
            
            android.util.Log.d(TAG, "Order detail response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val order = response.body()
                android.util.Log.d(TAG, "Order received: ${order?.id}")
                
                if (order != null) {
                    emit(Resource.Success(order))
                } else {
                    emit(Resource.Error("No order data received"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Failed to load order. Code: ${response.code()}, Body: $errorBody")
                
                val errorMessage = try {
                    val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                    jsonObject.get("error")?.asString 
                        ?: jsonObject.get("message")?.asString 
                        ?: "Failed to load order"
                } catch (e: Exception) {
                    "Failed to load order: ${response.message()}"
                }
                
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Exception loading order details", e)
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
                val errorMessage = try {
                    val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                    jsonObject.get("error")?.asString 
                        ?: jsonObject.get("message")?.asString 
                        ?: "Failed to create order"
                } catch (e: Exception) {
                    "Failed to create order: ${response.code()} ${response.message()}"
                }
                Log.e(TAG, "Create order error: $errorMessage (Response: $errorBody)")
                emit(Resource.Error(errorMessage))
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
