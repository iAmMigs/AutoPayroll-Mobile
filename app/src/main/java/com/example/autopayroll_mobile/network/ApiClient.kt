package com.example.autopayroll_mobile.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://autopayroll.org/"

    private fun createClient(context: Context): OkHttpClient {
        val authInterceptor = AuthInterceptor(context.applicationContext)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    fun getClient(context: Context): ApiService {
        val client = createClient(context.applicationContext) // Get the OkHttpClient

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use our custom client
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}