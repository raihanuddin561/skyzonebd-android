package com.skyzonebd.android.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skyzonebd.android.data.model.OrderStatus
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: String,
    authViewModel: com.skyzonebd.android.ui.auth.AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Check authentication
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.OrderDetail.createRoute(orderId)) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Surface,
                    navigationIconContentColor = Surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Status Card
            item {
                OrderStatusCard(
                    orderId = orderId,
                    status = OrderStatus.PROCESSING
                )
            }
            
            // Order Timeline
            item {
                OrderTimeline(status = OrderStatus.PROCESSING)
            }
            
            // Shipping Information
            item {
                ShippingInformationCard(
                    name = currentUser?.name ?: "N/A",
                    address = "Sample Address, Dhaka, Bangladesh",
                    phone = currentUser?.email ?: "N/A"
                )
            }
            
            // Order Items
            item {
                Text(
                    text = "Order Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Sample items - in real app, load from API
            items(3) { index ->
                OrderItemCard(
                    productName = "Sample Product ${index + 1}",
                    quantity = index + 1,
                    price = 1000.0 * (index + 1),
                    imageUrl = null
                )
            }
            
            // Order Summary
            item {
                OrderSummaryCard(
                    subtotal = 6000.0,
                    shipping = 100.0,
                    total = 6100.0
                )
            }
            
            // Payment Information
            item {
                PaymentInformationCard(
                    method = "Cash on Delivery",
                    status = "Pending"
                )
            }
            
            // Action Buttons
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Track order */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Track Order")
                    }
                    
                    OutlinedButton(
                        onClick = { /* TODO: Contact support */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Support, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact Support")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OrderStatusCard(orderId: String, status: OrderStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                OrderStatus.PENDING -> Warning.copy(alpha = 0.1f)
                OrderStatus.PROCESSING -> Info.copy(alpha = 0.1f)
                OrderStatus.CONFIRMED -> Info.copy(alpha = 0.1f)
                OrderStatus.SHIPPED -> Primary.copy(alpha = 0.1f)
                OrderStatus.DELIVERED -> Success.copy(alpha = 0.1f)
                OrderStatus.CANCELLED -> Error.copy(alpha = 0.1f)
                OrderStatus.RETURNED -> Error.copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (status) {
                            OrderStatus.PENDING -> Warning.copy(alpha = 0.2f)
                            OrderStatus.PROCESSING -> Info.copy(alpha = 0.2f)
                            OrderStatus.CONFIRMED -> Info.copy(alpha = 0.2f)
                            OrderStatus.SHIPPED -> Primary.copy(alpha = 0.2f)
                            OrderStatus.DELIVERED -> Success.copy(alpha = 0.2f)
                            OrderStatus.CANCELLED -> Error.copy(alpha = 0.2f)
                            OrderStatus.RETURNED -> Error.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (status) {
                        OrderStatus.PENDING -> Icons.Default.Pending
                        OrderStatus.PROCESSING -> Icons.Default.Autorenew
                        OrderStatus.CONFIRMED -> Icons.Default.Check
                        OrderStatus.SHIPPED -> Icons.Default.LocalShipping
                        OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                        OrderStatus.CANCELLED -> Icons.Default.Cancel
                        OrderStatus.RETURNED -> Icons.Default.Undo
                    },
                    contentDescription = null,
                    tint = when (status) {
                        OrderStatus.PENDING -> Warning
                        OrderStatus.PROCESSING -> Info
                        OrderStatus.CONFIRMED -> Info
                        OrderStatus.SHIPPED -> Primary
                        OrderStatus.DELIVERED -> Success
                        OrderStatus.CANCELLED -> Error
                        OrderStatus.RETURNED -> Error
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = status.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Order #$orderId",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun OrderTimeline(status: OrderStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Order Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TimelineStep(
                title = "Order Placed",
                date = "Dec 31, 2025 - 10:30 AM",
                isCompleted = true
            )
            
            TimelineStep(
                title = "Processing",
                date = "In Progress",
                isCompleted = status != OrderStatus.PENDING,
                isActive = status == OrderStatus.PROCESSING
            )
            
            TimelineStep(
                title = "Shipped",
                date = "Pending",
                isCompleted = status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED,
                isActive = status == OrderStatus.SHIPPED
            )
            
            TimelineStep(
                title = "Delivered",
                date = "Pending",
                isCompleted = status == OrderStatus.DELIVERED,
                isActive = status == OrderStatus.DELIVERED
            )
        }
    }
}

@Composable
private fun TimelineStep(
    title: String,
    date: String,
    isCompleted: Boolean,
    isActive: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = when {
                        isCompleted -> Success
                        isActive -> Primary
                        else -> TextSecondary.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Surface,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Primary else TextPrimary
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ShippingInformationCard(
    name: String,
    address: String,
    phone: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shipping Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            InfoRow(label = "Name", value = name)
            InfoRow(label = "Address", value = address)
            InfoRow(label = "Contact", value = phone)
        }
    }
}

@Composable
private fun OrderItemCard(
    productName: String,
    quantity: Int,
    price: Double,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = productName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qty: $quantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "৳${String.format("%,.2f", price)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    shipping: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
            
            SummaryRow(label = "Subtotal", value = "৳${String.format("%,.2f", subtotal)}")
            SummaryRow(label = "Shipping", value = "৳${String.format("%,.2f", shipping)}")
            
            Divider()
            
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
                    text = "৳${String.format("%,.2f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun PaymentInformationCard(
    method: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            InfoRow(label = "Payment Method", value = method)
            InfoRow(label = "Payment Status", value = status)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
