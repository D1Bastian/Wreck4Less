package com.example.wreck4less.data.model

data class WreckIntel(
    val make: String,
    val model: String,
    val year: String,
    val damage_description: String,
    val image_keys: List<String>,
    val contact_phone: String? = null,
    val location_label: String,
    val location_lat: Double? = null,
    val location_lng: Double? = null
)

data class WreckSubmission(
    val intel: WreckIntel
)

data class WreckResponse(
    val job_id: String,
    val status: String,
    val ops_timer: String,
    val support_line: String
)
