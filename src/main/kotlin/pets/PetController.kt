package com.upet.pets

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class PetController(
    private val petService: PetService
) {
    suspend fun createPet(call: ApplicationCall, ownerId: UUID) {
        val request = call.receive<CreatePetRequest>()
        val pet = petService.createPet(ownerId, request)
        call.respond(
            HttpStatusCode.Created,
            PetEnvelope(
                success = true,
                message = "Mascota creada correctamente.",
                pet = pet
            )
        )
    }

    suspend fun getMyPets(call: ApplicationCall, ownerId: UUID) {
        val pets = petService.getMyPets(ownerId)
        call.respond(
            HttpStatusCode.OK,
            PetListEnvelope(
                success = true,
                pets = pets
            )
        )
    }

    suspend fun getPetById(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val pet = petService.getPetById(ownerId, petId)
        if (pet == null) {
            call.respond(
                HttpStatusCode.NotFound,
                PetEnvelope(
                    success = false,
                    message = "Mascota no encontrada.",
                    pet = null
                )
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                PetEnvelope(
                    success = true,
                    message = "Mascota encontrada.",
                    pet = pet
                )
            )
        }
    }

    suspend fun updatePet(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val request = call.receive<UpdatePetRequest>()
        val pet = petService.updatePet(ownerId, petId, request)
        if (pet == null) {
            call.respond(
                HttpStatusCode.NotFound,
                PetEnvelope(
                    success = false,
                    message = "Mascota no encontrada.",
                    pet = null
                )
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                PetEnvelope(
                    success = true,
                    message = "Mascota actualizada correctamente.",
                    pet = pet
                )
            )
        }
    }

    suspend fun deletePet(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val deleted = petService.deletePet(ownerId, petId)
        if (!deleted) {
            call.respond(
                HttpStatusCode.NotFound,
                PetEnvelope(
                    success = false,
                    message = "Mascota no encontrada.",
                    pet = null
                )
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                PetEnvelope(
                    success = true,
                    message = "Mascota eliminada correctamente.",
                    pet = null
                )
            )
        }
    }
}