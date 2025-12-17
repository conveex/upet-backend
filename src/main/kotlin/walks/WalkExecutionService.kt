package com.upet.walks

import com.upet.domain.model.MediaFileType
import com.upet.domain.model.WalkStatus
import com.upet.media.MediaFilesRepository
import com.upet.notifications.NotificationService
import com.upet.tracking.TrackingService
import com.upet.users.UsersFcmRepository
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class WalkExecutionService(
    private val walkRepository: WalkRepository,
    private val mediaFilesRepository: MediaFilesRepository,
    private val trackingService: TrackingService,
    private val usersFcmRepository: UsersFcmRepository,
    private val notificationService: NotificationService
) {
    private val startRadiusMeters = 250.0
    private val endRadiusMeters = 300.0

    private fun haversineMeters(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(bLat - aLat)
        val dLng = Math.toRadians(bLng - aLng)
        val lat1 = Math.toRadians(aLat)
        val lat2 = Math.toRadians(bLat)

        val sinDLat = sin(dLat / 2.0)
        val sinDLng = sin(dLng / 2.0)

        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLng * sinDLng
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return r * c
    }

    private fun startPointOf(w: WalkDetailResponse): LatLngDto? =
        w.pickup ?: w.origin

    private fun endPointOf(w: WalkDetailResponse): LatLngDto? =
        w.dropoff ?: w.destination ?: w.origin

    fun startWalk(walkerUserId: UUID, walkId: UUID, request: StartWalkRequest): WalkDetailResponse {
        val current = walkRepository.findWalkDetailByWalker(walkerUserId, walkId)
            ?: throw WalkValidationException("Paseo no encontrado para este paseador.")

        if (current.status != WalkStatus.ACCEPTED) {
            throw WalkValidationException("Solo se puede iniciar un paseo en estado ACCEPTED.")
        }

        val startPoint = startPointOf(current)
            ?: throw WalkValidationException("El paseo no tiene punto de inicio definido (pickup/origin).")

        val dist = haversineMeters(request.lat, request.lng, startPoint.lat, startPoint.lng)
        if (dist > startRadiusMeters) {
            throw WalkValidationException("Debes estar cerca del punto de inicio para iniciar el paseo. Distancia: ${dist.toInt()}m.")
        }

        mediaFilesRepository.insertMediaFile(
            walkId = walkId,
            type = MediaFileType.WALK_START_PHOTO,
            storagePath = request.startPhotoUrl,
            sizeBytes = 0L,
            mimeType = "image/jpeg"
        )

        val updated = walkRepository.updateStatusForWalker(
            walkerUserId = walkerUserId,
            walkId = walkId,
            fromStatus = WalkStatus.ACCEPTED,
            toStatus = WalkStatus.STARTED,
            setActualStartTime = true
        ) ?: throw WalkValidationException("No fue posible iniciar el paseo (ya cambió de estado).")

        trackingService.initRealtimeTracking(updated)

        return updated
    }

    fun endWalk(walkerUserId: UUID, walkId: UUID, request: EndWalkRequest): WalkDetailResponse {
        val current = walkRepository.findWalkDetailByWalker(walkerUserId, walkId)
            ?: throw WalkValidationException("Paseo no encontrado para este paseador.")

        if (current.status != WalkStatus.STARTED) {
            throw WalkValidationException("Solo se puede finalizar un paseo en estado STARTED.")
        }

        val endPoint = endPointOf(current)
            ?: throw WalkValidationException("El paseo no tiene punto de fin definido.")

        val dist = haversineMeters(request.lat, request.lng, endPoint.lat, endPoint.lng)
        if (dist > endRadiusMeters) {
            throw WalkValidationException("Debes estar cerca del punto de fin para finalizar el paseo. Distancia: ${dist.toInt()}m.")
        }

        mediaFilesRepository.insertMediaFile(
            walkId = walkId,
            type = MediaFileType.WALK_END_PHOTO,
            storagePath = request.endPhotoUrl,
            sizeBytes = 0L,
            mimeType = "image/jpeg"
        )

        val updated = walkRepository.updateStatusForWalker(
            walkerUserId = walkerUserId,
            walkId = walkId,
            fromStatus = WalkStatus.STARTED,
            toStatus = WalkStatus.WALKER_FINISHED,
            setActualEndTime = true
        ) ?: throw WalkValidationException("No fue posible finalizar el paseo (ya cambió de estado).")

        val summaryRow = trackingService.generateAndStoreSummary(walkId)
        val estimatedSec = updated.estimatedDurationSeconds
        val realSec = summaryRow.totalDurationSeconds

        var finalWalk = updated

        if (estimatedSec != null && estimatedSec > 0 && realSec != null && realSec > (estimatedSec * 2)) {
            val moved = walkRepository.updateStatusForWalker(
                walkerUserId = walkerUserId,
                walkId = walkId,
                fromStatus = WalkStatus.WALKER_FINISHED,
                toStatus = WalkStatus.REVIEW_PENDING
            )
            if (moved != null) finalWalk = moved
        }

        runCatching { trackingService.stopRealtimeTracking(walkId) }

        val clientId = runCatching { UUID.fromString(finalWalk.clientId) }.getOrNull()
        if (clientId != null) {
            val token = usersFcmRepository.getFcmToken(clientId)
            notificationService.sendPush(
                token = token,
                title = "Paseo finalizado",
                body = "Tu paseador marcó el paseo como finalizado.",
                data = mapOf(
                    "type" to "WALK_FINISHED",
                    "walkId" to finalWalk.id
                )
            )
        }

        return finalWalk
    }
}