package com.example.autopayroll_mobile.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
        val client = createClient(context.applicationContext) // OkHttpClient

        // Create a Gson instance with lenient parsing
        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}