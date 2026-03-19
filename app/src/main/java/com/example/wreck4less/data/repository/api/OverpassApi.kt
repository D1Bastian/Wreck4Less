package com.example.wreck4less.data.repository.api

import com.example.wreck4less.data.repository.model.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApi {
    @GET("api/interpreter")
    suspend fun search(@Query("data") query: String): OverpassResponse
}
