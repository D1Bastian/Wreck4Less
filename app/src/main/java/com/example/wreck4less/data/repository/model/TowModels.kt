package com.example.wreck4less.data.repository.model

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
