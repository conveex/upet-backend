package com.upet.data.db.tables

import com.upet.domain.model.UserStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UsersTable : Table("users") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 255)

    val phone = varchar("phone", 50).nullable()
    val mainAddress = varchar("main_address", 255).nullable()
    val photoUrl = varchar("photo_url", 255).nullable()

    val emailVerified = bool("email_verified").default(false)
    val fcmToken = varchar("fcm_token", 255).nullable()

    val isClient = bool("is_client").default(false)
    val isWalker = bool("is_walker").default(false)
    val isAdmin = bool("is_admin").default(false)
    val status = enumerationByName("status", 32, UserStatus::class)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}