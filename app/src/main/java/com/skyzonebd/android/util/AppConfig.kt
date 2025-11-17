package com.skyzonebd.android.util

/**
 * Centralized app configuration
 * Contains all static information and constants used across the app
 */
object AppConfig {
    
    // Company Information
    const val COMPANY_NAME = "SkyZone BD"
    const val COMPANY_PHONE = "+8801918744551"
    const val COMPANY_EMAIL = "info@skyzonebd.com"
    const val COMPANY_ADDRESS = "Dhaka, Bangladesh"
    
    // Website & Social Links
    const val WEBSITE_URL = "https://skyzonebd.vercel.app"
    const val FACEBOOK_URL = "https://facebook.com/skyzonebd"
    const val INSTAGRAM_URL = "https://instagram.com/skyzonebd"
    const val TWITTER_URL = "https://twitter.com/skyzonebd"
    
    // API Configuration
    const val API_BASE_URL = "https://skyzonebd.vercel.app/api/"
    const val API_TIMEOUT = 30L // seconds
    
    // App Information
    const val APP_VERSION = "1.0.0"
    const val APP_BUILD = "1"
    
    // Support & Legal
    const val PRIVACY_POLICY_URL = "$WEBSITE_URL/privacy-policy"
    const val TERMS_URL = "$WEBSITE_URL/terms"
    const val REFUND_POLICY_URL = "$WEBSITE_URL/refund-policy"
    
    // Business Rules
    const val MIN_ORDER_AMOUNT = 0.0
    const val SHIPPING_FEE = 0.0
    const val TAX_RATE = 0.0
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val PRODUCTS_PAGE_SIZE = 20
    const val ORDERS_PAGE_SIZE = 10
    
    // Cache & Storage
    const val IMAGE_CACHE_SIZE = 50 * 1024 * 1024L // 50 MB
    const val CART_PREFERENCE_KEY = "cart_items"
    const val AUTH_TOKEN_KEY = "auth_token"
    
    // UI Configuration
    const val SHOW_STOCK_QUANTITY = false // Never show exact stock numbers to users
    const val CAROUSEL_AUTO_SCROLL_DELAY = 3000L // milliseconds
    const val SEARCH_DEBOUNCE_DELAY = 300L // milliseconds
}
