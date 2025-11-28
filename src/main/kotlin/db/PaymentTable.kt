package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object PaymentTable : Table("payments") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val walkId = uuid("walk_id").references(WalkTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()

    val amount = decimal("amount", precision = 10, scale = 2)
    val method = text("method")   // SIMULATED, CARD (validamos en app)
    val status = text("status")   // PENDING, APPROVED, REJECTED
    val gatewayReference = text("gateway_reference").nullable()
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}