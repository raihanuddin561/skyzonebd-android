package com.skyzonebd.android.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    
    @SerializedName(value = "orderId", alternate = ["orderNumber", "order_number"])
    val orderNumber: String = "",
    
    @SerializedName(value = "userId", alternate = ["user_id"])
    val userId: String? = null,
    
    // Guest order fields
    @SerializedName(value = "guestEmail", alternate = ["guest_email"])
    val guestEmail: String? = null,
    
    @SerializedName(value = "guestName", alternate = ["guest_name"])
    val guestName: String? = null,
    
    @SerializedName(value = "guestPhone", alternate = ["guest_phone"])
    val guestPhone: String? = null,
    
    @SerializedName(value = "guestCompany", alternate = ["guest_company"])
    val guestCompany: String? = null,
    
    // Order Details
    @SerializedName(value = "subtotal", alternate = ["sub_total"])
    val subtotal: Double = 0.0,
    
    @SerializedName("tax")
    val tax: Double = 0.0,
    
    @SerializedName("shipping")
    val shipping: Double = 0.0,
    
    @SerializedName("total")
    val total: Double = 0.0,
    
    // Status - backend sends lowercase strings
    @SerializedName("status")
    val statusString: String = "pending",
    
    @SerializedName(value = "paymentStatus", alternate = ["payment_status"])
    val paymentStatusString: String? = null,
    
    @SerializedName(value = "paymentMethod", alternate = ["payment_method"])
    val paymentMethod: String = "",
    
    // Addresses
    @SerializedName(value = "shippingAddress", alternate = ["shipping_address"])
    val shippingAddress: String = "",
    
    @SerializedName(value = "billingAddress", alternate = ["billing_address"])
    val billingAddress: String = "",
    
    // Notes
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String = "",
    
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String = "",
    
    // Relations
    @SerializedName(value = "items", alternate = ["orderItems", "order_items"])
    val orderItems: List<OrderItem> = emptyList(),
    
    @SerializedName("payments")
    val payments: List<Payment> = emptyList()
) {
    // Computed properties for enum values (backend sends lowercase strings)
    val status: OrderStatus
        get() = try {
            OrderStatus.valueOf(statusString.uppercase())
        } catch (e: Exception) {
            OrderStatus.PENDING
        }
    
    val paymentStatus: PaymentStatus
        get() = try {
            PaymentStatus.valueOf((paymentStatusString ?: "pending").uppercase())
        } catch (e: Exception) {
            PaymentStatus.PENDING
        }
}

data class OrderItem(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName(value = "orderId", alternate = ["order_id"])
    val orderId: String = "",
    
    @SerializedName(value = "productId", alternate = ["product_id"])
    val productId: String = "",
    
    @SerializedName("name")
    val name: String = "",  // Product name returned directly in order items
    
    @SerializedName("quantity")
    val quantity: Int = 0,
    
    @SerializedName("price")
    val price: Double = 0.0,
    
    @SerializedName("total")
    val total: Double = 0.0,
    
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String = "",
    
    @SerializedName("product")
    val product: SimpleProduct? = null
)

// Simplified product for order items
data class SimpleProduct(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName(value = "imageUrl", alternate = ["image_url"])
    val imageUrl: String? = null,
    
    @SerializedName("price")
    val price: Double = 0.0
)

enum class OrderStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("confirmed")
    CONFIRMED,
    
    @SerializedName("processing")
    PROCESSING,
    
    @SerializedName("shipped")
    SHIPPED,
    
    @SerializedName("delivered")
    DELIVERED,
    
    @SerializedName("cancelled")
    CANCELLED,
    
    @SerializedName("returned")
    RETURNED
}

enum class PaymentStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("paid")
    PAID,
    
    @SerializedName("failed")
    FAILED,
    
    @SerializedName("refunded")
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

// Guest info for order creation
data class GuestInfo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("mobile")
    val mobile: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("companyName")
    val companyName: String? = null
)

data class CreateOrderRequest(
    @SerializedName("items")
    val items: List<CreateOrderItem>,
    
    @SerializedName("shippingAddress")
    val shippingAddress: String,
    
    @SerializedName("billingAddress")
    val billingAddress: String,
    
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    // For guest orders - send as nested object to match web API
    @SerializedName("guestInfo")
    val guestInfo: GuestInfo? = null
)

data class CreateOrderItem(
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("name")
    val name: String  // Product name - required by backend
)

data class OrdersResponse(
    @SerializedName("orders")
    val orders: List<Order>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

// Response wrapper for createOrder endpoint which returns nested { order, message }
data class CreateOrderResponse(
    @SerializedName("order")
    val order: Order,
    
    @SerializedName("message")
    val message: String? = null
)
