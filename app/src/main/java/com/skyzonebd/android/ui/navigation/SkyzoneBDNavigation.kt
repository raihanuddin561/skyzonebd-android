package com.skyzonebd.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skyzonebd.android.ui.auth.AuthViewModel
import com.skyzonebd.android.ui.auth.LoginScreen
import com.skyzonebd.android.ui.auth.RegisterScreen
import com.skyzonebd.android.ui.cart.CartScreen
import com.skyzonebd.android.ui.cart.CartViewModel
import com.skyzonebd.android.ui.category.CategoryScreen
import com.skyzonebd.android.ui.checkout.CheckoutScreen
import com.skyzonebd.android.ui.home.HomeScreen
import com.skyzonebd.android.ui.product.ProductDetailScreen
import com.skyzonebd.android.ui.product.ProductsScreen
import com.skyzonebd.android.ui.order.OrdersScreen
import com.skyzonebd.android.ui.profile.ProfileScreen
import com.skyzonebd.android.ui.search.SearchScreen
import com.skyzonebd.android.ui.components.BottomNavigationBar

@Composable
fun SkyzoneBDNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel() // Shared CartViewModel for all screens
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Check if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Products.route,
        Screen.Categories.route,
        Screen.Cart.route,
        Screen.Profile.route
    )
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, cartViewModel = cartViewModel)
            }
            
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    navController = navController,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Cart
            composable(Screen.Cart.route) {
                CartScreen(navController = navController, viewModel = cartViewModel)
            }
            
            // Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    navController = navController,
                    cartViewModel = cartViewModel
                )
            }
            
            // Categories
            composable(Screen.Categories.route) {
                CategoryScreen(navController = navController)
            }
            
            // Category Products (specific category)
            composable(
                route = Screen.CategoryProducts.route,
                arguments = listOf(
                    navArgument("categorySlug") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categorySlug = backStackEntry.arguments?.getString("categorySlug") ?: ""
                CategoryScreen(
                    navController = navController,
                    categorySlug = categorySlug
                )
            }
            
            // Checkout
            composable(Screen.Checkout.route) {
                CheckoutScreen(navController = navController, cartViewModel = cartViewModel)
            }
            
            // Order Success
            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.skyzonebd.android.ui.order.OrderSuccessScreen(
                    orderId = orderId,
                    navController = navController
                )
            }
            
            // Products (All Products)
            composable(Screen.Products.route) {
                ProductsScreen(navController = navController, cartViewModel = cartViewModel)
            }
            
            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
            
            // Orders
            composable(Screen.Orders.route) {
                OrdersScreen(navController = navController)
            }
            
            // Search
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
        }
    }
}
