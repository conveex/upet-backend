package com.upet.walkers

import com.upet.users.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.walkerRoutes(
    walkerSelfController: WalkerSelfController,
    userRepository: UserRepository
) {
    routing {
        authenticate("auth-jwt") {
            route("/api/v1/walkers") {

                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    walkerSelfController.getMe(call, principal)
                }

                put("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    walkerSelfController.updateProfile(call, principal)
                }

                put("/me/photo") {
                    val principal = call.principal<JWTPrincipal>()
                    walkerSelfController.updatePhoto(call, principal, userRepository)
                }
            }
        }
    }
}