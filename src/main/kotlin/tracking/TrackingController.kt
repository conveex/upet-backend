package com.upet.tracking

import com.upet.walks.ApiErrorResponse
import com.upet.walks.WalkValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TrackingController(
    private val trackingService: TrackingService,
    private val accessService: TrackingAccessService
) {
    suspend fun postPosition(call: ApplicationCall, userId: UUID, walkId: UUID) {
        try {
            val req = call.receive<TrackingPointRequest>()
            accessService.assertWalkerCanPostPoint(userId, walkId)
            trackingService.saveTrackingPoint(walkId, req)
            call.respond(HttpStatusCode.OK, TrackingAckEnvelope(success = true, message = "Punto guardado."))
        } catch (e: WalkValidationException) {
            call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = e.message ?: "No se pudo guardar el punto."))
        }
    }

    suspend fun getSummary(call: ApplicationCall, userId: UUID, walkId: UUID) {
        try {
            accessService.assertUserCanReadSummary(userId, walkId)
            val row = accessService.findSummary(walkId)
            val resp = row?.let {
                TrackingSummaryResponse(
                    walkId = it.walkId,
                    totalDistanceMeters = it.totalDistanceMeters,
                    totalDurationSeconds = it.totalDurationSeconds,
                    startLat = it.startLat,
                    startLng = it.startLng,
                    endLat = it.endLat,
                    endLng = it.endLng,
                    createdAt = it.createdAt
                )
            }
            call.respond(
                HttpStatusCode.OK,
                TrackingSummaryEnvelope(
                    success = true,
                    message = if (resp == null) "Aún no hay resumen disponible." else "Resumen de tracking.",
                    summary = resp
                )
            )
        } catch (e: WalkValidationException) {
            call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(message = e.message ?: "No se pudo leer el resumen."))
        }
    }
}