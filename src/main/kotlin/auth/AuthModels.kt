package com.cnvx.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterOwnerRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class OwnerResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val mainAddress: String? = null,
    val profilePhotoUrl: String? = null
)

@Serializable
data class AuthResponse(
    val owner: OwnerResponse,
    val token: String
)