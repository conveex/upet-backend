package com.upet.tracking

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TrackingPointRequest(
    val lat: Double,
    val lng: Double,
    val timestampMillis: Long? = null,
    val accuracyMeters: Double? = null,
    val speedMetersPerSecond: Double? = null,
    val batteryLevel: Int? = null,
    val isManual: Boolean = false
)

@Serializable
data class TrackingSummaryResponse(
    val walkId: String,
    val totalDistanceMeters: Int? = null,
    val totalDurationSeconds: Int? = null,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val createdAt: LocalDateTime? = null
)

@Serializable
data class TrackingSummaryEnvelope(
    val success: Boolean,
    val message: String,
    val summary: TrackingSummaryResponse? = null
)

@Serializable
data class TrackingAckEnvelope(
    val success: Boolean,
    val message: String
)

data class TrackSummaryRow(
    val id: String,
    val walkId: String,
    val totalDistanceMeters: Int?,
    val totalDurationSeconds: Int?,
    val startLat: Double?,
    val startLng: Double?,
    val endLat: Double?,
    val endLng: Double?,
    val createdAt: LocalDateTime
)