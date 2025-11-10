package com.example.autopayroll_mobile.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Make sure this is http, not https, if your server doesn't support SSL
    // Or if you are testing on a local emulator, it should be http://10.0.2.2
    private const val BASE_URL = "https://autopayroll.org/"

    // This function correctly creates the client with the interceptor
    private fun createClient(context: Context): OkHttpClient {
        val authInterceptor = AuthInterceptor(context.applicationContext)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    // This function correctly builds Retrofit with the custom client
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