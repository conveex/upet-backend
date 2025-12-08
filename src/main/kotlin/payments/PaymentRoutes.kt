package com.upet.payments

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodResponse(
    val id: String,
    val code: String,
    val displayName: String,
    val description: String?
)

fun Route.paymentMethodRoutes(
    repository: PaymentMethodRepository
) {
    get("/payment-methods") {
        val methods = repository.findAll()
        val response = methods.map {
            PaymentMethodResponse(
                id = it.id.toString(),
                code = it.code,
                displayName = it.displayName,
                description = it.description
            )
        }
        call.respond(HttpStatusCode.OK, response)
    }
}