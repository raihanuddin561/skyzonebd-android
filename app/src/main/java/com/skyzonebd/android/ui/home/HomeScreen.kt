package com.skyzonebd.android.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.skyzonebd.android.data.model.Category
import com.skyzonebd.android.data.model.HeroSlide
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.AppConfig
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: com.skyzonebd.android.ui.cart.CartViewModel
) {
    val heroSlidesState by viewModel.heroSlides.collectAsState()
    val featuredProductsState by viewModel.featuredProducts.collectAsState()
    val allProductsState by viewModel.allProducts.collectAsState()
    val categoriesState by viewModel.categories.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val cartItemCount by cartViewModel.itemCount.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SkyzoneBD",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${AppConfig.COMPANY_PHONE}")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Contact Us")
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
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User Type Banner (B2B/B2C)
            item {
                UserTypeBanner(
                    userType = currentUser?.userType ?: UserType.GUEST,
                    onClick = {
                        if (currentUser == null) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            navController.navigate(Screen.Wholesale.route)
                        }
                    }
                )
            }
            
            // Hero Banner / Carousel
            item {
                HeroSection(
                    heroSlidesState = heroSlidesState,
                    onSlideClick = { slide ->
                        slide.productId?.let { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    }
                )
            }
            
            // Quick Categories
            item {
                SectionHeader(
                    title = "Categories",
                    onSeeAllClick = { navController.navigate(Screen.Categories.route) }
                )
                CategoryRow(
                    categoriesState = categoriesState,
                    onCategoryClick = { category ->
                        navController.navigate(Screen.CategoryProducts.createRoute(category.slug))
                    }
                )
            }
            
            // Featured Products
            item {
                SectionHeader(
                    title = "Featured Products",
                    onSeeAllClick = { navController.navigate(Screen.Products.route) }
                )
            }
            
            when (val state = featuredProductsState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
                is Resource.Success -> {
                    val featuredProducts = state.data?.products ?: emptyList()
                    if (featuredProducts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No featured products available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(
                                    items = featuredProducts,
                                    key = { product -> product.id } // Add key for better performance
                                ) { product ->
                                    ProductCard(
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
                }
                is Resource.Error -> {
                    item {
                        ErrorMessage(
                            message = state.message ?: "Failed to load products",
                            onRetry = { viewModel.loadFeaturedProducts() }
                        )
                    }
                }
                else -> {}
            }
            
            // All Products Grid
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "All Products",
                    onSeeAllClick = { navController.navigate(Screen.Products.route) }
                )
            }
            
            when (val state = allProductsState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
                is Resource.Success -> {
                    val products = state.data?.products ?: emptyList()
                    if (products.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No products available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(products.chunked(2)) { rowProducts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowProducts.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductCard(
                                            product = product,
                                            userType = currentUser?.userType ?: UserType.RETAIL,
                                            onClick = {
                                                navController.navigate(Screen.ProductDetail.createRoute(product.id))
                                            }
                                        )
                                    }
                                }
                                // Add empty space if odd number of products
                                if (rowProducts.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        ErrorMessage(
                            message = state.message ?: "Failed to load products",
                            onRetry = { viewModel.loadAllProducts() }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserTypeBanner(
    userType: UserType,
    onClick: () -> Unit
) {
    val (backgroundColor, text, icon) = when (userType) {
        UserType.WHOLESALE -> Triple(
            SecondaryLight,
            "You're shopping as Wholesale Buyer - Get volume discounts!",
            Icons.Default.Business
        )
        UserType.RETAIL -> Triple(
            Primary,
            "Want wholesale prices? Switch to Business Account",
            Icons.Default.Store
        )
        UserType.GUEST -> Triple(
            Info,
            "Sign up for exclusive deals and wholesale pricing",
            Icons.Default.Person
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun HeroSection(
    heroSlidesState: Resource<List<HeroSlide>>?,
    onSlideClick: (HeroSlide) -> Unit
) {
    when (heroSlidesState) {
        is Resource.Success -> {
            val slides = heroSlidesState.data ?: emptyList()
            if (slides.isEmpty()) {
                // Show default static hero if no slides
                DefaultHeroSection()
            } else {
                val pagerState = rememberPagerState(pageCount = { slides.size })
                val coroutineScope = rememberCoroutineScope()
                
                // Auto-scroll effect
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5000) // 5 seconds per slide
                        val nextPage = (pagerState.currentPage + 1) % slides.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        val slide = slides[page]
                        HeroSlideItem(
                            slide = slide,
                            onClick = { onSlideClick(slide) }
                        )
                    }
                    
                    // Page indicator
                    if (slides.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(slides.size) { index ->
                                val color = if (pagerState.currentPage == index) Primary else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
        is Resource.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            DefaultHeroSection()
        }
        null -> {
            DefaultHeroSection()
        }
    }
}

@Composable
fun HeroSlideItem(
    slide: HeroSlide,
    onClick: () -> Unit
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(slide.bgColor ?: "#3B82F6"))
    } catch (e: Exception) {
        Primary
    }
    
    val textColor = try {
        Color(android.graphics.Color.parseColor(slide.textColor ?: "#FFFFFF"))
    } catch (e: Exception) {
        Color.White
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (!slide.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = slide.imageUrl,
                    contentDescription = slide.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Overlay gradient for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = slide.title ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                
                if (!slide.subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = slide.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
                
                if (!slide.buttonText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(slide.buttonText, color = bgColor)
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultHeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Primary)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to SkyzoneBD",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your B2B & B2C Marketplace",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text("See All", color = Primary)
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    categoriesState: Resource<List<Category>>?,
    onCategoryClick: (Category) -> Unit
) {
    when (categoriesState) {
        is Resource.Success -> {
            val categories = categoriesState.data ?: emptyList()
            if (categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories.take(6)) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }
        }
        is Resource.Loading -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .background(BackgroundLight, RoundedCornerShape(8.dp))
                    )
                }
            }
        }
        else -> {} // Error or null - just don't show categories
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.skyzonebd.android.ui.common.getCategoryIcon(category.name),
                    contentDescription = category.name,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category Name
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    userType: UserType,
    onClick: () -> Unit
) {
    // Memoize calculations to avoid recomputation on recomposition
    val displayPrice = remember(product, userType) { product.getDisplayPrice(userType) }
    val discountPercentage = remember(product) { product.getDiscountPercentage() }
    val hasDiscount = remember(displayPrice, product.retailPrice) { 
        discountPercentage != null && displayPrice < product.retailPrice 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Featured badge
                if (product.isFeatured) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Discount badge
                discountPercentage?.let { discount ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = SalePriceColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "-$discount%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Product Name - Theme-aware color for dark mode
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price Section
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (product.displayUnit.isNotEmpty()) "৳${String.format("%.0f", displayPrice)}/${product.displayUnit}" else "৳${String.format("%.0f", displayPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    // Original price strikethrough if discounted
                    if (hasDiscount) {
                        Text(
                            text = if (product.displayUnit.isNotEmpty()) "৳${String.format("%.0f", product.retailPrice)}/${product.displayUnit}" else "৳${String.format("%.0f", product.retailPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Stock status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (product.stock > 0) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (product.stock > 0) Success else Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (product.stock > 0) "In Stock" else "Out of Stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stock > 0) Success else Error,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Wholesale indicator
                if (userType == UserType.WHOLESALE && product.wholesaleEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "MOQ: ${product.wholesaleMOQ} units",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

