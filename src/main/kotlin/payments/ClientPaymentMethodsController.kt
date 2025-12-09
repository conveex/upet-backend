package com.upet.payments

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class ClientPaymentMethodsController(
    private val repository: ClientPaymentMethodRepository
) {

    suspend fun listMyMethods(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación."
                )
            )
            return
        }

        val isClient = principal.getClaim("is_client", Boolean::class) ?: false
        if (!isClient) {
            call.respond(
                HttpStatusCode.Forbidden,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "Solo los clientes pueden gestionar métodos de pago de cliente."
                )
            )
            return
        }

        val userIdStr = principal.getClaim("user_id", String::class)
        val userId = try {
            UUID.fromString(userIdStr)
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido."
                )
            )
            return
        }

        val methods = repository.listForUser(userId)
        call.respond(
            HttpStatusCode.OK,
            UserPaymentMethodsEnvelope(
                success = true,
                message = "Métodos de pago del cliente.",
                methods = methods
            )
        )
    }

    suspend fun addMethod(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación."
                )
            )
            return
        }

        val isClient = principal.getClaim("is_client", Boolean::class) ?: false
        if (!isClient) {
            call.respond(
                HttpStatusCode.Forbidden,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "Solo los clientes pueden gestionar métodos de pago de cliente."
                )
            )
            return
        }

        val userIdStr = principal.getClaim("user_id", String::class)
        val userId = try {
            UUID.fromString(userIdStr)
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido."
                )
            )
            return
        }

        val request = try {
            call.receive<AddUserPaymentMethodRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "Body inválido para agregar método de pago."
                )
            )
            return
        }

        val paymentMethodId = try {
            UUID.fromString(request.paymentMethodId)
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "paymentMethodId no es un UUID válido."
                )
            )
            return
        }

        val methods = repository.addForUser(
            userId = userId,
            paymentMethodId = paymentMethodId,
            extraDetails = request.extraDetails
        )

        call.respond(
            HttpStatusCode.OK,
            UserPaymentMethodsEnvelope(
                success = true,
                message = "Método de pago agregado correctamente.",
                methods = methods
            )
        )
    }

    suspend fun deleteMethod(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación."
                )
            )
            return
        }

        val isClient = principal.getClaim("is_client", Boolean::class) ?: false
        if (!isClient) {
            call.respond(
                HttpStatusCode.Forbidden,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "Solo los clientes pueden gestionar métodos de pago de cliente."
                )
            )
            return
        }

        val userIdStr = principal.getClaim("user_id", String::class)
        val userId = try {
            UUID.fromString(userIdStr)
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido."
                )
            )
            return
        }

        val methodIdParam = call.parameters["methodId"]
        val paymentMethodId = try {
            UUID.fromString(methodIdParam)
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserPaymentMethodsEnvelope(
                    success = false,
                    message = "methodId de ruta no es un UUID válido."
                )
            )
            return
        }

        val methods = repository.deleteForUser(
            userId = userId,
            paymentMethodId = paymentMethodId
        )

        call.respond(
            HttpStatusCode.OK,
            UserPaymentMethodsEnvelope(
                success = true,
                message = "Método de pago eliminado correctamente.",
                methods = methods
            )
        )
    }
}
