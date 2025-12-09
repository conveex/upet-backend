package com.upet.users

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.userRoutes(
    usersController: UsersController
) {
    routing {
        authenticate("auth-jwt") {
            route("/api/v1/users") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    usersController.getMe(call, principal)
                }

                put("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    usersController.updateProfile(call, principal)
                }

                put("/me/photo") {
                    val principal = call.principal<JWTPrincipal>()
                    usersController.updatePhoto(call, principal)
                }

                delete("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    usersController.deleteMe(call, principal)
                }
            }
        }
    }
}