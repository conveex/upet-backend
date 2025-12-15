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
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isClient = principal.getClaim("is_client", Boolean::class) ?: false
                if (!isClient) {
                    return@post call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los clientes pueden crear paseos."))
                }

                controller.createWalk(call, userId)
            }

            get("pending") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isClient = principal.getClaim("is_client", Boolean::class) ?: false
                if (!isClient) {
                    return@get call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los clientes pueden listar sus paseos pendientes."))
                }

                controller.getPendingWalks(call, userId)
            }

            get("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isClient = principal.getClaim("is_client", Boolean::class) ?: false
                if (!isClient) {
                    return@get call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los clientes pueden ver detalle de sus paseos."))
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo faltante."))

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo inválido."))

                controller.getWalkDetail(call, userId, walkId)
            }

            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isClient = principal.getClaim("is_client", Boolean::class) ?: false
                if (!isClient) {
                    return@delete call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los clientes pueden cancelar paseos."))
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo faltante."))

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo inválido."))

                controller.cancelWalk(call, userId, walkId)
            }

            get("available") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isWalker = principal.getClaim("is_walker", Boolean::class) ?: false
                if (!isWalker) {
                    return@get call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los paseadores pueden ver paseos disponibles."))
                }

                controller.getAvailableWalksForWalker(call, userId)
            }

            get("available/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isWalker = principal.getClaim("is_walker", Boolean::class) ?: false
                if (!isWalker) {
                    return@get call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los paseadores pueden ver detalle de paseos disponibles."))
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo faltante."))

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo inválido."))

                controller.getAvailableWalkDetailForWalker(call, userId, walkId)
            }

            post("{id}/accept") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(message = "Token inválido."))

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "Token sin user_id."))

                val isWalker = principal.getClaim("is_walker", Boolean::class) ?: false
                if (!isWalker) {
                    return@post call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo los paseadores pueden aceptar paseos."))
                }

                val walkIdParam = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo faltante."))

                val walkId = runCatching { UUID.fromString(walkIdParam) }.getOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = "ID de paseo inválido."))

                controller.acceptWalk(call, userId, walkId)
            }
        }
    }
}