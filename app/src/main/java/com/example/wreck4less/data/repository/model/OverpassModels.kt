package com.example.wreck4less.data.repository.model

data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList()
)

data class OverpassElement(
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Map<String, String>? = emptyMap()
)
