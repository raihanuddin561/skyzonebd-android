package com.skyzonebd.android.data.repository

import com.skyzonebd.android.data.local.PreferencesManager
import com.skyzonebd.android.data.model.AuthResponse
import com.skyzonebd.android.data.model.LoginRequest
import com.skyzonebd.android.data.model.RegisterRequest
import com.skyzonebd.android.data.model.User
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    
    fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val authResponse = apiResponse.data
                    
                    if (authResponse.token != null) {
                        // Save token and user
                        preferencesManager.saveToken(authResponse.token)
                        authResponse.user?.let { preferencesManager.saveUser(it) }
                        
                        emit(Resource.Success(authResponse))
                    } else {
                        emit(Resource.Error("No authentication token received"))
                    }
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Login failed"))
                }
            } else {
                emit(Resource.Error("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    fun register(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        companyName: String? = null,
        isB2B: Boolean = false
    ): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val userType = if (isB2B) 
                com.skyzonebd.android.data.model.UserType.WHOLESALE 
            else 
                com.skyzonebd.android.data.model.UserType.RETAIL
            
            val request = RegisterRequest(
                email = email,
                password = password,
                name = name,
                phone = phone,
                companyName = companyName,
                userType = userType
            )
            
            val response = apiService.register(request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val authResponse = apiResponse.data
                    
                    if (authResponse.token != null) {
                        // Save token and user
                        preferencesManager.saveToken(authResponse.token)
                        authResponse.user?.let { preferencesManager.saveUser(it) }
                        
                        emit(Resource.Success(authResponse))
                    } else {
                        emit(Resource.Error("No authentication token received"))
                    }
                } else {
                    emit(Resource.Error(apiResponse?.message ?: apiResponse?.error ?: "Registration failed"))
                }
            } else {
                emit(Resource.Error("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
    
    suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            // Ignore network errors during logout
        } finally {
            preferencesManager.clear()
        }
    }
    
    fun getCurrentUser(): Flow<User?> {
        return preferencesManager.getUserFlow()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn()
    }
    
    suspend fun getToken(): String? {
        return preferencesManager.getToken()
    }
}
