package com.skyzonebd.android.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT authentication token to requests
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider()
        
        return if (!token.isNullOrEmpty()) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}
