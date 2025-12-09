package com.skyzonebd.android.ui.address

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    navController: NavController,
    authViewModel: com.skyzonebd.android.ui.auth.AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var isAuthChecked by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Sample addresses (in a real app, load from repository)
    var addresses by remember { 
        mutableStateOf(listOf<Address>()) 
    }
    
    // Wait for auth state to be checked, then redirect if not logged in
    LaunchedEffect(currentUser) {
        kotlinx.coroutines.delay(100)
        isAuthChecked = true
        
        if (currentUser == null) {
            android.util.Log.d("AddressesScreen", "No user found, redirecting to login")
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Addresses.route) { inclusive = true }
            }
        } else {
            android.util.Log.d("AddressesScreen", "User found: ${currentUser?.email}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (isAuthChecked && currentUser != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Address")
                }
            }
        }
    ) { padding ->
        // Show loading while checking authentication
        if (!isAuthChecked || currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        
        if (addresses.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No Addresses Yet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Add your delivery addresses for faster checkout",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Address")
                    }
                }
            }
        } else {
            // Addresses list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses) { address ->
                    AddressCard(
                        address = address,
                        onEdit = { /* TODO: Edit address */ },
                        onDelete = { 
                            addresses = addresses.filter { it.id != address.id }
                        }
                    )
                }
            }
        }
        
        // Add/Edit Address Dialog
        if (showAddDialog) {
            AddAddressDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newAddress ->
                    addresses = addresses + newAddress
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.name ?: "Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (!address.phone.isNullOrBlank()) {
                        Text(
                            text = address.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Primary
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = buildString {
                    append(address.street)
                    append(", ${address.city}")
                    if (address.state.isNotBlank()) append(", ${address.state}")
                    if (address.postalCode.isNotBlank()) append(" - ${address.postalCode}")
                },
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (address.isDefault == true) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Default",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Label (Home, Office, etc.)") },
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
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State/Division") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = { Text("Postal Code") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it }
                        )
                        Text("Set as default address")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (street.isNotBlank() && city.isNotBlank()) {
                        onSave(
                            Address(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name.ifBlank { null },
                                phone = phone.ifBlank { null },
                                street = street,
                                city = city,
                                state = state,
                                postalCode = postalCode,
                                isDefault = isDefault
                            )
                        )
                    }
                },
                enabled = street.isNotBlank() && city.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
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
