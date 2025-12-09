package com.skyzonebd.android.ui.rfq

import androidx.compose.foundation.layout.*
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
import com.skyzonebd.android.ui.navigation.Screen
import com.skyzonebd.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RFQScreen(
    navController: NavController,
    authViewModel: com.skyzonebd.android.ui.auth.AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var isAuthChecked by remember { mutableStateOf(false) }
    
    // Wait for auth state to be checked, then redirect if not logged in
    LaunchedEffect(currentUser) {
        kotlinx.coroutines.delay(100)
        isAuthChecked = true
        
        if (currentUser == null) {
            android.util.Log.d("RFQScreen", "No user found, redirecting to login")
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.RFQ.route) { inclusive = true }
            }
        } else {
            android.util.Log.d("RFQScreen", "User found: ${currentUser?.email}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My RFQs") },
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
                    onClick = { /* TODO: Navigate to create RFQ */ },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create RFQ")
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
        
        // Empty state (RFQ feature coming soon)
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
                    imageVector = Icons.Default.RequestQuote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No RFQs Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Request for Quotation feature is coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { navController.navigateUp() },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Back to Profile")
                }
            }
        }
    }
}
