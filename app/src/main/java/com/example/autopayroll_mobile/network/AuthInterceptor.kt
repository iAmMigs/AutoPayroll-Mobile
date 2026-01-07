package com.example.autopayroll_mobile.network

import android.content.Context
import android.util.Log // Import Log
import com.example.autopayroll_mobile.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val sessionManager = SessionManager(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = sessionManager.getToken()

        val newRequestBuilder = originalRequest.newBuilder()

        // 1. Force JSON response
        newRequestBuilder.addHeader("Accept", "application/json")

        // 2. Add Token and LOG IT
        if (!authToken.isNullOrBlank()) {
            Log.d("AuthInterceptor", "Attaching Token: Bearer $authToken")
            newRequestBuilder.addHeader("Authorization", "Bearer $authToken")
        } else {
            Log.e("AuthInterceptor", "CRITICAL: No Token found in SessionManager!")
        }

        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }
}