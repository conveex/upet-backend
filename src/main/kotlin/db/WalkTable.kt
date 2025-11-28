package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object WalkTable : Table("walks") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val ownerId = uuid("owner_id").references(OwnerTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)
    val walkerId = uuid("walker_id").references(WalkerTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)
    val routeId = uuid("route_id").references(RouteTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)

    val scheduledStartAt = timestamp("scheduled_start_at")
    val scheduledEndAt = timestamp("scheduled_end_at").nullable()
    val realStartAt = timestamp("real_start_at").nullable()
    val realEndAt = timestamp("real_end_at").nullable()

    val startLat = double("start_lat").nullable()
    val startLng = double("start_lng").nullable()
    val endLat = double("end_lat").nullable()
    val endLng = double("end_lng").nullable()

    // status: PENDING, ACCEPTED, IN_PROGRESS, FINISHED, CANCELLED
    val status = text("status")

    val price = decimal("price", precision = 10, scale = 2).default(java.math.BigDecimal("0.00"))

    val firebaseChatPath = text("firebase_chat_path")
    val firebaseLocationPath = text("firebase_location_path")

    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, ownerId)
        index(false, walkerId)
        index(false, status)
    }
}