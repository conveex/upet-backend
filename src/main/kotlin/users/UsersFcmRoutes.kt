package com.upet.users

import com.upet.walks.ApiErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.userFcmRoutes(controller: UsersFcmController) {

    authenticate("auth-jwt") {

        route("/api/v1/users") {

            post("fcm-token") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiErrorResponse(message = "Token inválido.")
                    )

                val userId = principal.getClaim("user_id", String::class)
                    ?.let(UUID::fromString)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorResponse(message = "Token sin user_id.")
                    )

                controller.updateMyFcmToken(call, userId)
            }
        }
    }
}