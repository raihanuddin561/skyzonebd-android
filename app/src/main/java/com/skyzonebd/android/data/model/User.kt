package com.skyzonebd.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * User model matching the Next.js Prisma schema
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("companyName")
    val companyName: String? = null,
    
    @SerializedName("role")
    val role: UserRole = UserRole.BUYER,
    
    @SerializedName("userType")
    val userType: UserType = UserType.RETAIL,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("businessInfo")
    val businessInfo: BusinessInfo? = null
)

enum class UserRole {
    @SerializedName("ADMIN")
    ADMIN,
    
    @SerializedName("SELLER")
    SELLER,
    
    @SerializedName("BUYER")
    BUYER
}

enum class UserType {
    @SerializedName("RETAIL")
    RETAIL,     // B2C - Individual/retail customers
    
    @SerializedName("WHOLESALE")
    WHOLESALE,  // B2B - Business/wholesale customers
    
    @SerializedName("GUEST")
    GUEST       // Not logged in
}

data class BusinessInfo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("companyType")
    val companyType: String? = null,
    
    @SerializedName("registrationNumber")
    val registrationNumber: String? = null,
    
    @SerializedName("taxId")
    val taxId: String? = null,
    
    @SerializedName("website")
    val website: String? = null,
    
    @SerializedName("employeeCount")
    val employeeCount: String? = null,
    
    @SerializedName("annualPurchaseVolume")
    val annualPurchaseVolume: String? = null,
    
    @SerializedName("tradeLicenseUrl")
    val tradeLicenseUrl: String? = null,
    
    @SerializedName("taxCertificateUrl")
    val taxCertificateUrl: String? = null,
    
    @SerializedName("verificationStatus")
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    
    @SerializedName("verifiedAt")
    val verifiedAt: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

enum class VerificationStatus {
    @SerializedName("PENDING")
    PENDING,
    
    @SerializedName("APPROVED")
    APPROVED,
    
    @SerializedName("REJECTED")
    REJECTED,
    
    @SerializedName("RESUBMIT")
    RESUBMIT
}

// Auth request/response models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,          // Required by web API
    val companyName: String,    // Required by web API
    val userType: UserType = UserType.RETAIL
)

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("token")
    val token: String? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null
)
