package com.skyzonebd.android.data.repository

import android.util.Log
import com.skyzonebd.android.data.model.Category
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    companion object {
        private const val TAG = "CategoryRepository"
    }
    
    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        try {
            Log.d(TAG, "getCategories - Starting request")
            emit(Resource.Loading())
            
            val response = apiService.getCategories()
            
            Log.d(TAG, "getCategories - Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "getCategories - Response body: success=${apiResponse?.success}, data null=${apiResponse?.data == null}, categories count=${apiResponse?.data?.size}")
                
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    Log.d(TAG, "getCategories - Success! Categories: ${apiResponse.data.size}")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    val errorMsg = apiResponse?.message ?: apiResponse?.error ?: "Failed to load categories"
                    Log.e(TAG, "getCategories - Error: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Failed to load categories: ${response.message()}"
                Log.e(TAG, "getCategories - HTTP Error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error occurred"
            Log.e(TAG, "getCategories - Exception: $errorMsg", e)
            emit(Resource.Error(errorMsg))
        }
    }
    
    fun getCategory(id: String): Flow<Resource<Category>> = flow {
        try {
            Log.d(TAG, "getCategory - Starting request: id=$id")
            emit(Resource.Loading())
            
            val response = apiService.getCategory(id)
            
            Log.d(TAG, "getCategory - Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "getCategory - Response body: success=${apiResponse?.success}, data null=${apiResponse?.data == null}")
                
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    Log.d(TAG, "getCategory - Success! Category: ${apiResponse.data.name}")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    val errorMsg = apiResponse?.message ?: apiResponse?.error ?: "Failed to load category"
                    Log.e(TAG, "getCategory - Error: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Failed to load category: ${response.message()}"
                Log.e(TAG, "getCategory - HTTP Error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error occurred"
            Log.e(TAG, "getCategory - Exception: $errorMsg", e)
            emit(Resource.Error(errorMsg))
        }
    }
}
