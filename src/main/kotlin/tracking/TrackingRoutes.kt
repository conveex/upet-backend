package com.upet.tracking

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.trackingRoutes(controller: TrackingController) {

    authenticate("auth-jwt") {

        route("/api/v1") {

            post("/tracking/{walkId}/position") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val walkId = UUID.fromString(call.parameters["walkId"] ?: "")

                controller.postPosition(call, userId, walkId)
            }

            get("/tracking/{walkId}/summary") {
                val p = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(p.getClaim("user_id", String::class)!!)
                val walkId = UUID.fromString(call.parameters["walkId"] ?: "")

                controller.getSummary(call, userId, walkId)
            }
        }
    }
}