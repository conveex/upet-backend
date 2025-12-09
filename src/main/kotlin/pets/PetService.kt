package com.upet.pets

import java.util.UUID

class PetService(
    private val petRepository: PetRepository
) {
    fun createPet(ownerId: UUID, request: CreatePetRequest): PetResponse =
        petRepository.create(ownerId, request)

    fun getMyPets(ownerId: UUID): List<PetResponse> =
        petRepository.findByOwner(ownerId)

    fun getPetById(ownerId: UUID, petId: UUID): PetResponse? =
        petRepository.findById(ownerId, petId)

    fun updatePet(ownerId: UUID, petId: UUID, request: UpdatePetRequest): PetResponse? =
        petRepository.update(ownerId, petId, request)

    fun deletePet(ownerId: UUID, petId: UUID): Boolean =
        petRepository.delete(ownerId, petId)
}