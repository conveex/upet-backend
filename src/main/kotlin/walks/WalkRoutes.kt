package com.upet.walks

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.walkRoutes(controller: WalkController) {

    authenticate("auth-jwt") {

        route("/api/v1/walks") {

            post("calculate-route") {
                controller.calculateRoute(call)
            }

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

                val isClient = principal.getClaim("isClient", Boolean::class) ?: false
                if (!isClient) {
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("success" to false, "message" to "Solo los clientes pueden crear paseos.")
                    )
                }

                controller.createWalk(call, userId)
            }

            // Listado simple
            get("pending") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val isClient = principal.getClaim("isClient", Boolean::class) ?: false
                if (!isClient) {
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("success" to false, "message" to "Solo los clientes pueden listar sus paseos pendientes.")
                    )
                }

                controller.getPendingWalks(call, userId)
            }

            // Detalle
            get("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val isClient = principal.getClaim("isClient", Boolean::class) ?: false
                if (!isClient) {
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("success" to false, "message" to "Solo los clientes pueden ver detalle de sus paseos.")
                    )
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de paseo faltante.")
                    )

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de paseo inválido.")
                    )

                controller.getWalkDetail(call, userId, walkId)
            }

            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val isClient = principal.getClaim("isClient", Boolean::class) ?: false
                if (!isClient) {
                    return@delete call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("success" to false, "message" to "Solo los clientes pueden cancelar paseos.")
                    )
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de paseo faltante.")
                    )

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("success" to false, "message" to "ID de paseo inválido.")
                    )

                controller.cancelWalk(call, userId, walkId)
            }
        }
    }
}