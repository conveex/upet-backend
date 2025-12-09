package com.upet.walkers

import kotlinx.serialization.Serializable

@Serializable
data class WalkerUserResponse(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String?,
    val status: String
)

@Serializable
data class WalkerProfileResponse(
    val id: String,
    val userId: String,
    val bio: String?,
    val experience: String?,
    val serviceZoneLabel: String,
    val ratingAverage: Double,
    val totalReviews: Int,
    val maxDogs: Int,
    val serviceCenterLat: Double,
    val serviceCenterLng: Double,
    val zoneRadiusKm: Double,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class WalkerSelfEnvelope(
    val success: Boolean,
    val message: String,
    val user: WalkerUserResponse?,
    val profile: WalkerProfileResponse?
)

@Serializable
data class UpdateWalkerProfileRequest(
    val bio: String? = null,
    val experience: String? = null,
    val serviceZoneLabel: String? = null,
    val maxDogs: Int? = null,
    val serviceCenterLat: Double? = null,
    val serviceCenterLng: Double? = null,
    val zoneRadiusKm: Double? = null
)

@Serializable
data class UpdateWalkerPhotoRequest(
    val photoUrl: String
)