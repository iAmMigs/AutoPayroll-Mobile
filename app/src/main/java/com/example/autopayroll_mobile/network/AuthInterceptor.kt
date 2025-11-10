package com.example.autopayroll_mobile.network

import android.content.Context
import com.example.autopayroll_mobile.utils.SessionManager // Import SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    // Get a reference to our SessionManager
    private val sessionManager = SessionManager(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read the token from SessionManager
        // The bug is likely that the token saved in your session is wrong
        val authToken = sessionManager.getToken()

        val newRequestBuilder = originalRequest.newBuilder()

        if (authToken != null) {
            // This logic is correct
            newRequestBuilder.addHeader("Authorization", "Bearer $authToken")
        }

        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }
}