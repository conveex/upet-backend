package com.upet.auth

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RegisterUserRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null,
    val mainAddress: String? = null,
    val isClient: Boolean = true,
    val isWalker: Boolean = false
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthUserResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val mainAddress: String?,
    val photoUrl: String?,
    val emailVerified: Boolean,
    val isClient: Boolean,
    val isWalker: Boolean,
    val isAdmin: Boolean,
    val status: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: AuthUserResponse? = null
)

@Serializable
data class RegisterWalkerRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null,
    val mainAddress: String? = null,
    val bio: String? = null,
    val experience: String? = null,
    val serviceZoneLabel: String,
    val serviceCenterLat: Double,
    val serviceCenterLng: Double,
    val zoneRadiusKm: Double,
    val maxDogsPerWalk: Int = 1
)