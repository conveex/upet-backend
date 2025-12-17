package com.upet.tracking

import com.google.cloud.Timestamp
import com.upet.walks.WalkDetailResponse
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackingService(
    private val firestoreRepo: FirestoreTrackingRepository,
    private val summariesRepo: WalkTrackSummariesRepository
) {
    fun saveTrackingPoint(walkId: UUID, req: TrackingPointRequest) {
        firestoreRepo.savePoint(walkId, req)
    }

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

    private fun toMillis(v: Any?): Long? = when (v) {
        is Long -> v
        is Int -> v.toLong()
        is Double -> v.toLong()
        is Timestamp -> v.toDate().time
        else -> null
    }

    fun generateAndStoreSummary(walkId: UUID): TrackSummaryRow {
        val points = firestoreRepo.fetchRecentPoints(walkId, limit = 500)

        val normalized = points.mapNotNull { m ->
            val lat = (m["lat"] as? Number)?.toDouble()
            val lng = (m["lng"] as? Number)?.toDouble()
            val ts = toMillis(m["timestamp"])
            if (lat == null || lng == null) null else Triple(lat, lng, ts)
        }.sortedBy { it.third ?: Long.MAX_VALUE }

        if (normalized.size < 2) {
            return summariesRepo.upsertSummary(
                walkId = walkId,
                totalDistanceMeters = null,
                totalDurationSeconds = null,
                startLat = normalized.firstOrNull()?.first,
                startLng = normalized.firstOrNull()?.second,
                endLat = normalized.lastOrNull()?.first,
                endLng = normalized.lastOrNull()?.second
            )
        }

        var totalMeters = 0.0
        for (i in 1 until normalized.size) {
            val (aLat, aLng, _) = normalized[i - 1]
            val (bLat, bLng, _) = normalized[i]
            totalMeters += haversineMeters(aLat, aLng, bLat, bLng)
        }

        val firstTs = normalized.first().third
        val lastTs = normalized.last().third
        val durationSec = if (firstTs != null && lastTs != null && lastTs >= firstTs) {
            ((lastTs - firstTs) / 1000L).toInt()
        } else null

        return summariesRepo.upsertSummary(
            walkId = walkId,
            totalDistanceMeters = totalMeters.toInt(),
            totalDurationSeconds = durationSec,
            startLat = normalized.first().first,
            startLng = normalized.first().second,
            endLat = normalized.last().first,
            endLng = normalized.last().second
        )
    }

    fun initRealtimeTracking(walk: WalkDetailResponse) {
        val walkId = UUID.fromString(walk.id)
        val clientId = UUID.fromString(walk.clientId)
        val walkerId = runCatching { UUID.fromString(walk.walkerId ?: "") }.getOrNull()

        val polyline = walk.selectedRoutePolylineEncoded
            ?: throw IllegalStateException("selectedRoutePolylineEncoded es null; se requiere para trackingDeviation.")

        firestoreRepo.upsertMeta(
            walkId = walkId,
            clientId = clientId,
            walkerId = walkerId,
            polylineEncoded = polyline,
            deviationThresholdMeters = 120,
            evaluationIntervalSeconds = 15,
            active = true
        )

        firestoreRepo.initState(walkId)
    }

    fun stopRealtimeTracking(walkId: UUID) {
        firestoreRepo.setActive(walkId, active = false)
    }
}