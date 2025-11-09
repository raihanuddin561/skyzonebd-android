package com.skyzonebd.android.ui.rfq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.CreateRFQRequest
import com.skyzonebd.android.data.model.RFQ
import com.skyzonebd.android.data.repository.RFQRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RFQViewModel @Inject constructor(
    private val rfqRepository: RFQRepository
) : ViewModel() {
    
    private val _rfqs = MutableStateFlow<Resource<List<RFQ>>?>(null)
    val rfqs: StateFlow<Resource<List<RFQ>>?> = _rfqs.asStateFlow()
    
    private val _selectedRFQ = MutableStateFlow<Resource<RFQ>?>(null)
    val selectedRFQ: StateFlow<Resource<RFQ>?> = _selectedRFQ.asStateFlow()
    
    private val _createState = MutableStateFlow<Resource<RFQ>?>(null)
    val createState: StateFlow<Resource<RFQ>?> = _createState.asStateFlow()
    
    init {
        loadRFQs()
    }
    
    fun loadRFQs(page: Int = 1) {
        viewModelScope.launch {
            rfqRepository.getRFQs(page).collect { resource ->
                _rfqs.value = resource
            }
        }
    }
    
    fun loadRFQDetails(id: String) {
        viewModelScope.launch {
            rfqRepository.getRFQ(id).collect { resource ->
                _selectedRFQ.value = resource
            }
        }
    }
    
    fun createRFQ(request: CreateRFQRequest) {
        viewModelScope.launch {
            rfqRepository.createRFQ(request).collect { resource ->
                _createState.value = resource
                if (resource is Resource.Success) {
                    loadRFQs()
                }
            }
        }
    }
    
    fun refresh() {
        loadRFQs()
    }
}
