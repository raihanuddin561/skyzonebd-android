package com.skyzonebd.android.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("orderNumber")
    val orderNumber: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    // Guest order fields
    @SerializedName("guestEmail")
    val guestEmail: String? = null,
    
    @SerializedName("guestName")
    val guestName: String? = null,
    
    @SerializedName("guestPhone")
    val guestPhone: String? = null,
    
    @SerializedName("guestCompany")
    val guestCompany: String? = null,
    
    // Order Details
    @SerializedName("subtotal")
    val subtotal: Double,
    
    @SerializedName("tax")
    val tax: Double = 0.0,
    
    @SerializedName("shipping")
    val shipping: Double = 0.0,
    
    @SerializedName("total")
    val total: Double,
    
    // Status
    @SerializedName("status")
    val status: OrderStatus = OrderStatus.PENDING,
    
    @SerializedName("paymentStatus")
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    
    // Addresses
    @SerializedName("shippingAddress")
    val shippingAddress: String,
    
    @SerializedName("billingAddress")
    val billingAddress: String,
    
    // Notes
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    // Relations
    @SerializedName("orderItems")
    val orderItems: List<OrderItem> = emptyList(),
    
    @SerializedName("payments")
    val payments: List<Payment> = emptyList()
)

data class OrderItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("total")
    val total: Double,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("product")
    val product: Product? = null
)

enum class OrderStatus {
    @SerializedName("PENDING")
    PENDING,
    
    @SerializedName("CONFIRMED")
    CONFIRMED,
    
    @SerializedName("PROCESSING")
    PROCESSING,
    
    @SerializedName("SHIPPED")
    SHIPPED,
    
    @SerializedName("DELIVERED")
    DELIVERED,
    
    @SerializedName("CANCELLED")
    CANCELLED,
    
    @SerializedName("RETURNED")
    RETURNED
}

enum class PaymentStatus {
    @SerializedName("PENDING")
    PENDING,
    
    @SerializedName("PAID")
    PAID,
    
    @SerializedName("FAILED")
    FAILED,
    
    @SerializedName("REFUNDED")
    REFUNDED
}

enum class PaymentMethod {
    @SerializedName("BANK_TRANSFER")
    BANK_TRANSFER,
    
    @SerializedName("CASH_ON_DELIVERY")
    CASH_ON_DELIVERY,
    
    @SerializedName("BKASH")
    BKASH,
    
    @SerializedName("NAGAD")
    NAGAD,
    
    @SerializedName("ROCKET")
    ROCKET,
    
    @SerializedName("CREDIT_CARD")
    CREDIT_CARD,
    
    @SerializedName("INVOICE_NET30")
    INVOICE_NET30,
    
    @SerializedName("INVOICE_NET60")
    INVOICE_NET60
}

data class Payment(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("method")
    val method: PaymentMethod,
    
    @SerializedName("status")
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    @SerializedName("transactionId")
    val transactionId: String? = null,
    
    @SerializedName("gateway")
    val gateway: String? = null,
    
    @SerializedName("gatewayRef")
    val gatewayRef: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class CreateOrderRequest(
    val items: List<CreateOrderItem>,
    val shippingAddress: String,
    val billingAddress: String,
    val paymentMethod: String,
    val notes: String? = null,
    // For guest orders
    val guestEmail: String? = null,
    val guestName: String? = null,
    val guestPhone: String? = null,
    val guestCompany: String? = null
)

data class CreateOrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double
)

data class OrdersResponse(
    @SerializedName("orders")
    val orders: List<Order>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)
