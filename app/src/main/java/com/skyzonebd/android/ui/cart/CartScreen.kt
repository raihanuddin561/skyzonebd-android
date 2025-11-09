package com.skyzonebd.android.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.data.model.UserType
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userType = currentUser?.userType ?: UserType.RETAIL
    
    // Update prices when user type changes
    LaunchedEffect(userType) {
        viewModel.updatePricesForUserType(userType)
    }
    
    val totalSavings = viewModel.getTotalSavings(userType)
    val shipping = if (totalAmount > 0) 50.0 else 0.0
    val tax = totalAmount * 0.05
    val total = totalAmount + shipping + tax
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CheckoutBottomBar(
                    total = total,
                    totalSavings = totalSavings,
                    onCheckout = {
                        navController.navigate(Screen.Checkout.route)
                    }
                )
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartView(
                onContinueShopping = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        userType = userType,
                        onQuantityChange = { newQuantity ->
                            viewModel.updateQuantity(item.id, newQuantity)
                        },
                        onIncrement = { viewModel.incrementQuantity(item.id) },
                        onDecrement = { viewModel.decrementQuantity(item.id) },
                        onRemove = {
                            viewModel.removeFromCart(item.id)
                        }
                    )
                }
                
                // Order Summary
                item {
                    OrderSummaryCard(
                        subtotal = totalAmount,
                        shipping = shipping,
                        tax = tax,
                        total = total,
                        totalSavings = totalSavings
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(
    onContinueShopping: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add some products to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Continue Shopping")
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    userType: UserType,
    onQuantityChange: (Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Product Image
            AsyncImage(
                model = item.product.images.firstOrNull(),
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "৳${item.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceColor
                    )
                    
                    // Show retail price strikethrough for wholesale
                    if (userType == UserType.WHOLESALE && item.product.wholesalePrice != null) {
                        Text(
                            text = "৳${item.product.retailPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Quantity Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(32.dp),
                            enabled = item.quantity > (item.product.moq ?: 1)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(32.dp),
                            enabled = item.quantity < item.product.stock
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    
                    // Remove Button
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Error
                        )
                    }
                }
                
                // Subtotal & MOQ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val moq = item.product.moq
                    if (moq != null && moq > 1) {
                        Text(
                            text = "MOQ: $moq",
                            style = MaterialTheme.typography.bodySmall,
                            color = Secondary
                        )
                    }
                    Text(
                        text = "Subtotal: ৳${"%.2f".format(item.price * item.quantity)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    shipping: Double,
    tax: Double,
    total: Double,
    totalSavings: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OrderSummaryRow("Subtotal", subtotal)
            OrderSummaryRow("Shipping", shipping)
            OrderSummaryRow("Tax (5%)", tax)
            
            if (totalSavings > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "You Save (Wholesale)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Green
                    )
                    Text(
                        text = "-৳${"%.2f".format(totalSavings)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "৳${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun OrderSummaryRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "৳${"%.2f".format(amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CheckoutBottomBar(
    total: Double,
    totalSavings: Double,
    onCheckout: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (totalSavings > 0) {
                Text(
                    text = "You saved ৳${"%.2f".format(totalSavings)} with wholesale pricing!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "৳${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.width(180.dp)
                ) {
                    Text("Proceed to Checkout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
