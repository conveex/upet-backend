package com.upet.routes

import com.google.firebase.FirebaseApp
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.firebaseHealthRoutes() {
    get ("/firebase-health"){
        val ok = FirebaseApp.getApps().isNotEmpty()
        call.respond(mapOf("firebaseInitialized" to ok))
    }
}