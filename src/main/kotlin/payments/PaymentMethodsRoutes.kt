package com.upet.payments

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.clientPaymentMethodsRoutes(
    controller: ClientPaymentMethodsController
) {
    routing {
        authenticate("auth-jwt") {
            route("/api/v1/client/payment-methods") {

                get {
                    val principal = call.principal<JWTPrincipal>()
                    controller.listMyMethods(call, principal)
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    controller.addMethod(call, principal)
                }

                delete("/{methodId}") {
                    val principal = call.principal<JWTPrincipal>()
                    controller.deleteMethod(call, principal)
                }
            }
        }
    }
}

fun Application.walkerPaymentMethodsRoutes(
    controller: WalkerPaymentMethodsController
) {
    routing {
        authenticate("auth-jwt") {
            route("/api/v1/walker/payment-methods") {

                get {
                    val principal = call.principal<JWTPrincipal>()
                    controller.listMyMethods(call, principal)
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    controller.addMethod(call, principal)
                }

                delete("/{methodId}") {
                    val principal = call.principal<JWTPrincipal>()
                    controller.deleteMethod(call, principal)
                }
            }
        }
    }
}