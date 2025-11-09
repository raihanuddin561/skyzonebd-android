package com.skyzonebd.android.ui.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.HeroSlide
import com.skyzonebd.android.data.model.Product
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val heroSlidesState by viewModel.heroSlides.collectAsState()
    val featuredProductsState by viewModel.featuredProducts.collectAsState()
    val allProductsState by viewModel.allProducts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
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
                    IconButton(onClick = { /* Navigate to notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                CategoryRow()
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
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(featuredProducts) { product ->
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
                                    color = TextSecondary
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
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
            fontWeight = FontWeight.Bold
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
fun CategoryRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(5) { index ->
            CategoryCard("Category ${index + 1}")
        }
    }
}

@Composable
fun CategoryCard(name: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundLight)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Product Image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(BackgroundLight),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(12.dp)) {
                // Product Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price
                val displayPrice = product.getDisplayPrice(userType)
                Text(
                    text = "৳${displayPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceColor
                )
                
                // Sale/Discount
                product.getDiscountPercentage()?.let { discount ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "৳${product.retailPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$discount% OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = SalePriceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Wholesale indicator
                if (userType == UserType.WHOLESALE && product.wholesaleEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MOQ: ${product.wholesaleMOQ} units",
                        style = MaterialTheme.typography.labelSmall,
                        color = WholesalePriceColor
                    )
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
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
