package com.upet.pets

import com.upet.domain.model.PetSize
import kotlinx.datetime.LocalDateTime
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
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
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

@Serializable
data class PetEnvelope(
    val success: Boolean,
    val message: String,
    val pet: PetResponse? = null
)

@Serializable
data class PetListEnvelope(
    val success: Boolean,
    val pets: List<PetResponse>
)
