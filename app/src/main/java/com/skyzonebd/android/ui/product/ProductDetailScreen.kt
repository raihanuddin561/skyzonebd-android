package com.skyzonebd.android.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val productState by viewModel.product.collectAsState()
    val relatedProducts by viewModel.relatedProducts.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(productId) {
        try {
            viewModel.loadProduct(productId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share product */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Add to wishlist */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                                // Show snackbar or navigate to cart
                            }
                        },
                        onBuyNow = {
                            if (viewModel.validateQuantity(product.moq)) {
                                cartViewModel.addToCart(product, quantity)
                                navController.navigate("checkout")
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
                            style = MaterialTheme.typography.bodyLarge
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
    Column {
        // Main Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            if (images.isNotEmpty()) {
                AsyncImage(
                    model = images.getOrNull(selectedIndex) ?: images.first(),
                    contentDescription = "Product Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center),
                    tint = Color.Gray
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
                    AsyncImage(
                        model = images[index],
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (index == selectedIndex) Primary.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(if (index == selectedIndex) 2.dp else 0.dp)
                            .clickable { onImageSelected(index) },
                        contentScale = ContentScale.Crop
                    )
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
            fontWeight = FontWeight.Bold
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
                    color = Color.Gray
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
                    color = Color.Gray,
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
                text = if (product.stock > 0) "In Stock (${product.stock} available)" else "Out of Stock",
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = quantity > (moq ?: 1)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 40.dp)
                )
                
                IconButton(
                    onClick = onIncrement,
                    enabled = quantity < stock
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
        
        if (moq != null && moq > 1) {
            Text(
                text = "MOQ: $moq units",
                style = MaterialTheme.typography.bodySmall,
                color = Secondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
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
                fontWeight = FontWeight.Bold
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
                        style = MaterialTheme.typography.bodyMedium
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
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
                fontWeight = FontWeight.Bold
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
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                // Reuse ProductCard from HomeScreen
                Box(modifier = Modifier.width(160.dp)) {
                    // You'll need to import ProductCard from HomeScreen
                    // or create a shared component
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
