package com.skyzonebd.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.ProductsResponse
import com.skyzonebd.android.data.repository.ProductRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<Resource<ProductsResponse>?>(null)
    val searchResults: StateFlow<Resource<ProductsResponse>?> = _searchResults.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private var searchJob: Job? = null
    
    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            searchJob?.cancel()
            _searchResults.value = null
            return
        }
        
        // Cancel previous search
        searchJob?.cancel()
        // Set loading state immediately
        _searchResults.value = Resource.Loading()
        
        searchJob = viewModelScope.launch {
            productRepository.searchProducts(query).collect { resource ->
                _searchResults.value = resource
            }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = null
    }
}
