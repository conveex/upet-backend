package com.upet.walkers

import com.upet.auth.RegisterWalkerRequest
import com.upet.data.db.tables.WalkerProfilesTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class WalkerProfileRepository {

    fun createProfileForUser(userId: UUID, request: RegisterWalkerRequest) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        transaction {
            WalkerProfilesTable.insert { row ->
                row[WalkerProfilesTable.id] = UUID.randomUUID()
                row[WalkerProfilesTable.userId] = userId
                row[WalkerProfilesTable.bio] = request.bio
                row[WalkerProfilesTable.experience] = request.experience
                row[WalkerProfilesTable.serviceZoneLabel] = request.serviceZoneLabel

                row[WalkerProfilesTable.ratingAverage] = 0.0
                row[WalkerProfilesTable.totalReviews] = 0
                row[WalkerProfilesTable.maxDogs] = request.maxDogsPerWalk

                row[WalkerProfilesTable.serviceCenterLat] = request.serviceCenterLat
                row[WalkerProfilesTable.serviceCenterLng] = request.serviceCenterLng
                row[WalkerProfilesTable.zoneRadiusKm] = request.zoneRadiusKm

                row[WalkerProfilesTable.createdAt] = now
                row[WalkerProfilesTable.updatedAt] = now
            }
        }
    }

    private fun rowToProfileResponse(row: ResultRow): WalkerProfileResponse =
        WalkerProfileResponse(
            id = row[WalkerProfilesTable.id].toString(),
            userId = row[WalkerProfilesTable.userId].toString(),
            bio = row[WalkerProfilesTable.bio],
            experience = row[WalkerProfilesTable.experience],
            serviceZoneLabel = row[WalkerProfilesTable.serviceZoneLabel],
            ratingAverage = row[WalkerProfilesTable.ratingAverage],
            totalReviews = row[WalkerProfilesTable.totalReviews],
            maxDogs = row[WalkerProfilesTable.maxDogs],
            serviceCenterLat = row[WalkerProfilesTable.serviceCenterLat],
            serviceCenterLng = row[WalkerProfilesTable.serviceCenterLng],
            zoneRadiusKm = row[WalkerProfilesTable.zoneRadiusKm],
            createdAt = row[WalkerProfilesTable.createdAt].toString(),
            updatedAt = row[WalkerProfilesTable.updatedAt].toString()
        )

    fun findProfileByUserId(userId: UUID): WalkerProfileResponse? = transaction {
        WalkerProfilesTable
            .selectAll().where { WalkerProfilesTable.userId eq userId }
            .limit(1)
            .firstOrNull()
            ?.let(::rowToProfileResponse)
    }

    fun upsertProfileForUser(userId: UUID, request: UpdateWalkerProfileRequest): WalkerProfileResponse =
        transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

            val existing = WalkerProfilesTable
                .selectAll().where { WalkerProfilesTable.userId eq userId }
                .limit(1)
                .firstOrNull()

            if (existing == null) {
                val inserted = WalkerProfilesTable.insert { row ->
                    row[WalkerProfilesTable.id] = UUID.randomUUID()
                    row[WalkerProfilesTable.userId] = userId
                    row[WalkerProfilesTable.bio] = request.bio
                    row[WalkerProfilesTable.experience] = request.experience
                    row[WalkerProfilesTable.serviceZoneLabel] =
                        request.serviceZoneLabel ?: "Zona sin especificar"

                    row[WalkerProfilesTable.ratingAverage] = 0.0
                    row[WalkerProfilesTable.totalReviews] = 0
                    row[WalkerProfilesTable.maxDogs] = request.maxDogs ?: 1

                    row[WalkerProfilesTable.serviceCenterLat] = request.serviceCenterLat ?: 0.0
                    row[WalkerProfilesTable.serviceCenterLng] = request.serviceCenterLng ?: 0.0
                    row[WalkerProfilesTable.zoneRadiusKm] = request.zoneRadiusKm ?: 1.0

                    row[WalkerProfilesTable.createdAt] = now
                    row[WalkerProfilesTable.updatedAt] = now
                }.resultedValues!!.first()

                rowToProfileResponse(inserted)
            } else {
                WalkerProfilesTable.update({ WalkerProfilesTable.userId eq userId }) { row ->
                    request.bio?.let { row[WalkerProfilesTable.bio] = it }
                    request.experience?.let { row[WalkerProfilesTable.experience] = it }
                    request.serviceZoneLabel?.let { row[WalkerProfilesTable.serviceZoneLabel] = it }
                    request.maxDogs?.let { row[WalkerProfilesTable.maxDogs] = it }
                    request.serviceCenterLat?.let { row[WalkerProfilesTable.serviceCenterLat] = it }
                    request.serviceCenterLng?.let { row[WalkerProfilesTable.serviceCenterLng] = it }
                    request.zoneRadiusKm?.let { row[WalkerProfilesTable.zoneRadiusKm] = it }
                    row[WalkerProfilesTable.updatedAt] = now
                }

                WalkerProfilesTable
                    .selectAll().where { WalkerProfilesTable.userId eq userId }
                    .limit(1)
                    .first()
                    .let(::rowToProfileResponse)
            }
        }
}