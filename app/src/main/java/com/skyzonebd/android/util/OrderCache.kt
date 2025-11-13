package com.skyzonebd.android.util

import com.skyzonebd.android.data.model.Order

/**
 * Simple singleton to temporarily store the last created order
 * Used to pass order details between Checkout and OrderSuccess screens
 */
object OrderCache {
    private var lastOrder: Order? = null
    
    fun setLastOrder(order: Order) {
        lastOrder = order
    }
    
    fun getLastOrder(): Order? {
        return lastOrder
    }
    
    fun clearLastOrder() {
        lastOrder = null
    }
}
