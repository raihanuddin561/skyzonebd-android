package com.skyzonebd.android.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.home.HomeViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: com.skyzonebd.android.ui.cart.CartViewModel
) {
    val allProductsState by viewModel.allProducts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val cartItemCount by cartViewModel.itemCount.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("newest") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllProducts()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    com.skyzonebd.android.ui.common.CartIconWithBadge(
                        itemCount = cartItemCount,
                        onClick = { navController.navigate(Screen.Cart.route) }
                    )
                    
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
        when (val state = allProductsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is Resource.Success -> {
                var products = state.data?.products ?: emptyList()
                
                // Apply filters
                if (selectedCategory != null) {
                    products = products.filter { it.categoryId == selectedCategory }
                }
                
                // Apply sorting
                products = when (sortBy) {
                    "price_low" -> products.sortedBy { it.price }
                    "price_high" -> products.sortedByDescending { it.price }
                    "name_asc" -> products.sortedBy { it.name }
                    "name_desc" -> products.sortedByDescending { it.name }
                    else -> products // newest (default API order)
                }
                
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Products Available",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Check back later for new items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Filter/Sort Chips
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SurfaceLight,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active filters indicator
                                if (selectedCategory != null || sortBy != "newest") {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            selectedCategory = null
                                            sortBy = "newest"
                                        },
                                        label = { Text("Clear Filters") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                                
                                // Sort chip
                                FilterChip(
                                    selected = sortBy != "newest",
                                    onClick = { showFilterDialog = true },
                                    label = {
                                        Text(
                                            when (sortBy) {
                                                "price_low" -> "Price: Low to High"
                                                "price_high" -> "Price: High to Low"
                                                "name_asc" -> "Name: A-Z"
                                                "name_desc" -> "Name: Z-A"
                                                else -> "Sort"
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Sort,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                
                                // Category filter chip
                                if (selectedCategory != null) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { showFilterDialog = true },
                                        label = { Text("Category") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Category,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Products Count
                        state.data?.pagination?.let { pagination ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SurfaceLight,
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${pagination.total ?: products.size} Products",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    // Sort/Filter options could go here
                                }
                            }
                        }
                        
                        // Products Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                ProductGridItem(
                                    product = product,
                                    userType = currentUser?.userType ?: UserType.GUEST,
                                    onClick = {
                                        navController.navigate(Screen.ProductDetail.createRoute(product.id))
                                    }
                                )
                            }
                            
                            // Load More Button
                            state.data?.pagination?.let { pagination ->
                                if (pagination.hasNext == true) {
                                    item {
                                        Button(
                                            onClick = {
                                                viewModel.loadAllProducts(page = (pagination.page ?: 1) + 1)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            Text("Load More")
                                        }
                                    }
                                }
                            }
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
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ErrorLight
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Failed to Load Products",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = state.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.loadAllProducts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            ProductFilterDialog(
                currentSort = sortBy,
                currentCategory = selectedCategory,
                onDismiss = { showFilterDialog = false },
                onApply = { newSort, newCategory ->
                    sortBy = newSort
                    selectedCategory = newCategory
                    showFilterDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductGridItem(
    product: Product,
    userType: UserType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Featured Badge
                if (product.isFeatured == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = Secondary,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "Featured",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price Display
                val displayPrice = product.getDisplayPrice(userType)
                
                if (userType == UserType.WHOLESALE && product.wholesaleEnabled) {
                    // Show retail price crossed out and wholesale price
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (product.displayUnit.isNotEmpty()) "৳${product.retailPrice}/${product.displayUnit}" else "৳${product.retailPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = if (product.displayUnit.isNotEmpty()) "৳$displayPrice/${product.displayUnit}" else "৳$displayPrice",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    
                    // MOQ Badge
                    product.moq?.let { moq ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Secondary.copy(alpha = 0.2f),
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = "MOQ: $moq",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (product.displayUnit.isNotEmpty()) "৳$displayPrice/${product.displayUnit}" else "৳$displayPrice",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                
                // Stock Status
                if (product.availability != "in_stock") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorLight
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterDialog(
    currentSort: String,
    currentCategory: String?,
    onDismiss: () -> Unit,
    onApply: (String, String?) -> Unit
) {
    var selectedSort by remember { mutableStateOf(currentSort) }
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort Products") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sort Options
                Text(
                    "Sort By",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                val sortOptions = listOf(
                    "newest" to "Newest First",
                    "price_low" to "Price: Low to High",
                    "price_high" to "Price: High to Low",
                    "name_asc" to "Name: A-Z",
                    "name_desc" to "Name: Z-A"
                )
                
                sortOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSort = value }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == value,
                            onClick = { selectedSort = value }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Category Filter (simplified - you can expand this)
                Text(
                    "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All Categories")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(selectedSort, selectedCategory) }
            ) {
                Text("Apply", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

