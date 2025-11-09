package com.skyzonebd.android.ui.navigation

sealed class Screen(val route: String) {
    // Main screens
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Products : Screen("products")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    object Categories : Screen("categories")
    object CategoryProducts : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }
    object Search : Screen("search")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    
    // Auth screens
    object Login : Screen("login")
    object Register : Screen("register")
    
    // User screens
    object Profile : Screen("profile")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(orderId: String) = "order/$orderId"
    }
    object Wishlist : Screen("wishlist")
    object Addresses : Screen("addresses")
    
    // B2B screens
    object Wholesale : Screen("wholesale")
    object RFQ : Screen("rfq")
    object RFQCreate : Screen("rfq/create")
    object RFQDetail : Screen("rfq/{rfqId}") {
        fun createRoute(rfqId: String) = "rfq/$rfqId"
    }
}
