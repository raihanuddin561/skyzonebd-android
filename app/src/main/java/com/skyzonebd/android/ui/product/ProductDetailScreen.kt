package com.skyzonebd.android.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.cart.CartViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: CartViewModel
) {
    val productState by viewModel.product.collectAsState()
    val relatedProducts by viewModel.relatedProducts.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val cartItemCount by cartViewModel.itemCount.collectAsState()
    
    var selectedImageIndex by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(productId) {
        try {
            viewModel.loadProduct(productId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    com.skyzonebd.android.ui.common.CartIconWithBadge(
                        itemCount = cartItemCount,
                        onClick = { navController.navigate(Screen.Cart.route) }
                    )
                    IconButton(onClick = { /* Share product */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Add to wishlist */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                    }
                    
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
        },
        bottomBar = {
            when (val state = productState) {
                is Resource.Success -> state.data?.let { product ->
                    BottomAddToCartBar(
                        product = product,
                        quantity = quantity,
                        userType = currentUser?.userType ?: UserType.RETAIL,
                        onAddToCart = {
                            if (viewModel.validateQuantity(product.moq)) {
                                cartViewModel.addToCart(product, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Added to cart",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                scope.launch {
                                    val moq = product.moq ?: 1
                                    snackbarHostState.showSnackbar(
                                        message = "Minimum order quantity is $moq",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onBuyNow = {
                            if (viewModel.validateQuantity(product.moq)) {
                                cartViewModel.addToCart(product, quantity)
                                navController.navigate(Screen.Checkout.route)
                            } else {
                                scope.launch {
                                    val moq = product.moq ?: 1
                                    snackbarHostState.showSnackbar(
                                        message = "Minimum order quantity is $moq",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }
                else -> {}
            }
        }
    ) { padding ->
        when (val state = productState) {
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
            is Resource.Success -> state.data?.let { product ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Image Gallery
                    item {
                        ImageGallery(
                            images = product.images,
                            selectedIndex = selectedImageIndex,
                            onImageSelected = { selectedImageIndex = it }
                        )
                    }
                    
                    // Product Info
                    item {
                        ProductInfo(
                            product = product,
                            userType = currentUser?.userType ?: UserType.RETAIL
                        )
                    }
                    
                    // Quantity Selector
                    item {
                        QuantitySelector(
                            quantity = quantity,
                            moq = product.moq,
                            stock = product.stock,
                            onIncrement = { viewModel.incrementQuantity(product.moq) },
                            onDecrement = { viewModel.decrementQuantity(product.moq) },
                            onQuantityChange = { viewModel.setQuantity(it, product.moq) }
                        )
                    }
                    
                    // Wholesale Tier Pricing (for wholesale users)
                    if (currentUser?.userType == UserType.WHOLESALE && product.wholesaleTiers.isNotEmpty()) {
                        item {
                            WholesaleTierPricing(tiers = product.wholesaleTiers)
                        }
                    }
                    
                    // Product Description
                    item {
                        ProductDescription(description = product.description)
                    }
                    
                    // Specifications
                    item {
                        ProductSpecifications(product = product)
                    }
                    
                    // Related Products
                    if (relatedProducts.isNotEmpty()) {
                        item {
                            RelatedProducts(
                                products = relatedProducts,
                                userType = currentUser?.userType ?: UserType.RETAIL,
                                onProductClick = { relatedProduct ->
                                    navController.navigate("product/${relatedProduct.id}")
                                }
                            )
                        }
                    }
                    
                    // Bottom padding for bottom bar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Text(
                            text = state.message ?: "Failed to load product",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Error
                        )
                        Button(onClick = { viewModel.loadProduct(productId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ImageGallery(
    images: List<String>,
    selectedIndex: Int,
    onImageSelected: (Int) -> Unit
) {
    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        
        // Simple pan constraint - limit to reasonable bounds
        val maxOffset = 1000f * scale
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
            y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
        )
    }
    
    // Reset zoom when image changes
    LaunchedEffect(selectedIndex) {
        scale = 1f
        offset = Offset.Zero
    }
    
    Column {
        // Main Image with zoom and pan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (images.isNotEmpty()) {
                AsyncImage(
                    model = images.getOrNull(selectedIndex) ?: images.first(),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state),
                    contentScale = ContentScale.Fit
                )
                
                // Zoom slider control
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ZoomOut,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Slider(
                            value = scale,
                            onValueChange = { newScale ->
                                scale = newScale
                            },
                            valueRange = 1f..3f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        "${(scale * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                
                // Image indicator
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        images.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (index == selectedIndex) Color.White else Color.White.copy(alpha = 0.4f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                }
            } else {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Thumbnail Row
        if (images.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images.size) { index ->
                    Card(
                        modifier = Modifier
                            .size(70.dp)
                            .clickable { onImageSelected(index) },
                        border = if (index == selectedIndex) 
                            androidx.compose.foundation.BorderStroke(2.dp, Primary) 
                        else null,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (index == selectedIndex) 4.dp else 1.dp
                        )
                    ) {
                        AsyncImage(
                            model = images[index],
                            contentDescription = "Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductInfo(
    product: Product,
    userType: UserType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface  // Theme-aware text color
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Brand and Category
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (product.brand != null) {
                Text(
                    text = "Brand: ${product.brand}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant  // Theme-aware secondary text
                )
            }
            // Category display removed - API returns category as string ID, not object
            // Use categoryId if needed: product.categoryId
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Price
        val displayPrice = product.getDisplayPrice(userType)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "৳${displayPrice}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            // Show retail price strikethrough for wholesale users
            if (userType == UserType.WHOLESALE && product.wholesalePrice != null) {
                Text(
                    text = "৳${product.retailPrice}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
        
        if (userType == UserType.WHOLESALE) {
            Text(
                text = "Wholesale Price",
                style = MaterialTheme.typography.bodySmall,
                color = Secondary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stock Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (product.stock > 0) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (product.stock > 0) Color.Green else Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (product.stock > 0) "In Stock" else "Out of Stock",
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.stock > 0) Color.Green else Color.Red
            )
        }
        
        // MOQ for wholesale
        val moq = product.moq
        if (moq != null && moq > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Minimum Order Quantity: ${product.moq}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary
                )
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    moq: Int?,
    stock: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    var quantityText by remember(quantity) { mutableStateOf(quantity.toString()) }
    var showError by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            onDecrement()
                            quantityText = (quantity - 1).coerceAtLeast(moq ?: 1).toString()
                        },
                        enabled = quantity > (moq ?: 1)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = if (quantity > (moq ?: 1)) Primary else Color.Gray
                        )
                    }
                    
                    // Editable TextField for quantity
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newValue ->
                            // Only allow digits
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                quantityText = newValue
                                val newQty = newValue.toIntOrNull()
                                if (newQty != null) {
                                    val minQty = moq ?: 1
                                    when {
                                        newQty < minQty -> {
                                            showError = true
                                        }
                                        newQty > stock -> {
                                            showError = true
                                        }
                                        else -> {
                                            showError = false
                                            onQuantityChange(newQty)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        singleLine = true,
                        isError = showError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = Error,
                            errorTextColor = MaterialTheme.colorScheme.onSurface,
                            errorContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            onIncrement()
                            quantityText = (quantity + 1).coerceAtMost(stock).toString()
                        },
                        enabled = quantity < stock
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = if (quantity < stock) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Helper text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (moq != null && moq > 1) {
                    Text(
                        text = "Min: $moq units",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (showError && quantity < moq) Error else Secondary
                    )
                }
            }
            
            if (showError) {
                val minQty = moq ?: 1
                val errorMsg = when {
                    quantity < minQty -> "Minimum order quantity is $minQty"
                    quantity > stock -> "Only $stock units available"
                    else -> ""
                }
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WholesaleTierPricing(tiers: List<com.skyzonebd.android.data.model.WholesaleTier>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wholesale Tier Pricing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            tiers.forEach { tier ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${tier.minQuantity}+ units",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳${tier.price}/unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                if (tier != tiers.last()) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun ProductDescription(description: String?) {
    if (description.isNullOrBlank()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductSpecifications(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            SpecificationRow("SKU", product.sku)
            if (product.brand != null) {
                SpecificationRow("Brand", product.brand)
            }
            SpecificationRow("Weight", product.weight?.let { "${it}g" })
            SpecificationRow("Dimensions", product.dimensions)
        }
    }
}

@Composable
fun SpecificationRow(label: String, value: String?) {
    if (value == null) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RelatedProducts(
    products: List<Product>,
    userType: UserType,
    onProductClick: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Related Products",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                Box(modifier = Modifier.width(160.dp)) {
                    com.skyzonebd.android.ui.home.ProductCard(
                        product = product,
                        userType = userType,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomAddToCartBar(
    product: Product,
    quantity: Int,
    userType: UserType,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary
                ),
                enabled = product.stock > 0
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Cart")
            }
            
            Button(
                onClick = onBuyNow,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                enabled = product.stock > 0
            ) {
                Text("Buy Now")
            }
        }
    }
}

