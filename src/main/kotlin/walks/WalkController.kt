package com.upet.walks

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class WalkController(
    private val walkService: WalkService
) {
    suspend fun calculateRoute(call: ApplicationCall) {
        try {
            val request = call.receive<CalculateRouteRequest>()
            val routes = walkService.calculateRoute(request)
            call.respond(
                HttpStatusCode.OK,
                CalculateRouteResponse(success = true, routes = routes)
            )
        } catch (e: WalkValidationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorResponse(message = e.message ?: "Solicitud de cálculo de ruta inválida.")
            )
        }
    }

    suspend fun createWalk(call: ApplicationCall, clientId: UUID) {
        try {
            val request = call.receive<CreateWalkRequest>()
            val walk = walkService.createWalk(clientId, request)
            call.respond(
                HttpStatusCode.Created,
                WalkEnvelope(
                    success = true,
                    message = "Paseo creado correctamente.",
                    walk = walk
                )
            )
        } catch (e: WalkValidationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkEnvelope(
                    success = false,
                    message = e.message ?: "Datos inválidos para crear el paseo.",
                    walk = null
                )
            )
        }
    }

    suspend fun getPendingWalks(call: ApplicationCall, clientId: UUID) {
        val walks = walkService.getPendingSummariesByClient(clientId)
        call.respond(
            HttpStatusCode.OK,
            WalkSummaryListEnvelope(success = true, walks = walks)
        )
    }

    suspend fun getWalkDetail(call: ApplicationCall, clientId: UUID, walkId: UUID) {
        val walk = walkService.getWalkDetailById(clientId, walkId)
        if (walk == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkEnvelope(success = false, message = "Paseo no encontrado.", walk = null)
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                WalkEnvelope(success = true, message = "Detalle del paseo.", walk = walk)
            )
        }
    }

    suspend fun cancelWalk(call: ApplicationCall, clientId: UUID, walkId: UUID) {
        val walk = walkService.cancelPendingWalk(clientId, walkId)
        if (walk == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkEnvelope(success = false, message = "Paseo no encontrado o no se puede cancelar.", walk = null)
            )
        } else {
            call.respond(
                HttpStatusCode.OK,
                WalkEnvelope(success = true, message = "Paseo cancelado correctamente.", walk = walk)
            )
        }
    }
}