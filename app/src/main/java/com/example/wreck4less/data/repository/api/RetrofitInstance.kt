package com.example.wreck4less.data.repository.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.example.wreck4less.BuildConfig

object RetrofitInstance {

    // Android emulator localhost
    private const val BASE_URL = BuildConfig.API_BASE_URL

    val api: WreckApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(WreckApi::class.java)
    }
}
