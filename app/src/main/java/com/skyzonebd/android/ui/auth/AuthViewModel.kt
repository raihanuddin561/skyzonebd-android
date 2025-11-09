package com.skyzonebd.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.AuthResponse
import com.skyzonebd.android.data.model.User
import com.skyzonebd.android.data.repository.AuthRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState.asStateFlow()
    
    val currentUser: StateFlow<User?> = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val isLoggedIn: StateFlow<Boolean> = currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { resource ->
                _loginState.value = resource
            }
        }
    }
    
    fun register(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        companyName: String? = null,
        isB2B: Boolean = false
    ) {
        viewModelScope.launch {
            authRepository.register(email, password, name, phone, companyName, isB2B)
                .collect { resource ->
                    _registerState.value = resource
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
    
    fun clearLoginState() {
        _loginState.value = null
    }
    
    fun clearRegisterState() {
        _registerState.value = null
    }
}
