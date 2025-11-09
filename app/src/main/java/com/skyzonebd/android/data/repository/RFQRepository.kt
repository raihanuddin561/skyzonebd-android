package com.skyzonebd.android.data.repository

import com.skyzonebd.android.data.model.CreateRFQRequest
import com.skyzonebd.android.data.model.RFQ
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RFQRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    fun getRFQs(
        page: Int = 1,
        limit: Int = 20
    ): Flow<Resource<List<RFQ>>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getRFQs(page, limit)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load RFQs"))
                }
            } else {
                emit(Resource.Error("Failed to load RFQs: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun getRFQ(id: String): Flow<Resource<RFQ>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getRFQ(id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to load RFQ"))
                }
            } else {
                emit(Resource.Error("Failed to load RFQ: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun createRFQ(request: CreateRFQRequest): Flow<Resource<RFQ>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.createRFQ(request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Failed to create RFQ"))
                }
            } else {
                emit(Resource.Error("Failed to create RFQ: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}
