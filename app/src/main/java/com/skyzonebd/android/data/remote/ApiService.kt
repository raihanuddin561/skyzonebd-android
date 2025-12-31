package com.skyzonebd.android.data.remote

import com.skyzonebd.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service interface for SkyzoneBD backend
 * Base URL: https://skyzonebd.vercel.app/api/
 */
interface ApiService {
    
    // ==================== Authentication ====================
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    // ==================== Products ====================
    
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("category") categorySlug: String? = null,
        @Query("search") search: String? = null,
        @Query("featured") featured: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("brand") brand: String? = null,
        @Query("sortBy") sortBy: String? = null
    ): Response<ApiResponse<ProductsResponse>>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<ApiResponse<ProductDetailResponse>>
    
    @GET("products/slug/{slug}")
    suspend fun getProductBySlug(@Path("slug") slug: String): Response<ApiResponse<ProductDetailResponse>>
    
    @GET("products")
    suspend fun getFeaturedProducts(
        @Query("featured") featured: String = "true",
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<ProductsResponse>>
    
    // ==================== Categories ====================
    
    @GET("categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>
    
    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): Response<ApiResponse<Category>>
    
    // ==================== Search ====================
    
    @GET("products")
    suspend fun searchProducts(
        @Query("search") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 12
    ): Response<ApiResponse<ProductsResponse>>
    
    // ==================== Orders ====================
    
    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<OrdersResponse>
    
    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<Order>
    
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<CreateOrderResponse>>
    
    @PUT("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<ApiResponse<Order>>
    
    // ==================== RFQ (Request for Quote) ====================
    
    @GET("rfq")
    suspend fun getRFQs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<RFQ>>>
    
    @GET("rfq/{id}")
    suspend fun getRFQ(@Path("id") id: String): Response<ApiResponse<RFQ>>
    
    @POST("rfq")
    suspend fun createRFQ(@Body request: CreateRFQRequest): Response<ApiResponse<RFQ>>
    
    // ==================== Hero Slides ====================
    
    @GET("hero-slides")
    suspend fun getHeroSlides(): Response<ApiResponse<List<HeroSlide>>>
    
    // ==================== User Profile ====================
    
    @PUT("auth/profile")
    suspend fun updateProfile(@Body user: User): Response<ApiResponse<User>>
    
    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Unit>>
    
    // ==================== Addresses ====================
    
    @GET("auth/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<Address>>>
    
    @POST("auth/addresses")
    suspend fun createAddress(@Body address: Address): Response<ApiResponse<Address>>
    
    @PUT("auth/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body address: Address
    ): Response<ApiResponse<Address>>
    
    @DELETE("auth/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<ApiResponse<Unit>>
}

// Request models
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// Generic API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)
