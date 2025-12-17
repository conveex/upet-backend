package com.upet.data.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object WalkTrackSummariesTable : Table("walk_track_summaries") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val walkId = uuid("walk_id")
        .references(
            WalksTable.id,
            onDelete = ReferenceOption.CASCADE
        )

    val totalDistanceMeters = integer("total_distance_meters").nullable()
    val totalDurationSeconds = integer("total_duration_seconds").nullable()

    val startLat = decimal("start_lat", 10, 7).nullable()
    val startLng = decimal("start_lng", 10, 7).nullable()
    val endLat = decimal("end_lat", 10, 7).nullable()
    val endLng = decimal("end_lng", 10, 7).nullable()

    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}