package com.heroesofpenta.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()

        // 1. Get current token
        val currentToken = tokenProvider.getToken()

        // 2. Add token to the request headers if present
        if (!currentToken.isNullOrEmpty()) {
            originalRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        val response = chain.proceed(originalRequest)

        // 3. If unauthorized, try refreshing the token (if you have a refresh flow)
        if (response.code == 401) {
            response.close() // close this response to avoid leaks

            val newToken = refreshToken() // calls server /api/refresh or similar
            return if (!newToken.isNullOrEmpty()) {
                // Retry the original request with new token
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                chain.proceed(newRequest)
            } else {
                // If refresh fails, we can handle forced logout or return the 401
                response
            }
        }

        return response
    }

    private fun refreshToken(): String? {
        // 1. Make a synchronous call to a refresh endpoint
        // 2. If successful, store new token via tokenProvider.saveToken()
        // 3. Return new token or null if fail
        return tokenProvider.refreshToken()
    }
}
