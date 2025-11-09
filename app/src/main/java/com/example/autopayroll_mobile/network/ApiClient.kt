// In com/example/autopayroll_mobile/network/ApiClient.kt
package com.example.autopayroll_mobile.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://autopayroll.org/"

    // Keep your createClient function
    private fun createClient(context: Context): OkHttpClient {
        val authInterceptor = AuthInterceptor(context)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    // Create a new function to build the Retrofit instance
    fun getClient(context: Context): ApiService {
        val client = createClient(context) // Get the OkHttpClient with the interceptor

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use our custom client
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}