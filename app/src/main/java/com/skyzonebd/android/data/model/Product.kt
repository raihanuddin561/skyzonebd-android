package com.skyzonebd.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Product model matching the Next.js Prisma schema
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    // Images (Vercel Blob URLs)
    @SerializedName("imageUrl")
    val imageUrl: String,
    
    @SerializedName("imageUrls")
    val imageUrls: List<String> = emptyList(),
    
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    
    // Product Details
    @SerializedName("brand")
    val brand: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("specifications")
    val specifications: Map<String, Any>? = null,
    
    // B2C Pricing
    @SerializedName("retailPrice")
    val retailPrice: Double,
    
    @SerializedName("salePrice")
    val salePrice: Double? = null,
    
    @SerializedName("retailMOQ")
    val retailMOQ: Int = 1,
    
    @SerializedName("comparePrice")
    val comparePrice: Double? = null,
    
    // B2B Pricing
    @SerializedName("wholesaleEnabled")
    val wholesaleEnabled: Boolean = false,
    
    @SerializedName("wholesaleMOQ")
    val wholesaleMOQ: Int = 5,
    
    @SerializedName("baseWholesalePrice")
    val baseWholesalePrice: Double? = null,
    
    // Legacy fields
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("wholesalePrice")
    val wholesalePrice: Double? = null,
    
    @SerializedName("minOrderQuantity")
    val minOrderQuantity: Int = 1,
    
    // Inventory
    @SerializedName("stockQuantity")
    val stockQuantity: Int = 0,
    
    @SerializedName("availability")
    val availability: String = "in_stock",
    
    @SerializedName("sku")
    val sku: String? = null,
    
    // Seller Information
    @SerializedName("sellerId")
    val sellerId: String? = null,
    
    // Metadata
    @SerializedName("categoryId")
    val categoryId: String? = null,
    
    @SerializedName("categorySlug")
    val categorySlug: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("isFeatured")
    val isFeatured: Boolean = false,
    
    @SerializedName("rating")
    val rating: Double? = null,
    
    @SerializedName("reviewCount")
    val reviewCount: Int = 0,
    
    // SEO
    @SerializedName("metaTitle")
    val metaTitle: String? = null,
    
    @SerializedName("metaDescription")
    val metaDescription: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
    
    // Note: category field removed from JSON parsing to avoid conflicts
    // API sometimes returns it as string (ID) and sometimes as object
    // Use categoryId field instead which is always a string
    
    // Relations
    // Category is commented out because API returns it inconsistently (string vs object)
    // @SerializedName("category")
    // val category: Category? = null
) {
    // Provide wholesaleTiers as a property with default empty list
    // to avoid parsing issues if API doesn't include it
    val wholesaleTiers: List<WholesaleTier> get() = emptyList()
    
    // Helper properties for backward compatibility
    val images: List<String> get() = if (imageUrls.isNotEmpty()) imageUrls else listOf(imageUrl)
    val stock: Int get() = stockQuantity
    val moq: Int? get() = if (wholesaleEnabled) wholesaleMOQ else retailMOQ
    val dimensions: String? get() = null // Can be added if needed
    val weight: Int? get() = null // Can be added if needed
    
    // Helper functions
    fun getDisplayPrice(userType: UserType, quantity: Int = 1): Double {
        return when {
            userType == UserType.WHOLESALE && wholesaleEnabled -> {
                getWholesalePrice(quantity)
            }
            salePrice != null -> salePrice
            else -> retailPrice
        }
    }
    
    fun getWholesalePrice(quantity: Int): Double {
        if (!wholesaleEnabled || wholesaleTiers.isEmpty()) {
            return baseWholesalePrice ?: retailPrice
        }
        
        // Find the appropriate tier based on quantity
        val applicableTier = wholesaleTiers
            .filter { quantity >= it.minQuantity }
            .filter { it.maxQuantity == null || quantity <= it.maxQuantity }
            .maxByOrNull { it.minQuantity }
        
        return applicableTier?.price ?: baseWholesalePrice ?: retailPrice
    }
    
    fun getDiscountPercentage(): Int? {
        return if (salePrice != null && salePrice < retailPrice) {
            ((retailPrice - salePrice) / retailPrice * 100).toInt()
        } else null
    }
    
    fun isInStock(): Boolean = availability == "in_stock" && stockQuantity > 0
}

data class WholesaleTier(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("minQuantity")
    val minQuantity: Int,
    
    @SerializedName("maxQuantity")
    val maxQuantity: Int? = null,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("discount")
    val discount: Double,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ProductsResponse(
    @SerializedName("products")
    val products: List<Product>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null,
    
    @SerializedName("categories")
    val categories: List<Category>? = null,
    
    // Legacy fields for backward compatibility
    @SerializedName("total")
    val total: Int? = null,
    
    @SerializedName("page")
    val page: Int? = null,
    
    @SerializedName("limit")
    val limit: Int? = null,
    
    @SerializedName("totalPages")
    val totalPages: Int? = null
)

data class Pagination(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("hasNext")
    val hasNext: Boolean,
    
    @SerializedName("hasPrev")
    val hasPrev: Boolean
)

data class ProductDetailResponse(
    @SerializedName("product")
    val product: Product,
    
    @SerializedName("relatedProducts")
    val relatedProducts: List<Product>? = null
)
