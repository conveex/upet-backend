package com.upet.walkers

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.walkerAdminRoutes(
    controller: WalkerAdminController
) {
    route("/admin/walkers") {

        authenticate("auth-jwt") {
            get("/pending") {
                val principal = call.principal<JWTPrincipal>()
                controller.listPending(call, principal)
            }

            post("/{id}/approve") {
                val principal = call.principal<JWTPrincipal>()
                controller.approve(call, principal)
            }

            post("/{id}/reject") {
                val principal = call.principal<JWTPrincipal>()
                controller.reject(call, principal)
            }
        }
    }
}