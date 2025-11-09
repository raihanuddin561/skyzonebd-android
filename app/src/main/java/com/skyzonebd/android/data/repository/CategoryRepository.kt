package com.skyzonebd.android.data.repository

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
    
    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getCategories()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load categories"))
                }
            } else {
                emit(Resource.Error("Failed to load categories: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun getCategory(id: String): Flow<Resource<Category>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getCategory(id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load category"))
                }
            } else {
                emit(Resource.Error("Failed to load category: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}
