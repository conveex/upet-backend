package com.upet.auth

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    controller: AuthController
) {
    route("/auth") {
        post("/register") {
            controller.register(call)
        }

        post("/register/walker") {
            controller.registerWalker(call)
        }

        post("/login") {
            controller.login(call)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                controller.me(call, principal)
            }
        }
    }
}