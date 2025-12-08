package com.upet.walkers

import com.upet.auth.RegisterWalkerRequest
import com.upet.data.db.tables.WalkerProfilesTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
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
}