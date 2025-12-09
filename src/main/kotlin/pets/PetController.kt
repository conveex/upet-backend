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
        call.respond(HttpStatusCode.Created, mapOf("success" to true, "pet" to pet))
    }

    suspend fun getMyPets(call: ApplicationCall, ownerId: UUID) {
        val pets = petService.getMyPets(ownerId)
        call.respond(HttpStatusCode.OK, pets)
    }

    suspend fun getPetById(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val pet = petService.getPetById(ownerId, petId)
        if (pet == null) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("success" to false, "message" to "Mascota no encontrada.")
            )
        } else {
            call.respond(HttpStatusCode.OK, pet)
        }
    }

    suspend fun updatePet(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val request = call.receive<UpdatePetRequest>()
        val pet = petService.updatePet(ownerId, petId, request)
        if (pet == null) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("success" to false, "message" to "Mascota no encontrada.")
            )
        } else {
            call.respond(HttpStatusCode.OK, pet)
        }
    }

    suspend fun deletePet(call: ApplicationCall, ownerId: UUID, petId: UUID) {
        val deleted = petService.deletePet(ownerId, petId)
        if (!deleted) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("success" to false, "message" to "Mascota no encontrada.")
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                mapOf("success" to true, "message" to "Mascota eliminada correctamente.")
            )
        }
    }
}