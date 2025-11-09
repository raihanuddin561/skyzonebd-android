package com.skyzonebd.android.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.Category
import com.skyzonebd.android.data.model.ProductsResponse
import com.skyzonebd.android.data.repository.CategoryRepository
import com.skyzonebd.android.data.repository.ProductRepository
import com.skyzonebd.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _categories = MutableStateFlow<Resource<List<Category>>?>(null)
    val categories: StateFlow<Resource<List<Category>>?> = _categories.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    private val _categoryProducts = MutableStateFlow<Resource<ProductsResponse>?>(null)
    val categoryProducts: StateFlow<Resource<ProductsResponse>?> = _categoryProducts.asStateFlow()
    
    init {
        loadCategories()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { resource ->
                _categories.value = resource
            }
        }
    }
    
    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        loadCategoryProducts(category.id)
    }
    
    private fun loadCategoryProducts(categoryId: String, page: Int = 1) {
        viewModelScope.launch {
            productRepository.getProducts(
                page = page,
                limit = 20,
                categoryId = categoryId
            ).collect { resource ->
                _categoryProducts.value = resource
            }
        }
    }
    
    fun refresh() {
        loadCategories()
        _selectedCategory.value?.let { loadCategoryProducts(it.id) }
    }
}
