package com.upet.walks

import com.upet.data.db.tables.WalkPaymentMethodsTable
import com.upet.data.db.tables.WalkPetsTable
import com.upet.data.db.tables.WalksTable
import com.upet.domain.model.WalkSource
import com.upet.domain.model.WalkStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

private fun nowUtc(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.UTC)

class WalkRepository {

    private fun rowToDetail(row: ResultRow, petIds: List<UUID>, paymentMethodIds: List<UUID>): WalkDetailResponse {
        val origin = row[WalksTable.originLat]?.let { lat ->
            row[WalksTable.originLng]?.let { lng -> LatLngDto(lat, lng) }
        }

        val destination = row[WalksTable.destinationLat]?.let { lat ->
            row[WalksTable.destinationLng]?.let { lng -> LatLngDto(lat, lng) }
        }

        val pickup = row[WalksTable.pickupLat]?.let { lat ->
            row[WalksTable.pickupLng]?.let { lng -> LatLngDto(lat, lng) }
        }

        val dropoff = row[WalksTable.dropoffLat]?.let { lat ->
            row[WalksTable.dropoffLng]?.let { lng -> LatLngDto(lat, lng) }
        }

        return WalkDetailResponse(
            id = row[WalksTable.id].toString(),
            clientId = row[WalksTable.clientId].toString(),
            walkerId = row[WalksTable.walkerId]?.toString(),
            predefinedRouteId = row[WalksTable.predefinedRouteId]?.toString(),
            type = row[WalksTable.type],
            source = row[WalksTable.walkSource],
            status = row[WalksTable.status],

            origin = origin,
            destination = destination,
            pickup = pickup,
            dropoff = dropoff,

            estimatedDistanceMeters = row[WalksTable.estimatedDistanceMeters],
            estimatedDurationSeconds = row[WalksTable.estimatedDurationSeconds],

            selectedRoutePolylineEncoded = row[WalksTable.selectedRoutePolylineEncoded],

            requestedStartTime = row[WalksTable.requestedStartTime],
            actualStartTime = row[WalksTable.actualStartTime],
            actualEndTime = row[WalksTable.actualEndTime],

            priceAmount = row[WalksTable.priceAmount],
            priceCurrency = row[WalksTable.priceCurrency],

            selectedPaymentMethodId = row[WalksTable.selectedPaymentMethodId]?.toString(),
            agreedPaymentMethodId = row[WalksTable.agreedPaymentMethodId]?.toString(),

            chatThreadId = row[WalksTable.chatThreadId],
            trackingId = row[WalksTable.trackingId],

            petIds = petIds.map(UUID::toString),
            paymentMethodIds = paymentMethodIds.map(UUID::toString),

            createdAt = row[WalksTable.createdAt],
            updatedAt = row[WalksTable.updatedAt]
        )
    }

    fun createWalk(
        clientId: UUID,
        request: CreateWalkRequest,
        priceAmount: Double,
        priceCurrency: String
    ): WalkDetailResponse = transaction {
        val now = nowUtc()
        val walkId = UUID.randomUUID()

        val inserted = WalksTable.insert { row ->
            row[id] = walkId
            row[WalksTable.clientId] = clientId
            row[walkerId] = null
            row[predefinedRouteId] = request.predefinedRouteId?.let(UUID::fromString)

            row[type] = request.type
            row[walkSource] = if (request.predefinedRouteId != null) WalkSource.PREDEFINED else WalkSource.CUSTOM
            row[status] = WalkStatus.PENDING

            row[originLat] = request.origin?.lat
            row[originLng] = request.origin?.lng
            row[destinationLat] = request.destination?.lat
            row[destinationLng] = request.destination?.lng

            row[estimatedDistanceMeters] = request.estimatedDistanceMeters
            row[estimatedDurationSeconds] = request.estimatedDurationSeconds

            row[requestedStartTime] = request.requestedStartTime
            row[actualStartTime] = null
            row[actualEndTime] = null

            row[pickupLat] = request.pickup?.lat
            row[pickupLng] = request.pickup?.lng
            row[dropoffLat] = request.dropoff?.lat
            row[dropoffLng] = request.dropoff?.lng

            row[selectedRoutePolylineEncoded] = request.selectedRoutePolylineEncoded

            row[WalksTable.priceAmount] = priceAmount
            row[WalksTable.priceCurrency] = priceCurrency

            row[selectedPaymentMethodId] = null
            row[agreedPaymentMethodId] = null

            row[chatThreadId] = null
            row[trackingId] = null

            row[createdAt] = now
            row[updatedAt] = now
        }.resultedValues!!.single()

        request.petIds.distinct().forEach { petIdStr ->
            WalkPetsTable.insert { row ->
                row[WalkPetsTable.id] = UUID.randomUUID()
                row[WalkPetsTable.walkId] = walkId
                row[WalkPetsTable.petId] = UUID.fromString(petIdStr)
            }
        }

        request.paymentMethodIds.distinct().forEach { pmIdStr ->
            WalkPaymentMethodsTable.insert { row ->
                row[WalkPaymentMethodsTable.id] = UUID.randomUUID()
                row[WalkPaymentMethodsTable.walkId] = walkId
                row[WalkPaymentMethodsTable.paymentMethodId] = UUID.fromString(pmIdStr)
            }
        }

        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(inserted, petIds, paymentIds)
    }

    fun findPendingSummariesByClient(clientId: UUID): List<WalkSummaryResponse> = transaction {
        WalksTable
            .selectAll()
            .where { (WalksTable.clientId eq clientId) and (WalksTable.status eq WalkStatus.PENDING) }
            .orderBy(WalksTable.requestedStartTime, SortOrder.ASC)
            .map { row ->
                WalkSummaryResponse(
                    id = row[WalksTable.id].toString(),
                    type = row[WalksTable.type],
                    status = row[WalksTable.status],
                    requestedStartTime = row[WalksTable.requestedStartTime],
                    estimatedDistanceMeters = row[WalksTable.estimatedDistanceMeters],
                    estimatedDurationSeconds = row[WalksTable.estimatedDurationSeconds],
                    priceAmount = row[WalksTable.priceAmount],
                    priceCurrency = row[WalksTable.priceCurrency]
                )
            }
    }

    fun findWalkDetailById(clientId: UUID, walkId: UUID): WalkDetailResponse? = transaction {
        val row = WalksTable
            .selectAll()
            .where { (WalksTable.id eq walkId) and (WalksTable.clientId eq clientId) }
            .singleOrNull()
            ?: return@transaction null

        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(row, petIds, paymentIds)
    }

    fun cancelPendingWalk(clientId: UUID, walkId: UUID): WalkDetailResponse? = transaction {
        val now = nowUtc()

        val updated = WalksTable.update(
            where = {
                (WalksTable.id eq walkId) and
                        (WalksTable.clientId eq clientId) and
                        (WalksTable.status eq WalkStatus.PENDING)
            }
        ) { row ->
            row[status] = WalkStatus.CANCELLED
            row[updatedAt] = now
        }

        if (updated == 0) return@transaction null

        val row = WalksTable.selectAll().where { WalksTable.id eq walkId }.singleOrNull() ?: return@transaction null
        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(row, petIds, paymentIds)
    }
}