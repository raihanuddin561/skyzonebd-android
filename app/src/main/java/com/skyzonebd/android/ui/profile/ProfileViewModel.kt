package com.skyzonebd.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.local.PreferencesManager
import com.skyzonebd.android.data.model.Address
import com.skyzonebd.android.data.model.User
import com.skyzonebd.android.data.remote.ApiService
import com.skyzonebd.android.data.repository.AuthRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _addresses = MutableStateFlow<Resource<List<Address>>?>(null)
    val addresses: StateFlow<Resource<List<Address>>?> = _addresses.asStateFlow()
    
    init {
        loadUser()
        loadAddresses()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _user.value = user
            }
        }
    }
    
    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _addresses.value = Resource.Loading()
                val response = apiService.getAddresses()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        _addresses.value = Resource.Success(apiResponse.data)
                    } else {
                        _addresses.value = Resource.Error(apiResponse?.message ?: "Failed to load addresses")
                    }
                } else {
                    _addresses.value = Resource.Error("Failed to load addresses")
                }
            } catch (e: Exception) {
                _addresses.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
