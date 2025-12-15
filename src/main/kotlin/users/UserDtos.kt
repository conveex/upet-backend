package com.upet.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserPhotoRequest(
    val photoUrl: String
)

@Serializable
data class UpdateUserProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val mainAddress: String? = null
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val name: String?,
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
data class UserProfileEnvelope(
    val success: Boolean,
    val message: String,
    val user: UserProfileResponse?
)

@Serializable
data class UpdateFcmTokenRequest(
    val token: String
)

@Serializable
data class SimpleEnvelope(
    val success: Boolean,
    val message: String
)