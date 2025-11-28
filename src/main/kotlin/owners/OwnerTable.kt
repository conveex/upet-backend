package com.cnvx.owners

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object OwnerTable : Table("owners") {
    val id = uuid("id")
    val name = text("name")
    val email = text("email").uniqueIndex()
    val phone = text("phone")
    val mainAddress = text("main_address").nullable()
    val profilePhotoUrl = text("profile_photo_url").nullable()
    val passwordHash = text("password_hash")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}