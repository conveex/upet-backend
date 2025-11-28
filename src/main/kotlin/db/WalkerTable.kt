package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object WalkerTable : Table("walkers") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val name = text("name")
    val email = text("email").uniqueIndex()
    val phone = text("phone")
    val description = text("description").nullable()
    val age = integer("age").nullable()
    val serviceArea = text("service_area").nullable()
    val profilePhotoUrl = text("profile_photo_url").nullable()
    val dogLimit = integer("dog_limit").default(1)
    val avgRating = decimal("avg_rating", precision = 3, scale = 2).default(java.math.BigDecimal("0.00"))
    val active = bool("active").default(true)
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}