package com.upet.walks

import kotlinx.serialization.Serializable

@Serializable
data class StartWalkRequest(
    val startPhotoUrl: String,
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Double? = null
)

@Serializable
data class EndWalkRequest(
    val endPhotoUrl: String,
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Double? = null
)