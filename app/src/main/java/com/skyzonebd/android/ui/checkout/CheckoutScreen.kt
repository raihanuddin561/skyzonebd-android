package com.skyzonebd.android.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skyzonebd.android.data.model.Address
import com.skyzonebd.android.data.model.CartItem
import com.skyzonebd.android.ui.cart.CartViewModel
import com.skyzonebd.android.ui.theme.*
import com.skyzonebd.android.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalAmount by cartViewModel.totalAmount.collectAsState()
    val shippingAddress by checkoutViewModel.shippingAddress.collectAsState()
    val paymentMethod by checkoutViewModel.paymentMethod.collectAsState()
    val orderState by checkoutViewModel.orderState.collectAsState()
    
    var note by remember { mutableStateOf("") }
    var showAddressDialog by remember { mutableStateOf(false) }
    
    // Handle order creation result
    LaunchedEffect(orderState) {
        when (orderState) {
            is Resource.Success -> {
                // Clear cart and navigate to order confirmation
                cartViewModel.clearCart()
                navController.navigate("order_success/${(orderState as Resource.Success).data?.id}") {
                    popUpTo("checkout") { inclusive = true }
                }
                checkoutViewModel.resetOrderState()
            }
            is Resource.Error -> {
                // Error is shown in UI
            }
            else -> {}
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
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                totalAmount = totalAmount,
                onPlaceOrder = {
                    checkoutViewModel.placeOrder(
                        items = cartItems,
                        totalAmount = totalAmount,
                        note = note.takeIf { it.isNotBlank() }
                    )
                },
                enabled = shippingAddress != null && orderState !is Resource.Loading
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
            // Shipping Address Section
            item {
                SectionCard(
                    title = "Shipping Address",
                    icon = Icons.Default.LocationOn
                ) {
                    if (shippingAddress != null) {
                        AddressCard(
                            address = shippingAddress!!,
                            onEdit = { showAddressDialog = true }
                        )
                    } else {
                        OutlinedButton(
                            onClick = { showAddressDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Shipping Address")
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
                        PaymentMethod.values().forEach { method ->
                            PaymentMethodOption(
                                method = method,
                                selected = paymentMethod == method,
                                onSelect = { checkoutViewModel.setPaymentMethod(method) }
                            )
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Error
                            )
                            Text(
                                (orderState as Resource.Error).message ?: "Failed to place order",
                                color = Error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Address Dialog
    if (showAddressDialog) {
        AddressInputDialog(
            address = shippingAddress,
            onDismiss = { showAddressDialog = false },
            onSave = { address ->
                checkoutViewModel.setShippingAddress(address)
                showAddressDialog = false
            }
        )
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
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
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
fun AddressCard(
    address: Address,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        address.name ?: "Shipping Address",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(address.phone ?: "", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(address.street, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${address.city}, ${address.state} ${address.postalCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(address.country, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = Primary)
                }
            }
        }
    }
}

@Composable
fun PaymentMethodOption(
    method: PaymentMethod,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) PrimaryLight else Surface
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
                    when (method) {
                        PaymentMethod.CASH_ON_DELIVERY -> Icons.Default.Money
                        PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                        PaymentMethod.MOBILE_BANKING -> Icons.Default.PhoneAndroid
                        PaymentMethod.CREDIT_CARD -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = if (selected) Primary else OnSurface
                )
                Text(
                    method.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
            RadioButton(
                selected = selected,
                onClick = onSelect
            )
        }
    }
}

@Composable
fun CheckoutItemCard(item: CartItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
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
                Text(
                    "Quantity: ${item.quantity}",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            Button(
                onClick = onPlaceOrder,
                enabled = enabled,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Place Order", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressInputDialog(
    address: Address?,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var name by remember { mutableStateOf(address?.name ?: "") }
    var phone by remember { mutableStateOf(address?.phone ?: "") }
    var street by remember { mutableStateOf(address?.street ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var state by remember { mutableStateOf(address?.state ?: "") }
    var postalCode by remember { mutableStateOf(address?.postalCode ?: "") }
    var country by remember { mutableStateOf(address?.country ?: "Bangladesh") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (address == null) "Add Address" else "Edit Address") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                item {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State/Division") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Postal Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Address(
                            id = address?.id ?: "",
                            userId = address?.userId ?: "",
                            name = name,
                            phone = phone,
                            street = street,
                            city = city,
                            state = state,
                            postalCode = postalCode,
                            country = country,
                            isDefault = address?.isDefault ?: false
                        )
                    )
                },
                enabled = name.isNotBlank() && phone.isNotBlank() && street.isNotBlank() &&
                        city.isNotBlank() && state.isNotBlank() && postalCode.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
