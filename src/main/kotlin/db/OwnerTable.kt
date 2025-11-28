package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object OwnerTable : Table("owners") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val name = text("name")
    val email = text("email").uniqueIndex()
    val phone = text("phone")
    val mainAddress = text("main_address").nullable()
    val profilePhotoUrl = text("profile_photo_url").nullable()
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}