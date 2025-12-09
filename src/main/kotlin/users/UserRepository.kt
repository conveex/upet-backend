package com.upet.users

import com.upet.auth.RegisterUserRequest
import com.upet.data.db.tables.UsersTable
import com.upet.domain.model.UserStatus
import com.upet.users.domain.User
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepository {
    private fun rowToUser(row: ResultRow): User {
        return User(
            id = row[UsersTable.id],
            email = row[UsersTable.email],
            passwordHash = row[UsersTable.passwordHash],
            name = row[UsersTable.name],
            phone = row[UsersTable.phone],
            mainAddress = row[UsersTable.mainAddress],
            photoUrl = row[UsersTable.photoUrl],
            emailVerified = row[UsersTable.emailVerified],
            fcmToken = row[UsersTable.fcmToken],
            isClient = row[UsersTable.isClient],
            isWalker = row[UsersTable.isWalker],
            isAdmin = row[UsersTable.isAdmin],
            status = row[UsersTable.status]
        )
    }

    fun findByEmail(email: String): User? = transaction {
        UsersTable
            .selectAll().where { UsersTable.email.lowerCase() eq email.lowercase() }
            .limit(1)
            .firstOrNull()
            ?.let(::rowToUser)
    }

    fun findById(id: UUID): User? = transaction {
        UsersTable
            .selectAll().where { UsersTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let(::rowToUser)
    }

    fun existsByEmail(email: String): Boolean = transaction {
        !UsersTable
            .selectAll().where { UsersTable.email.lowerCase() eq email.lowercase() }
            .empty()
    }

    fun createUser(
        request: RegisterUserRequest,
        passwordHash: String,
        isAdmin: Boolean = false
    ): User = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val userId = UUID.randomUUID()

        val status = if (request.isWalker) UserStatus.PENDING_APPROVAL else UserStatus.ACTIVE

        UsersTable.insert { row ->
            row[UsersTable.id] = userId
            row[UsersTable.email] = request.email.lowercase()
            row[UsersTable.passwordHash] = passwordHash
            row[UsersTable.name] = request.name
            row[UsersTable.phone] = request.phone
            row[UsersTable.mainAddress] = request.mainAddress
            row[UsersTable.photoUrl] = null
            row[UsersTable.emailVerified] = false
            row[UsersTable.fcmToken] = null
            row[UsersTable.isClient] = request.isClient
            row[UsersTable.isWalker] = request.isWalker
            row[UsersTable.isAdmin] = isAdmin
            row[UsersTable.status] = status
            row[UsersTable.createdAt] = now
            row[UsersTable.updatedAt] = now
        }

        User(
            id = userId,
            email = request.email.lowercase(),
            passwordHash = passwordHash,
            name = request.name,
            phone = request.phone,
            mainAddress = request.mainAddress,
            photoUrl = null,
            emailVerified = false,
            fcmToken = null,
            isClient = request.isClient,
            isWalker = request.isWalker,
            isAdmin = isAdmin,
            status = status
        )
    }

    fun countAll(): Long = transaction {
        UsersTable.selectAll().count()
    }

    fun findPendingWalkers(): List<User> = transaction {
        UsersTable
            .selectAll().where {
                (UsersTable.isWalker eq true) and
                        (UsersTable.status eq UserStatus.PENDING_APPROVAL)
            }
            .map(::rowToUser)
    }

    fun updateWalkerStatus(id: UUID, newStatus: UserStatus): User? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updated = UsersTable.update({ UsersTable.id eq id }) { row ->
            row[UsersTable.status] = newStatus
            row[UsersTable.updatedAt] = now
        }

        if (updated == 0) return@transaction null

        UsersTable
            .selectAll().where { UsersTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let(::rowToUser)
    }

    fun updatePhoto(userId: UUID, newPhotoUrl: String?): User? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updatedRows = UsersTable.update({ UsersTable.id eq userId }) { row ->
            row[UsersTable.photoUrl] = newPhotoUrl
            row[UsersTable.updatedAt] = now
        }

        if (updatedRows == 0) {
            null
        } else {
            UsersTable
                .selectAll().where { UsersTable.id eq userId }
                .limit(1)
                .firstOrNull()
                ?.let(::rowToUser)
        }
    }

    fun updateProfile(userId: UUID, request: UpdateUserProfileRequest): User? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updatedRows = UsersTable.update({ UsersTable.id eq userId }) { row ->
            request.name?.let { row[UsersTable.name] = it }
            request.phone?.let { row[UsersTable.phone] = it }
            request.mainAddress?.let { row[UsersTable.mainAddress] = it }
            row[UsersTable.updatedAt] = now
        }

        if (updatedRows == 0) {
            null
        } else {
            UsersTable
                .selectAll().where { UsersTable.id eq userId }
                .limit(1)
                .firstOrNull()
                ?.let(::rowToUser)
        }
    }

    fun softDelete(userId: UUID): User? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updatedRows = UsersTable.update({ UsersTable.id eq userId }) { row ->
            row[UsersTable.status] = UserStatus.INACTIVE
            row[UsersTable.fcmToken] = null
            row[UsersTable.updatedAt] = now
        }

        if (updatedRows == 0) {
            null
        } else {
            UsersTable
                .selectAll().where { UsersTable.id eq userId }
                .limit(1)
                .firstOrNull()
                ?.let(::rowToUser)
        }
    }
}