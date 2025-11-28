package com.cnvx.pets

import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.petRoutes() {

    authenticate {
        // GET /api/owners/{ownerId}/pets
        get("/api/owners/{ownerId}/pets") {
            val ownerIdParam = call.parameters["ownerId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val ownerId = UUID.fromString(ownerIdParam)

            val rows = transaction {
                PetTable.select { PetTable.ownerId eq ownerId }.toList()
            }

            val pets = rows.map { row ->
                PetResponse(
                    id = row[PetTable.id].toString(),
                    ownerId = row[PetTable.ownerId].toString(),
                    name = row[PetTable.name],
                    breed = row[PetTable.breed],
                    color = row[PetTable.color],
                    size = row[PetTable.size],
                    age = row[PetTable.age],
                    behavior = row[PetTable.behavior],
                    photoUrl = row[PetTable.photoUrl],
                    active = row[PetTable.active]
                )
            }

            call.respond(pets)
        }

        // POST /api/pets
        post("/api/pets") {
            val body = call.receive<CreatePetRequest>()

            val petId = UUID.randomUUID()

            transaction {
                PetTable.insert {
                    it[id] = petId
                    it[ownerId] = UUID.fromString(body.ownerId)
                    it[name] = body.name
                    it[breed] = body.breed
                    it[color] = body.color
                    it[size] = body.size
                    it[age] = body.age
                    it[behavior] = body.behavior
                    it[photoUrl] = body.photoUrl
                    it[active] = true
                }
            }

            val response = PetResponse(
                id = petId.toString(),
                ownerId = body.ownerId,
                name = body.name,
                breed = body.breed,
                color = body.color,
                size = body.size,
                age = body.age,
                behavior = body.behavior,
                photoUrl = body.photoUrl,
                active = true
            )

            call.respond(HttpStatusCode.Created, response)
        }

        // PUT /api/pets/{petId}
        put("/api/pets/{petId}") {
            val petIdParam = call.parameters["petId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val petId = UUID.fromString(petIdParam)
            val body = call.receive<UpdatePetRequest>()

            val updatedRows = transaction {
                PetTable.update({ PetTable.id eq petId }) {
                    it[name] = body.name
                    it[breed] = body.breed
                    it[color] = body.color
                    it[size] = body.size
                    it[age] = body.age
                    it[behavior] = body.behavior
                    it[photoUrl] = body.photoUrl
                    it[active] = body.active
                }
            }

            if (updatedRows == 0) {
                call.respond(HttpStatusCode.NotFound, "Pet no encontrado")
                return@put
            }

            val row = transaction {
                PetTable.select { PetTable.id eq petId }.single()
            }

            val response = PetResponse(
                id = petId.toString(),
                ownerId = row[PetTable.ownerId].toString(),
                name = row[PetTable.name],
                breed = row[PetTable.breed],
                color = row[PetTable.color],
                size = row[PetTable.size],
                age = row[PetTable.age],
                behavior = row[PetTable.behavior],
                photoUrl = row[PetTable.photoUrl],
                active = row[PetTable.active]
            )

            call.respond(response)
        }
    }
}
