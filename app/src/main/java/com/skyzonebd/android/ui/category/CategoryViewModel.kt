package com.skyzonebd.android.ui.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyzonebd.android.data.model.Category
import com.skyzonebd.android.data.model.ProductsResponse
import com.skyzonebd.android.data.repository.CategoryRepository
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
    
    private var categoriesJob: Job? = null
    private var productsJob: Job? = null
    
    companion object {
        private const val TAG = "CategoryViewModel"
    }
    
    init {
        Log.d(TAG, "CategoryViewModel initialized")
        loadCategories()
    }
    
    fun loadCategories() {
        Log.d(TAG, "loadCategories - Starting")
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            categoryRepository.getCategories().collect { resource ->
                Log.d(TAG, "loadCategories - Resource: ${resource::class.simpleName}")
                _categories.value = resource
            }
        }
    }
    
    fun selectCategory(category: Category) {
        Log.d(TAG, "selectCategory - Category: ${category.name} (${category.slug})")
        _selectedCategory.value = category
        loadCategoryProducts(category.slug)
    }
    
    fun selectCategoryBySlug(categorySlug: String) {
        Log.d(TAG, "selectCategoryBySlug - CategorySlug: $categorySlug")
        viewModelScope.launch {
            // Find category from loaded categories by slug
            val category = (_categories.value as? Resource.Success)?.data?.find { it.slug == categorySlug }
            if (category != null) {
                Log.d(TAG, "selectCategoryBySlug - Found category: ${category.name}")
                _selectedCategory.value = category
            } else {
                Log.d(TAG, "selectCategoryBySlug - Category not found, creating placeholder")
                // Create a placeholder category with the slug
                _selectedCategory.value = Category(
                    id = "",
                    name = categorySlug.replace("-", " ").split(" ").joinToString(" ") { it.capitalize() },
                    slug = categorySlug,
                    description = null,
                    imageUrl = null,
                    isActive = true,
                    count = null,
                    createdAt = "",
                    updatedAt = ""
                )
            }
            loadCategoryProducts(categorySlug)
        }
    }
    
    fun selectCategoryById(categoryId: String) {
        Log.d(TAG, "selectCategoryById - CategoryId: $categoryId")
        viewModelScope.launch {
            // Wait for categories to load if they haven't yet
            if (_categories.value !is Resource.Success) {
                Log.d(TAG, "selectCategoryById - Categories not loaded yet, waiting...")
                // Wait for categories to load (with timeout)
                val startTime = System.currentTimeMillis()
                while (_categories.value !is Resource.Success && System.currentTimeMillis() - startTime < 5000) {
                    kotlinx.coroutines.delay(100)
                }
            }
            
            // Find category from loaded categories
            val category = (_categories.value as? Resource.Success)?.data?.find { it.id == categoryId }
            if (category != null) {
                Log.d(TAG, "selectCategoryById - Found category: ${category.name}, slug: ${category.slug}")
                _selectedCategory.value = category
                loadCategoryProducts(category.slug)
            } else {
                Log.e(TAG, "selectCategoryById - Category not found with ID: $categoryId")
                _categoryProducts.value = Resource.Error("Category not found")
            }
        }
    }
    
    fun clearSelection() {
        Log.d(TAG, "clearSelection - Clearing category selection")
        _selectedCategory.value = null
        _categoryProducts.value = null
    }
    
    private fun loadCategoryProducts(categorySlug: String, page: Int = 1) {
        Log.d(TAG, "loadCategoryProducts - CategorySlug: $categorySlug, Page: $page")
        // Cancel any previous products loading job
        productsJob?.cancel()
        // Set loading state immediately
        _categoryProducts.value = Resource.Loading()
        Log.d(TAG, "loadCategoryProducts - Set loading state, starting API call")
        productsJob = viewModelScope.launch {
            try {
                productRepository.getProducts(
                    page = page,
                    limit = 20,
                    categorySlug = categorySlug
                ).collect { resource ->
                    Log.d(TAG, "loadCategoryProducts - Collected resource: ${resource::class.simpleName}")
                    when (resource) {
                        is Resource.Success -> {
                            val count = resource.data?.products?.size ?: 0
                            Log.d(TAG, "loadCategoryProducts - Success! Products count: $count")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "loadCategoryProducts - Error: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "loadCategoryProducts - Loading from repository...")
                        }
                    }
                    _categoryProducts.value = resource
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadCategoryProducts - Exception: ${e.message}", e)
                _categoryProducts.value = Resource.Error(e.message ?: "Failed to load products")
            }
        }
    }
    
    fun refresh() {
        Log.d(TAG, "refresh - Refreshing categories and products")
        loadCategories()
        _selectedCategory.value?.let { loadCategoryProducts(it.slug) }
    }
}
