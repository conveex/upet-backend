package com.upet.tracking

import com.upet.walks.WalkRepository
import com.upet.walks.WalkValidationException
import com.upet.domain.model.WalkStatus
import java.util.UUID

class TrackingAccessService(
    private val walkRepository: WalkRepository,
    private val summariesRepo: WalkTrackSummariesRepository
) {
    fun assertWalkerCanPostPoint(userId: UUID, walkId: UUID) {
        val walk = walkRepository.findWalkDetailByWalker(userId, walkId)
            ?: throw WalkValidationException("Paseo no encontrado para este paseador.")

        if (walk.status != WalkStatus.STARTED) {
            throw WalkValidationException("Solo se puede enviar tracking cuando el paseo está STARTED.")
        }
    }

    fun assertUserCanReadSummary(userId: UUID, walkId: UUID) {
        val allowed = walkRepository.isUserRelatedToWalk(userId, walkId)
        if (!allowed) throw WalkValidationException("No tienes acceso a este paseo.")
    }

    fun findSummary(walkId: UUID): TrackSummaryRow? =
        summariesRepo.findLatestByWalkId(walkId)
}