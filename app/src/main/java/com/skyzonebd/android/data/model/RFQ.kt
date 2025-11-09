package com.skyzonebd.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request for Quote (RFQ) - B2B Feature
 */
data class RFQ(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("rfqNumber")
    val rfqNumber: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("subject")
    val subject: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("targetPrice")
    val targetPrice: Double? = null,
    
    @SerializedName("status")
    val status: RFQStatus = RFQStatus.PENDING,
    
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("items")
    val items: List<RFQItem> = emptyList()
)

data class RFQItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("rfqId")
    val rfqId: String,
    
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("product")
    val product: Product? = null
)

enum class RFQStatus {
    @SerializedName("PENDING")
    PENDING,
    
    @SerializedName("QUOTED")
    QUOTED,
    
    @SerializedName("ACCEPTED")
    ACCEPTED,
    
    @SerializedName("REJECTED")
    REJECTED,
    
    @SerializedName("EXPIRED")
    EXPIRED
}

data class CreateRFQRequest(
    val subject: String,
    val message: String? = null,
    val targetPrice: Double? = null,
    val items: List<CreateRFQItem>
)

data class CreateRFQItem(
    val productId: String,
    val quantity: Int,
    val notes: String? = null
)

data class HeroSlide(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("subtitle")
    val subtitle: String? = null,
    
    @SerializedName("imageUrl")
    val imageUrl: String,
    
    @SerializedName("linkUrl")
    val linkUrl: String? = null,
    
    @SerializedName("productId")
    val productId: String? = null,
    
    @SerializedName("buttonText")
    val buttonText: String = "Shop Now",
    
    @SerializedName("position")
    val position: Int = 0,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("bgColor")
    val bgColor: String = "#3B82F6",
    
    @SerializedName("textColor")
    val textColor: String = "#FFFFFF",
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("product")
    val product: Product? = null
)
