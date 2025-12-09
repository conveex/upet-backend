package com.upet.pets

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

fun Route.petRoutes(controller: PetController) {

    authenticate("auth-jwt") {
        route("/api/v1/pets") {

            post {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("success" to false, "message" to "Token inválido.")
                    )

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "Token sin user_id.")
                    )

                controller.createPet(call, userId)
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                controller.getMyPets(call, userId)
            }

            get("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val petIdParam = call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota faltante.")
                    )

                val petId = runCatching { UUID.fromString(petIdParam) }.getOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota inválido.")
                    )

                controller.getPetById(call, userId, petId)
            }

            put("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@put call.respond(HttpStatusCode.BadRequest)

                val petIdParam = call.parameters["id"]
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota faltante.")
                    )

                val petId = runCatching { UUID.fromString(petIdParam) }.getOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota inválido.")
                    )

                controller.updatePet(call, userId, petId)
            }

            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val petIdParam = call.parameters["id"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota faltante.")
                    )

                val petId = runCatching { UUID.fromString(petIdParam) }.getOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de mascota inválido.")
                    )

                controller.deletePet(call, userId, petId)
            }
        }
    }
}