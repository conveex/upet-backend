package com.upet.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class UsersFcmController(
    private val repo: UsersFcmRepository
) {
    suspend fun updateMyFcmToken(call: ApplicationCall, userId: UUID) {
        val req = call.receive<UpdateFcmTokenRequest>()

        val token = req.token.trim()
        if(token.isBlank() || token.length > 255) {
            return call.respond(
                HttpStatusCode.BadRequest,
                SimpleEnvelope(success = false, message = "Token FCM inválido.")
            )
        }

        val ok = repo.updateFcmToken(userId, token)
        if(!ok) {
            call.respond(HttpStatusCode.NotFound, SimpleEnvelope(false, "Usuario no encontrado."))
        } else {
            call.respond(HttpStatusCode.OK, SimpleEnvelope(true, "FCM token actualizado correctamente."))
        }
    }
}