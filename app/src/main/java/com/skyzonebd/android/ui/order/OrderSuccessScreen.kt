package com.skyzonebd.android.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skyzonebd.android.data.model.Order
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    orderId: String,
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel(),
    authViewModel: com.skyzonebd.android.ui.auth.AuthViewModel = hiltViewModel()
) {
    android.util.Log.d("OrderSuccessScreen", "=== Screen composed ===")
    android.util.Log.d("OrderSuccessScreen", "OrderId parameter: '$orderId'")
    
    val orderState by viewModel.selectedOrder.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isGuest = currentUser == null
    
    android.util.Log.d("OrderSuccessScreen", "Is guest user: $isGuest")
    
    // Get order from singleton cache (set when order was placed)
    var orderFromCache by remember { mutableStateOf<Order?>(null) }
    var cacheLoadError by remember { mutableStateOf<String?>(null) }
    
    // Load order from cache once when screen is created
    LaunchedEffect(Unit) {
        android.util.Log.d("OrderSuccessScreen", "LaunchedEffect - Loading from cache...")
        try {
            orderFromCache = com.skyzonebd.android.util.OrderCache.getLastOrder()
            android.util.Log.d("OrderSuccessScreen", "Loaded order from cache: ${orderFromCache?.id}")
            if (orderFromCache != null) {
                android.util.Log.d("OrderSuccessScreen", "Cache order number: ${orderFromCache?.orderNumber}")
                android.util.Log.d("OrderSuccessScreen", "Cache order items: ${orderFromCache?.orderItems?.size}")
            } else {
                android.util.Log.w("OrderSuccessScreen", "No order found in cache!")
            }
        } catch (e: Exception) {
            android.util.Log.e("OrderSuccessScreen", "Error loading order from cache: ${e.message}", e)
            cacheLoadError = e.message
            e.printStackTrace()
        }
    }
    
    // Only try to load from API if user is logged in and order wasn't passed
    LaunchedEffect(orderId, currentUser, orderFromCache) {
        if (!isGuest && orderId.isNotEmpty() && orderId != "null" && orderFromCache == null) {
            android.util.Log.d("OrderSuccessScreen", "Loading order from API: $orderId")
            viewModel.loadOrderDetails(orderId)
        }
    }
    
    // Clear the cache when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("OrderSuccessScreen", "Screen disposed, clearing cache")
            com.skyzonebd.android.util.OrderCache.clearLastOrder()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Placed Successfully") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Icon and Message
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Success.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Success
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Order Placed Successfully!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Thank you for your order. We'll send you a confirmation email shortly.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Order Details
            // Use order from cache if available (just placed), otherwise use from API (logged-in users)
            val displayOrder = orderFromCache ?: (orderState as? Resource.Success)?.data
            
            if (displayOrder != null) {
                item {
                    OrderSummaryCard(displayOrder)
                }
                
                // Order Items
                if (displayOrder.orderItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Order Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(displayOrder.orderItems) { item ->
                        OrderItemCard(item)
                    }
                }
            } else {
                // Show loading or error only if we're trying to fetch from API
                when (val state = orderState) {
                    is Resource.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is Resource.Error -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Error)
                                    Text(
                                        text = state.message ?: "Failed to load order details",
                                        color = Error
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // For guest users, show a message if order details aren't available
                        if (isGuest) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PrimaryLight)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Order Confirmation",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Your order has been placed successfully. Order ID: $orderId",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "You will receive a confirmation via email/SMS shortly.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Only show "View All Orders" for logged-in users
                    if (!isGuest) {
                        Button(
                            onClick = {
                                navController.navigate(Screen.Orders.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View All Orders")
                        }
                    } else {
                        // For guest users, show login prompt
                        Button(
                            onClick = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Login to Track Orders")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Continue Shopping")
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OrderSummaryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            OrderDetailRow("Order Number", order.orderNumber)
            OrderDetailRow("Status", order.status.name)
            OrderDetailRow("Payment Method", order.paymentMethod)
            OrderDetailRow("Payment Status", order.paymentStatus.name)
            
            Divider()
            
            OrderDetailRow("Subtotal", "৳${String.format("%.2f", order.subtotal)}")
            if (order.tax > 0) {
                OrderDetailRow("Tax", "৳${String.format("%.2f", order.tax)}")
            }
            if (order.shipping > 0) {
                OrderDetailRow("Shipping", "৳${String.format("%.2f", order.shipping)}")
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "৳${String.format("%.2f", order.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            if (!order.shippingAddress.isNullOrEmpty()) {
                Divider()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Shipping Address",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.shippingAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!order.notes.isNullOrEmpty()) {
                Divider()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Order Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OrderItemCard(item: com.skyzonebd.android.data.model.OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.takeIf { it.isNotEmpty() } ?: item.product?.name ?: "Product #${item.productId}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qty: ${item.quantity}${item.unit} × ৳${String.format("%.2f", item.price)}/${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "৳${String.format("%.2f", item.total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}

