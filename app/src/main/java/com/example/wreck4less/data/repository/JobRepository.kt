package com.example.wreck4less.data.repository

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * JobRepository handles the networking logic.
 * This is where Retrofit interfaces with your Python Backend.
 */
interface JobRepository {
    @POST("v1/jobs/request")
    suspend fun createJob(@Body request: TowRequest): Response<JobResponse>
}

data class TowRequest(
    val customer_id: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val vehicle_type: String = "sedan"
)

data class JobResponse(
    val job_id: String,
    val stripe_client_secret: String
)