package com.upet.routes

import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.healthRoutes() {
    route("/health") {
        get {
            call.respondText("UPet Backend OK!")
        }
    }
}