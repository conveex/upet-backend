package com.cnvx.pets

import kotlinx.serialization.Serializable

@Serializable
data class PetResponse(
    val id: String,
    val ownerId: String,
    val name: String,
    val breed: String? = null,
    val color: String? = null,
    val size: String,
    val age: Int? = null,
    val behavior: String? = null,
    val photoUrl: String? = null,
    val active: Boolean
)

@Serializable
data class CreatePetRequest(
    val ownerId: String,
    val name: String,
    val breed: String? = null,
    val color: String? = null,
    val size: String,
    val age: Int? = null,
    val behavior: String? = null,
    val photoUrl: String? = null
)

@Serializable
data class UpdatePetRequest(
    val name: String,
    val breed: String? = null,
    val color: String? = null,
    val size: String,
    val age: Int? = null,
    val behavior: String? = null,
    val photoUrl: String? = null,
    val active: Boolean
)
