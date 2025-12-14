package com.upet.walks

import com.upet.domain.model.WalkSource
import com.upet.domain.model.WalkStatus
import com.upet.domain.model.WalkType
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LatLngDto(
    val lat: Double,
    val lng: Double
)

@Serializable
data class RoutePointDto(
    val lat: Double,
    val lng: Double
)

@Serializable
data class CalculatedRouteDto(
    val polylineEncoded: String,
    val distanceKm: Double,
    val durationMin: Int,
    val priceAmount: Double = 0.0,
    val priceCurrency: String = "MXN"
)

@Serializable
data class CalculateRouteRequest(
    val type: WalkType,
    val origin: LatLngDto,
    val destination: LatLngDto? = null,
    val timeMinutes: Int? = null,
    val distanceKm: Double? = null
)

@Serializable
data class CalculateRouteResponse(
    val success: Boolean,
    val routes: List<CalculatedRouteDto>
)

@Serializable
data class CreateWalkRequest(
    val type: WalkType,
    val origin: LatLngDto? = null,
    val destination: LatLngDto? = null,

    val pickup: LatLngDto? = null,
    val dropoff: LatLngDto? = null,

    val estimatedDistanceMeters: Int? = null,
    val estimatedDurationSeconds: Int? = null,

    val selectedRoutePolylineEncoded: String,

    val requestedStartTime: LocalDateTime,

    val predefinedRouteId: String? = null,

    val petIds: List<String>,
    val paymentMethodIds: List<String>
)

@Serializable
data class WalkSummaryResponse(
    val id: String,
    val type: WalkType,
    val status: WalkStatus,
    val requestedStartTime: LocalDateTime,
    val estimatedDistanceMeters: Int? = null,
    val estimatedDurationSeconds: Int? = null,
    val priceAmount: Double,
    val priceCurrency: String
)

@Serializable
data class WalkDetailResponse(
    val id: String,
    val clientId: String,
    val walkerId: String? = null,
    val predefinedRouteId: String? = null,
    val type: WalkType,
    val source: WalkSource,
    val status: WalkStatus,

    val origin: LatLngDto? = null,
    val destination: LatLngDto? = null,
    val pickup: LatLngDto? = null,
    val dropoff: LatLngDto? = null,

    val estimatedDistanceMeters: Int? = null,
    val estimatedDurationSeconds: Int? = null,

    val selectedRoutePolylineEncoded: String? = null,

    val requestedStartTime: LocalDateTime,
    val actualStartTime: LocalDateTime? = null,
    val actualEndTime: LocalDateTime? = null,

    val priceAmount: Double,
    val priceCurrency: String,

    val selectedPaymentMethodId: String? = null,
    val agreedPaymentMethodId: String? = null,

    val chatThreadId: String? = null,
    val trackingId: String? = null,

    val petIds: List<String>,
    val paymentMethodIds: List<String>,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
data class WalkEnvelope(
    val success: Boolean,
    val message: String,
    val walk: WalkDetailResponse? = null
)

@Serializable
data class WalkSummaryListEnvelope(
    val success: Boolean,
    val walks: List<WalkSummaryResponse>
)

@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String
)