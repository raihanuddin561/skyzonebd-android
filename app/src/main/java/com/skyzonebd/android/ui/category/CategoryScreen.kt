package com.skyzonebd.android.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.Category
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.common.getCategoryIcon
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categorySlug: String? = null,
    viewModel: CategoryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val productsState by viewModel.categoryProducts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // If categorySlug is provided, select that category
    LaunchedEffect(categorySlug) {
        if (!categorySlug.isNullOrBlank()) {
            try {
                viewModel.selectCategoryBySlug(categorySlug)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedCategory?.name ?: "Categories") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectedCategory != null) {
                            viewModel.clearSelection()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show Login/Register for guest users
                    if (currentUser == null) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Login.route) }
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Login",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { navController.navigate(Screen.Profile.route) }
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            categorySlug != null && selectedCategory == null -> {
                // Loading specific category
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            selectedCategory != null -> {
                // Show products in selected category
                when (val state = productsState) {
                    is Resource.Loading, null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is Resource.Success -> {
                        val products = state.data?.products ?: emptyList()
                        if (products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "No products in this category",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(onClick = { navController.navigateUp() }) {
                                        Text("Go Back")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(products) { product ->
                                    com.skyzonebd.android.ui.home.ProductCard(
                                        product = product,
                                        userType = currentUser?.userType ?: UserType.RETAIL,
                                        onClick = {
                                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(state.message ?: "Failed to load products")
                                Button(onClick = { 
                                    categorySlug?.let { viewModel.selectCategoryBySlug(it) }
                                }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // Show categories list
                when (val state = categoriesState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is Resource.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data ?: emptyList()) { category ->
                                CategoryGridCard(
                                    category = category,
                                    onClick = { viewModel.selectCategory(category) }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(state.message ?: "Failed to load categories")
                                Button(onClick = { viewModel.loadCategories() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun CategoryGridCard(
    category: Category,
    onClick: () -> Unit
) {
    // Generate unique gradient colors based on category name hash - theme aware
    val gradientColors = remember(category.id) {
        val hash = category.name.hashCode()
        val hue = (hash % 360).toFloat()
        listOf(
            Color.hsv(hue, 0.6f, 0.9f),
            Color.hsv((hue + 30) % 360, 0.7f, 0.95f)
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors.map { it.copy(alpha = 0.15f) }
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Category Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.name),
                        contentDescription = category.name,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Name
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Product Count with badge style
                if (category.count != null && category.count > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${category.count} items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (category.count != null && category.count > 0) {
                Text(
                    "${category.count} products",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

