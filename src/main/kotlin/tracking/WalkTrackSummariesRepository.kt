package com.upet.tracking

import com.upet.data.db.tables.WalkTrackSummariesTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

private fun Double.toBd(scale: Int = 7): BigDecimal =
    BigDecimal.valueOf(this).setScale(scale, RoundingMode.HALF_UP)

private fun Double?.toBdOrNull(scale: Int = 7): BigDecimal? =
    this?.toBd(scale)

private fun BigDecimal?.toDoubleOrNull(): Double? =
    this?.toDouble()

class WalkTrackSummariesRepository {
    fun upsertSummary(
        walkId: UUID,
        totalDistanceMeters: Int?,
        totalDurationSeconds: Int?,
        startLat: Double?,
        startLng: Double?,
        endLat: Double?,
        endLng: Double?
    ): TrackSummaryRow = transaction {
        val id = UUID.randomUUID()
        val created = nowUtc()

        WalkTrackSummariesTable.insert { row ->
            row[WalkTrackSummariesTable.id] = id
            row[WalkTrackSummariesTable.walkId] = walkId
            row[WalkTrackSummariesTable.totalDistanceMeters] = totalDistanceMeters
            row[WalkTrackSummariesTable.totalDurationSeconds] = totalDurationSeconds
            row[WalkTrackSummariesTable.startLat] = startLat.toBdOrNull()
            row[WalkTrackSummariesTable.startLng] = startLng.toBdOrNull()
            row[WalkTrackSummariesTable.endLat] = endLat.toBdOrNull()
            row[WalkTrackSummariesTable.endLng] = endLng.toBdOrNull()
            row[WalkTrackSummariesTable.createdAt] = created
        }

        TrackSummaryRow(
            id = id.toString(),
            walkId = walkId.toString(),
            totalDistanceMeters = totalDistanceMeters,
            totalDurationSeconds = totalDurationSeconds,
            startLat = startLat,
            startLng = startLng,
            endLat = endLat,
            endLng = endLng,
            createdAt = created
        )
    }

    fun findLatestByWalkId(walkId: UUID): TrackSummaryRow? = transaction {
        WalkTrackSummariesTable
            .selectAll()
            .where { WalkTrackSummariesTable.walkId eq walkId }
            .orderBy(WalkTrackSummariesTable.createdAt, org.jetbrains.exposed.sql.SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { r ->
                TrackSummaryRow(
                    id = r[WalkTrackSummariesTable.id].toString(),
                    walkId = r[WalkTrackSummariesTable.walkId].toString(),
                    totalDistanceMeters = r[WalkTrackSummariesTable.totalDistanceMeters],
                    totalDurationSeconds = r[WalkTrackSummariesTable.totalDurationSeconds],
                    startLat = r[WalkTrackSummariesTable.startLat].toDoubleOrNull(),
                    startLng = r[WalkTrackSummariesTable.startLng].toDoubleOrNull(),
                    endLat = r[WalkTrackSummariesTable.endLat].toDoubleOrNull(),
                    endLng = r[WalkTrackSummariesTable.endLng].toDoubleOrNull(),
                    createdAt = r[WalkTrackSummariesTable.createdAt]
                )
            }
    }
}