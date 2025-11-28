package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object CheckPhotoTable : Table("check_photos") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val walkId = uuid("walk_id").references(WalkTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)

    // type: START, END (validamos en app)
    val type = text("type")
    val photoUrl = text("photo_url")
    val takenAt = timestamp("taken_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, walkId)
    }
}