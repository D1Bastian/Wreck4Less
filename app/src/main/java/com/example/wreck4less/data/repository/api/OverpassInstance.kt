package com.example.wreck4less.data.repository.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OverpassInstance {
    private const val BASE_URL = "https://overpass-api.de/"

    val api: OverpassApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApi::class.java)
    }
}
