package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object RatingTable : Table("ratings") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val walkId = uuid("walk_id").references(WalkTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()
    val ownerId = uuid("owner_id").references(OwnerTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)
    val walkerId = uuid("walker_id").references(WalkerTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)

    val stars = integer("stars") // 1..5 lo validamos en app
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, walkerId)
    }
}