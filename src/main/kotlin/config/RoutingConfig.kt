package com.upet.config

import com.upet.routes.dbHealthRoutes
import com.upet.routes.firebaseHealthRoutes
import com.upet.routes.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
        dbHealthRoutes()
        firebaseHealthRoutes()
    }
}