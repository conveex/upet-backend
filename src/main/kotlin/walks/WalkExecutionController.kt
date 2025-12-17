package com.upet.walks

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class WalkExecutionController(
    private val walkService: WalkService,
    private val walkExecutionService: WalkExecutionService
) {
    suspend fun getActiveWalksForClient(call: ApplicationCall, clientId: UUID) {
        val walks = walkService.getActiveSummariesByClient(clientId) // lo agregamos abajo en WalkService
        call.respond(HttpStatusCode.OK, WalkSummaryListEnvelope(success = true, walks = walks))
    }

    suspend fun getActiveWalksForWalker(call: ApplicationCall, walkerUserId: UUID) {
        val walks = walkService.getActiveSummariesByWalker(walkerUserId) // lo agregamos abajo en WalkService
        call.respond(HttpStatusCode.OK, WalkSummaryListEnvelope(success = true, walks = walks))
    }

    suspend fun startWalk(call: ApplicationCall, walkerUserId: UUID, walkId: UUID) {
        try {
            val req = call.receive<StartWalkRequest>()
            val walk = walkExecutionService.startWalk(walkerUserId, walkId, req)
            call.respond(HttpStatusCode.OK, WalkEnvelope(success = true, message = "Paseo iniciado correctamente.", walk = walk))
        } catch (e: WalkValidationException) {
            call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = e.message ?: "No se pudo iniciar el paseo."))
        }
    }

    suspend fun endWalk(call: ApplicationCall, walkerUserId: UUID, walkId: UUID) {
        try {
            val req = call.receive<EndWalkRequest>()
            val walk = walkExecutionService.endWalk(walkerUserId, walkId, req)
            call.respond(HttpStatusCode.OK, WalkEnvelope(success = true, message = "Paseo finalizado por el paseador.", walk = walk))
        } catch (e: WalkValidationException) {
            call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = e.message ?: "No se pudo finalizar el paseo."))
        }
    }
}