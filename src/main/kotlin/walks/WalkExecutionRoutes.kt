package com.upet.walks

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.walkExecutionRoutes(controller: WalkExecutionController) {

    authenticate("auth-jwt") {

        route("/api/v1") {

            get("/walks/active") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val isClient = p.getClaim("is_client", Boolean::class) == true
                if (!isClient) {
                    call.respond(io.ktor.http.HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo clientes."))
                    return@get
                }
                controller.getActiveWalksForClient(call, userId)
            }

            get("/walker/walks/active") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val isWalker = p.getClaim("is_walker", Boolean::class) == true
                if (!isWalker) {
                    call.respond(io.ktor.http.HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo paseadores."))
                    return@get
                }
                controller.getActiveWalksForWalker(call, userId)
            }

            post("/walks/{id}/start") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val isWalker = p.getClaim("is_walker", Boolean::class) == true
                if (!isWalker) {
                    call.respond(io.ktor.http.HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo paseadores."))
                    return@post
                }

                val id = call.parameters["id"] ?: run {
                    call.respond(io.ktor.http.HttpStatusCode.BadRequest, ApiErrorResponse(message = "Falta id."))
                    return@post
                }

                controller.startWalk(call, userId, UUID.fromString(id))
            }

            post("/walks/{id}/end") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val isWalker = p.getClaim("is_walker", Boolean::class) == true
                if (!isWalker) {
                    call.respond(io.ktor.http.HttpStatusCode.Forbidden, ApiErrorResponse(message = "Solo paseadores."))
                    return@post
                }

                val id = call.parameters["id"] ?: run {
                    call.respond(io.ktor.http.HttpStatusCode.BadRequest, ApiErrorResponse(message = "Falta id."))
                    return@post
                }

                controller.endWalk(call, userId, UUID.fromString(id))
            }
        }
    }
}