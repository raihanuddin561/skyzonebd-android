package com.skyzonebd.android.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
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
    
    private val TAG = "AuthRepository"
    private val gson = Gson()
    
    fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            // Client-side validation
            if (email.isBlank()) {
                emit(Resource.Error("Email is required"))
                return@flow
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emit(Resource.Error("Please enter a valid email address"))
                return@flow
            }
            
            if (password.isBlank()) {
                emit(Resource.Error("Password is required"))
                return@flow
            }
            
            if (password.length < 6) {
                emit(Resource.Error("Password must be at least 6 characters"))
                return@flow
            }
            
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    if (authResponse.success && authResponse.token != null) {
                        // Save token and user
                        preferencesManager.saveToken(authResponse.token)
                        authResponse.user?.let { preferencesManager.saveUser(it) }
                        
                        Log.d(TAG, "Login successful!")
                        emit(Resource.Success(authResponse))
                    } else {
                        // API returned success=false with error message
                        val error = authResponse.error ?: authResponse.message ?: "Invalid email or password"
                        Log.e(TAG, "Login failed with error: $error")
                        emit(Resource.Error(error))
                    }
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                // Parse error body to get actual error message
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                    jsonObject.get("error")?.asString 
                        ?: jsonObject.get("message")?.asString 
                        ?: getDefaultLoginErrorMessage(response.code())
                } catch (e: Exception) {
                    getDefaultLoginErrorMessage(response.code())
                }
                Log.e(TAG, "Login error: $errorMessage (Code: ${response.code()}, Response: $errorBody)")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "No internet connection. Please check your network."
                e.message?.contains("timeout") == true -> 
                    "Connection timeout. Please check your internet and try again."
                e.message?.contains("Connection refused") == true -> 
                    "Cannot connect to server. Please try again later."
                else -> 
                    "Network error: ${e.message ?: "Please check your connection"}"
            }
            Log.e(TAG, "Login exception: $errorMessage", e)
            emit(Resource.Error(errorMessage))
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
            
            // Client-side validation
            if (name.isBlank()) {
                emit(Resource.Error("Name is required"))
                return@flow
            }
            
            if (email.isBlank()) {
                emit(Resource.Error("Email is required"))
                return@flow
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emit(Resource.Error("Please enter a valid email address"))
                return@flow
            }
            
            if (password.isBlank()) {
                emit(Resource.Error("Password is required"))
                return@flow
            }
            
            if (password.length < 6) {
                emit(Resource.Error("Password must be at least 6 characters"))
                return@flow
            }
            
            // Validate required fields per web API
            if (phone.isNullOrBlank()) {
                emit(Resource.Error("Phone number is required"))
                return@flow
            }
            
            if (companyName.isNullOrBlank()) {
                emit(Resource.Error("Company name is required"))
                return@flow
            }
            
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
            
            Log.d(TAG, "Registration request: email=$email, name=$name, phone=$phone, companyName=$companyName, userType=$userType")
            
            val response = apiService.register(request)
            
            Log.d(TAG, "Registration response code: ${response.code()}")
            Log.d(TAG, "Registration response message: ${response.message()}")
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                Log.d(TAG, "Auth Response: $authResponse")
                
                if (authResponse != null) {
                    if (authResponse.success && authResponse.token != null) {
                        // Save token and user
                        preferencesManager.saveToken(authResponse.token)
                        authResponse.user?.let { 
                            Log.d(TAG, "Saving user: ${it.email}")
                            preferencesManager.saveUser(it) 
                        }
                        
                        Log.d(TAG, "Registration successful! Token saved.")
                        emit(Resource.Success(authResponse))
                    } else {
                        // API returned success=false with error message
                        val error = authResponse.error ?: authResponse.message ?: "Registration failed. Please try again."
                        Log.e(TAG, "Registration failed with error: $error")
                        emit(Resource.Error(error))
                    }
                } else {
                    val error = "Empty response from server"
                    Log.e(TAG, error)
                    emit(Resource.Error(error))
                }
            } else {
                // Parse error body to get actual error message
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Registration HTTP error - Code: ${response.code()}, Message: ${response.message()}")
                Log.e(TAG, "Registration error body: $errorBody")
                
                val errorMessage = try {
                    val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                    Log.d(TAG, "Parsed error JSON: $jsonObject")
                    
                    val errorMsg = jsonObject.get("error")?.asString 
                        ?: jsonObject.get("message")?.asString 
                        ?: null
                    
                    if (errorMsg != null) {
                        Log.d(TAG, "Extracted error message: $errorMsg")
                        errorMsg
                    } else {
                        Log.w(TAG, "No error/message field found in JSON, using default")
                        getDefaultRegistrationErrorMessage(response.code())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse error JSON: ${e.message}", e)
                    getDefaultRegistrationErrorMessage(response.code())
                }
                
                Log.e(TAG, "Final registration error message: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "No internet connection. Please check your network."
                e.message?.contains("timeout") == true -> 
                    "Connection timeout. Please check your internet and try again."
                e.message?.contains("Connection refused") == true -> 
                    "Cannot connect to server. Please try again later."
                else -> 
                    "Network error: ${e.message ?: "Please check your connection"}"
            }
            Log.e(TAG, "Registration exception: $errorMessage", e)
            emit(Resource.Error(errorMessage))
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
    
    private fun getDefaultLoginErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid email or password. Please check your credentials."
            401 -> "Invalid email or password. Please try again."
            403 -> "Your account has been disabled. Please contact support."
            404 -> "Account not found. Please check your email or register."
            422 -> "Invalid email or password format."
            429 -> "Too many login attempts. Please try again later."
            500 -> "Server error. Please try again later."
            503 -> "Service temporarily unavailable. Please try again later."
            else -> "Login failed. Please check your email and password."
        }
    }
    
    private fun getDefaultRegistrationErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid registration data. Please check all fields."
            409 -> "Email already exists. Please use a different email or login."
            422 -> "Invalid data format. Please check all fields."
            429 -> "Too many registration attempts. Please try again later."
            500 -> "Server error. Please try again later."
            503 -> "Service temporarily unavailable. Please try again later."
            else -> "Registration failed. Please try again."
        }
    }
}
