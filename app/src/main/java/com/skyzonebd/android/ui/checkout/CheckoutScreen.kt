package com.skyzonebd.android.ui.checkout

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skyzonebd.android.data.model.Address
import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.data.model.PaymentMethod
import com.skyzonebd.android.ui.cart.CartViewModel
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.AppConfig
import com.skyzonebd.android.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    cartViewModel: CartViewModel,
    authViewModel: com.skyzonebd.android.ui.auth.AuthViewModel = hiltViewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalAmount by cartViewModel.totalAmount.collectAsState()
    val paymentMethod by checkoutViewModel.paymentMethod.collectAsState()
    val orderState by checkoutViewModel.orderState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    
    var note by remember { mutableStateOf("") }
    var shippingAddressText by remember { mutableStateOf("") }
    var billingAddressText by remember { mutableStateOf("") }
    var paymentReference by remember { mutableStateOf("") }  // Transaction ID for manual payments
    
    // Guest user fields
    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestMobile by remember { mutableStateOf("") }
    var guestCompany by remember { mutableStateOf("") }
    
    // Registered user mobile (editable)
    var userMobile by remember { mutableStateOf(currentUser?.phone ?: "") }
    
    val isGuest = currentUser == null
    
    // Update userMobile when currentUser changes
    LaunchedEffect(currentUser) {
        userMobile = currentUser?.phone ?: ""
    }
    
    // Only Cash on Delivery is currently active; others are coming soon
    val availablePaymentMethods = listOf(
        PaymentMethod.CASH_ON_DELIVERY,
        PaymentMethod.BKASH,
        PaymentMethod.NAGAD,
        PaymentMethod.ROCKET,
        PaymentMethod.BANK_TRANSFER,
        PaymentMethod.CREDIT_CARD
    )

    // Methods that are enabled (only Cash on Delivery for now)
    val enabledPaymentMethods = setOf(PaymentMethod.CASH_ON_DELIVERY)
    
    // Check if current payment method requires transaction ID
    val requiresPaymentReference = paymentMethod == PaymentMethod.BKASH || 
                                   paymentMethod == PaymentMethod.BANK_TRANSFER
    
    // Track if navigation has been triggered to prevent re-navigation
    // Using rememberSaveable to persist across configuration changes
    var hasNavigated by rememberSaveable { mutableStateOf(false) }
    
    // Handle order creation result
    LaunchedEffect(orderState) {
        val state = orderState
        android.util.Log.d("CheckoutScreen", "LaunchedEffect triggered: state=$state, hasNavigated=$hasNavigated")
        
        if (state is Resource.Success && !hasNavigated) {
            val order = state.data
            android.util.Log.d("CheckoutScreen", "Order received: id=${order?.id}, orderNumber=${order?.orderNumber}")
            android.util.Log.d("CheckoutScreen", "Order object: $order")
            
            if (order != null && !order.id.isNullOrBlank()) {
                hasNavigated = true
                android.util.Log.d("CheckoutScreen", "Starting navigation process...")
                
                // Store order for success screen FIRST (before clearing cart)
                com.skyzonebd.android.util.OrderCache.setLastOrder(order)
                android.util.Log.d("CheckoutScreen", "Order stored in cache")
                
                // Clear cart
                cartViewModel.clearCart()
                android.util.Log.d("CheckoutScreen", "Cart cleared")
                
                // Small delay to ensure order is saved
                kotlinx.coroutines.delay(100)
                
                // Navigate
                try {
                    val route = Screen.OrderSuccess.createRoute(order.id)
                    android.util.Log.d("CheckoutScreen", "Navigating to route: $route")
                    android.util.Log.d("CheckoutScreen", "Order ID: ${order.id}")
                    android.util.Log.d("CheckoutScreen", "Order Number: ${order.orderNumber}")
                    
                    // Post navigation to next frame to avoid composition issues
                    kotlinx.coroutines.delay(200)
                    
                    navController.navigate(route) {
                        // Remove checkout screen from backstack
                        popUpTo(Screen.Checkout.route) { 
                            inclusive = true 
                        }
                        // Avoid multiple instances of success screen
                        launchSingleTop = true
                    }
                    android.util.Log.d("CheckoutScreen", "Navigation called successfully")
                    
                    // Reset order state after navigation to prevent re-triggering
                    kotlinx.coroutines.delay(100)
                    checkoutViewModel.resetOrderState()
                    android.util.Log.d("CheckoutScreen", "Order state reset")
                } catch (e: Exception) {
                    android.util.Log.e("CheckoutScreen", "Navigation failed with exception: ${e.message}", e)
                    e.printStackTrace()
                    hasNavigated = false // Reset flag if navigation failed
                }
            } else {
                android.util.Log.e("CheckoutScreen", "Order is null or has null ID! Order: $order")
            }
        } else if (state is Resource.Error) {
            android.util.Log.e("CheckoutScreen", "Order creation error: ${state.message}")
            // Reset navigation flag on error so user can retry
            hasNavigated = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                totalAmount = totalAmount,
                onPlaceOrder = {
                    // If only one address is provided, use it for both
                    val finalShippingAddress = shippingAddressText.ifBlank { billingAddressText }
                    val finalBillingAddress = billingAddressText.ifBlank { shippingAddressText }
                    
                    if (isGuest) {
                        // Guest checkout - validate guest info
                        checkoutViewModel.placeGuestOrder(
                            items = cartItems,
                            totalAmount = totalAmount,
                            note = note.takeIf { it.isNotBlank() },
                            guestName = guestName,
                            guestEmail = guestEmail,
                            guestMobile = guestMobile,
                            guestCompany = guestCompany.takeIf { it.isNotBlank() },
                            shippingAddress = finalShippingAddress,
                            billingAddress = finalBillingAddress,
                            paymentReference = paymentReference.takeIf { requiresPaymentReference && it.isNotBlank() }
                        )
                    } else {
                        // Registered user checkout
                        checkoutViewModel.placeOrder(
                            items = cartItems,
                            totalAmount = totalAmount,
                            note = note.takeIf { it.isNotBlank() },
                            shippingAddress = finalShippingAddress,
                            billingAddress = finalBillingAddress,
                            mobile = userMobile,
                            paymentReference = paymentReference.takeIf { requiresPaymentReference && it.isNotBlank() }
                        )
                    }
                },
                enabled = (shippingAddressText.isNotBlank() || billingAddressText.isNotBlank()) &&
                        orderState !is Resource.Loading &&
                        (!isGuest || (guestName.isNotBlank() && guestMobile.isNotBlank())) &&
                        (isGuest || userMobile.isNotBlank()) &&
                        (!requiresPaymentReference || paymentReference.length >= 5)  // Validate transaction ID
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
            // User Information Section
            item {
                SectionCard(
                    title = if (isGuest) "Your Information" else "Contact Information",
                    icon = Icons.Default.Person
                ) {
                    if (isGuest) {
                        // Guest User Fields
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { guestName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full Name *") },
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = guestEmail,
                                onValueChange = { guestEmail = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Email (Optional)") },
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = guestMobile,
                                onValueChange = { guestMobile = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Mobile Number *") },
                                singleLine = true,
                                placeholder = { Text("+880-1711-123456") }
                            )
                            
                            OutlinedTextField(
                                value = guestCompany,
                                onValueChange = { guestCompany = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Company Name (Optional)") },
                                singleLine = true
                            )
                        }
                    } else {
                        // Registered User Fields
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Name (read-only)
                            OutlinedTextField(
                                value = currentUser?.name ?: "",
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full Name") },
                                singleLine = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            
                            // Email (read-only)
                            OutlinedTextField(
                                value = currentUser?.email ?: "",
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Email") },
                                singleLine = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            
                            // Mobile (editable)
                            OutlinedTextField(
                                value = userMobile,
                                onValueChange = { userMobile = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Mobile Number *") },
                                singleLine = true,
                                placeholder = { Text("+880-1711-123456") },
                                supportingText = { 
                                    Text(
                                        "You can update your mobile number if needed",
                                        style = MaterialTheme.typography.bodySmall
                                    ) 
                                }
                            )
                            
                            // Company Name (read-only if exists)
                            if (!currentUser?.companyName.isNullOrBlank()) {
                                OutlinedTextField(
                                    value = currentUser?.companyName ?: "",
                                    onValueChange = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Company Name") },
                                    singleLine = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Shipping Address Section
            item {
                SectionCard(
                    title = "Shipping Address",
                    icon = Icons.Default.LocationOn
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = shippingAddressText,
                            onValueChange = { shippingAddressText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your complete shipping address...") },
                            minLines = 4,
                            maxLines = 6
                        )
                        if (shippingAddressText.isNotBlank() && billingAddressText.isBlank()) {
                            Text(
                                text = "ℹ This address will be used for billing too",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary
                            )
                        }
                    }
                }
            }
            
            // Billing Address Section
            item {
                SectionCard(
                    title = "Billing Address",
                    icon = Icons.Default.LocationOn
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = billingAddressText,
                            onValueChange = { billingAddressText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your billing address (optional if same as shipping)...") },
                            minLines = 4,
                            maxLines = 6
                        )
                        if (billingAddressText.isNotBlank() && shippingAddressText.isBlank()) {
                            Text(
                                text = "ℹ This address will be used for shipping too",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary
                            )
                        }
                    }
                }
            }
            
            // Payment Method Section
            item {
                SectionCard(
                    title = "Payment Method",
                    icon = Icons.Default.Payment
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availablePaymentMethods.forEach { method ->
                            val isEnabled = method in enabledPaymentMethods
                            PaymentMethodOption(
                                method = method,
                                selected = paymentMethod == method,
                                enabled = isEnabled,
                                onSelect = {
                                    if (isEnabled) checkoutViewModel.setPaymentMethod(method)
                                }
                            )
                        }
                    }
                }
            }
            
            // Transaction ID Section (for manual payment methods)
            if (requiresPaymentReference) {
                item {
                    SectionCard(
                        title = when (paymentMethod) {
                            PaymentMethod.BKASH -> "bKash Transaction ID"
                            PaymentMethod.BANK_TRANSFER -> "Bank Transfer Reference"
                            else -> "Payment Reference"
                        },
                        icon = Icons.Default.Receipt
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = paymentReference,
                                onValueChange = { paymentReference = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Transaction ID / Reference Number *") },
                                placeholder = { 
                                    Text(
                                        when (paymentMethod) {
                                            PaymentMethod.BKASH -> "e.g., 8N5A2B3C4D"
                                            PaymentMethod.BANK_TRANSFER -> "e.g., TRX20260124ABC123"
                                            else -> "Enter reference number"
                                        }
                                    ) 
                                },
                                isError = paymentReference.isNotBlank() && paymentReference.length < 5,
                                supportingText = {
                                    if (paymentReference.isNotBlank() && paymentReference.length < 5) {
                                        Text(
                                            "Minimum 5 characters required",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            when (paymentMethod) {
                                                PaymentMethod.BKASH -> "Enter the TrxID from your bKash app after sending money"
                                                PaymentMethod.BANK_TRANSFER -> "Enter the transaction reference from your bank"
                                                else -> "This helps us verify your payment"
                                            }
                                        )
                                    }
                                },
                                singleLine = true
                            )
                            
                            // Helper card with payment instructions
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "How to get your Transaction ID:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            when (paymentMethod) {
                                                PaymentMethod.BKASH -> "1. Send money to our bKash number\n2. Open bKash app → Go to Transactions\n3. Find your payment → Copy TrxID\n4. Paste it above"
                                                PaymentMethod.BANK_TRANSFER -> "1. Transfer money to our bank account\n2. Check your bank statement or receipt\n3. Copy the transaction reference number\n4. Paste it above"
                                                else -> "Check your payment confirmation for the reference number"
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Order Items Section
            item {
                SectionCard(
                    title = "Order Items (${cartItems.size})",
                    icon = Icons.Default.ShoppingBag
                ) {}
            }
            
            items(cartItems) { item ->
                CheckoutItemCard(item)
            }
            
            // Order Note Section
            item {
                SectionCard(
                    title = "Order Note (Optional)",
                    icon = Icons.Default.Notes
                ) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add any special instructions...") },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Order Summary
            item {
                OrderSummaryCard(
                    subtotal = totalAmount,
                    shipping = 0.0,
                    total = totalAmount
                )
            }
            
            // Error Message
            if (orderState is Resource.Error) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Error
                                )
                                Text(
                                    "Failed to Place Order",
                                    color = Error,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                (orderState as Resource.Error).message ?: "Network error occurred",
                                color = Error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Please check your internet connection and try again.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // Loading indicator
            if (orderState is Resource.Loading) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text(
                                "Processing your order...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Need Help? Contact card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = Primary
                            )
                            Column {
                                Text(
                                    "Need Help?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Contact us for assistance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${AppConfig.COMPANY_PHONE}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text(AppConfig.COMPANY_PHONE)
                        }
                    }
                }
            }
            
            // Success indicator - for debugging
            if (orderState is Resource.Success) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Order Created!",
                                color = Success,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Order ID: ${(orderState as Resource.Success).data?.id}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Navigating to success page...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Primary)
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun PaymentMethodOption(
    method: PaymentMethod,
    selected: Boolean,
    enabled: Boolean = true,
    onSelect: () -> Unit
) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        selected -> Primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        selected -> Primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected && enabled,
                enabled = enabled,
                onClick = onSelect
            ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected && enabled) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected && enabled) 3.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            else if (selected) Primary.copy(alpha = 0.15f)
                            else Primary.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (method) {
                            PaymentMethod.CASH_ON_DELIVERY -> Icons.Default.Money
                            PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                            PaymentMethod.BKASH, PaymentMethod.NAGAD, PaymentMethod.ROCKET -> Icons.Default.PhoneAndroid
                            PaymentMethod.CREDIT_CARD -> Icons.Default.CreditCard
                            PaymentMethod.INVOICE_NET30, PaymentMethod.INVOICE_NET60 -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                               else if (selected) Primary
                               else Primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        when (method) {
                            PaymentMethod.CASH_ON_DELIVERY -> "Cash on Delivery"
                            PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
                            PaymentMethod.BKASH -> "bKash"
                            PaymentMethod.NAGAD -> "Nagad"
                            PaymentMethod.ROCKET -> "Rocket"
                            PaymentMethod.CREDIT_CARD -> "Credit / Debit Card"
                            PaymentMethod.INVOICE_NET30 -> "Invoice (Net 30)"
                            PaymentMethod.INVOICE_NET60 -> "Invoice (Net 60)"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected && enabled) FontWeight.Bold else FontWeight.Normal,
                        color = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.onSurface
                    )
                    if (method == PaymentMethod.CASH_ON_DELIVERY && enabled) {
                        Text(
                            "Pay when your order arrives",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (!enabled) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Coming Soon",
                                style = MaterialTheme.typography.labelSmall,
                                color = Warning,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            RadioButton(
                selected = selected && enabled,
                onClick = if (enabled) onSelect else null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Primary,
                    unselectedColor = if (!enabled)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun CheckoutItemCard(item: CartItem) {
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                val quantityText = if (item.product.displayUnit.isNotEmpty()) {
                    "${item.quantity}${item.product.displayUnit} × ৳${item.price}/${item.product.displayUnit}"
                } else {
                    "Quantity: ${item.quantity} × ৳${item.price}"
                }
                Text(
                    quantityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Text(
                "৳${item.total}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    shipping: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                Text("৳$subtotal", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shipping", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (shipping > 0) "৳$shipping" else "FREE",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (shipping == 0.0) Success else OnSurface
                )
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "৳$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun CheckoutBottomBar(
    totalAmount: Double,
    onPlaceOrder: () -> Unit,
    enabled: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        "৳$totalAmount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
            Button(
                onClick = onPlaceOrder,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Place Order", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
