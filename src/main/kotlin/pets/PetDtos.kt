package com.upet.pets

import com.upet.domain.model.PetSize
import kotlinx.serialization.Serializable

@Serializable
data class PetResponse(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val color: String? = null,
    val size: PetSize,
    val age: Int? = null,
    val behavior: String? = null,
    val specialConditions: String? = null,
    val photoUrl: String? = null,
    val createdAt: kotlinx.datetime.LocalDateTime,
    val updatedAt: kotlinx.datetime.LocalDateTime
)

@Serializable
data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String? = null,
    val color: String? = null,
    val size: PetSize,
    val age: Int,
    val behavior: String? = null,
    val specialConditions: String? = null,
    val photoUrl: String? = null
)

@Serializable
data class UpdatePetRequest(
    val name: String? = null,
    val species: String? = null,
    val breed: String? = null,
    val color: String? = null,
    val size: PetSize? = null,
    val age: Int? = null,
    val behavior: String? = null,
    val specialConditions: String? = null,
    val photoUrl: String? = null
)