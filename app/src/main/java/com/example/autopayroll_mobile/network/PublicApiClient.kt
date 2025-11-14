package com.example.autopayroll_mobile.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A NEW, separate Retrofit client that does NOT send an
 * authentication token. This is for public API routes.
 */
object PublicApiClient {

    private const val BASE_URL = "https://autopayroll.org/"

    // This client is simple, it has NO interceptors
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // Use the simple public client
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // This creates a public version of the ApiService
    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // A simple getter for the public service
    fun getService(): ApiService {
        return apiService
    }
}