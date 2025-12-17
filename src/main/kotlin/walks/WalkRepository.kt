package com.upet.walks

import com.upet.data.db.tables.WalkPaymentMethodsTable
import com.upet.data.db.tables.WalkPetsTable
import com.upet.data.db.tables.WalkerProfilesTable
import com.upet.data.db.tables.WalksTable
import com.upet.domain.model.WalkSource
import com.upet.domain.model.WalkStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.coalesce
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    private fun haversineDistanceKm(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(bLat - aLat)
        val dLng = Math.toRadians(bLng - aLng)
        val lat1 = Math.toRadians(aLat)
        val lat2 = Math.toRadians(bLat)

        val sinDLat = sin(dLat / 2.0)
        val sinDLng = sin(dLng / 2.0)

        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLng * sinDLng
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return r * c
    }

    private fun walkStartPoint(row: ResultRow): LatLngDto? {
        val pLat = row[WalksTable.pickupLat]
        val pLng = row[WalksTable.pickupLng]
        if (pLat != null && pLng != null) return LatLngDto(pLat, pLng)

        val oLat = row[WalksTable.originLat]
        val oLng = row[WalksTable.originLng]
        if (oLat != null && oLng != null) return LatLngDto(oLat, oLng)

        return null
    }

    private data class WalkerZone(
        val centerLat: Double,
        val centerLng: Double,
        val radiusKm: Double,
        val maxDogs: Int
    )

    private fun loadWalkerZone(walkerUserId: UUID): WalkerZone? {
        val profile = WalkerProfilesTable
            .selectAll()
            .where { WalkerProfilesTable.userId eq walkerUserId }
            .singleOrNull() ?: return null

        return WalkerZone(
            centerLat = profile[WalkerProfilesTable.serviceCenterLat],
            centerLng = profile[WalkerProfilesTable.serviceCenterLng],
            radiusKm = profile[WalkerProfilesTable.zoneRadiusKm],
            maxDogs = profile[WalkerProfilesTable.maxDogs]
        )
    }

    private fun walkDogCount(walkId: UUID): Int =
        WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.count().toInt()

    private fun isWalkAvailableForWalker(zone: WalkerZone, walkRow: ResultRow): Boolean {
        val walkId = walkRow[WalksTable.id]

        // Capacidad
        val dogs = walkDogCount(walkId)
        if (dogs > zone.maxDogs) return false

        // Zona
        val start = walkStartPoint(walkRow) ?: return false
        val distKm = haversineDistanceKm(zone.centerLat, zone.centerLng, start.lat, start.lng)
        if (distKm > zone.radiusKm) return false

        return true
    }

    fun findAvailableSummariesForWalker(walkerUserId: UUID): List<WalkSummaryResponse> = transaction {
        val zone = loadWalkerZone(walkerUserId) ?: return@transaction emptyList()

        WalksTable
            .selectAll()
            .where { (WalksTable.status eq WalkStatus.PENDING) and (WalksTable.walkerId eq null) }
            .orderBy(WalksTable.requestedStartTime, SortOrder.ASC)
            .filter { row -> isWalkAvailableForWalker(zone, row) }
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

    fun findAvailableWalkDetailForWalker(walkerUserId: UUID, walkId: UUID): WalkDetailResponse? = transaction {
        val zone = loadWalkerZone(walkerUserId) ?: return@transaction null

        val row = WalksTable
            .selectAll()
            .where { (WalksTable.id eq walkId) and (WalksTable.status eq WalkStatus.PENDING) and (WalksTable.walkerId eq null) }
            .singleOrNull() ?: return@transaction null

        if (!isWalkAvailableForWalker(zone, row)) return@transaction null

        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(row, petIds, paymentIds)
    }

    fun acceptWalk(
        walkerUserId: UUID,
        walkId: UUID,
        agreedPaymentMethodId: UUID
    ): WalkDetailResponse? = transaction {
        val zone = loadWalkerZone(walkerUserId) ?: return@transaction null
        val now = nowUtc()

        val row = WalksTable
            .selectAll()
            .where { (WalksTable.id eq walkId) and (WalksTable.status eq WalkStatus.PENDING) and (WalksTable.walkerId eq null) }
            .singleOrNull() ?: return@transaction null

        if (!isWalkAvailableForWalker(zone, row)) return@transaction null

        val allowed = WalkPaymentMethodsTable
            .selectAll()
            .where { (WalkPaymentMethodsTable.walkId eq walkId) and (WalkPaymentMethodsTable.paymentMethodId eq agreedPaymentMethodId) }
            .any()

        if (!allowed) return@transaction null

        val updated = WalksTable.update(
            where = {
                (WalksTable.id eq walkId) and
                        (WalksTable.status eq WalkStatus.PENDING) and
                        (WalksTable.walkerId eq null)
            }
        ) { w ->
            w[WalksTable.walkerId] = walkerUserId
            w[WalksTable.status] = WalkStatus.ACCEPTED
            w[WalksTable.agreedPaymentMethodId] = agreedPaymentMethodId

            w[WalksTable.chatThreadId] = walkId.toString()

            w[WalksTable.updatedAt] = now
        }

        if (updated == 0) return@transaction null

        val updatedRow = WalksTable.selectAll().where { WalksTable.id eq walkId }.singleOrNull() ?: return@transaction null
        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(updatedRow, petIds, paymentIds)
    }

    fun findActiveSummariesByClient(clientId: UUID): List<WalkSummaryResponse> = transaction {
        val activeStatuses = listOf(
            WalkStatus.ACCEPTED,
            WalkStatus.STARTED,
            WalkStatus.WALKER_FINISHED
        )

        WalksTable
            .selectAll()
            .where { (WalksTable.clientId eq clientId) and (WalksTable.status inList activeStatuses) }
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

    fun findActiveSummariesByWalker(walkerUserId: UUID): List<WalkSummaryResponse> = transaction {
        val activeStatuses = listOf(
            WalkStatus.ACCEPTED,
            WalkStatus.STARTED,
            WalkStatus.WALKER_FINISHED
        )

        WalksTable
            .selectAll()
            .where { (WalksTable.walkerId eq walkerUserId) and (WalksTable.status inList activeStatuses) }
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

    fun findWalkDetailByWalker(walkerUserId: UUID, walkId: UUID): WalkDetailResponse? = transaction {
        val row = WalksTable
            .selectAll()
            .where { (WalksTable.id eq walkId) and (WalksTable.walkerId eq walkerUserId) }
            .singleOrNull()
            ?: return@transaction null

        val petIds = WalkPetsTable.selectAll().where { WalkPetsTable.walkId eq walkId }.map { it[WalkPetsTable.petId] }
        val paymentIds = WalkPaymentMethodsTable.selectAll().where { WalkPaymentMethodsTable.walkId eq walkId }
            .map { it[WalkPaymentMethodsTable.paymentMethodId] }

        rowToDetail(row, petIds, paymentIds)
    }

    fun updateStatusForWalker(
        walkerUserId: UUID,
        walkId: UUID,
        fromStatus: WalkStatus,
        toStatus: WalkStatus,
        setActualStartTime: Boolean = false,
        setActualEndTime: Boolean = false
    ): WalkDetailResponse? = transaction {
        val now = nowUtc()

        val updated = WalksTable.update(
            where = {
                (WalksTable.id eq walkId) and
                        (WalksTable.walkerId eq walkerUserId) and
                        (WalksTable.status eq fromStatus)
            }
        ) { row ->
            row[status] = toStatus
            row[updatedAt] = now

            if (setActualStartTime) row[actualStartTime] = now
            if (setActualEndTime) row[actualEndTime] = now

            row[WalksTable.chatThreadId] = coalesce(WalksTable.chatThreadId, stringLiteral(walkId.toString()))
            row[WalksTable.trackingId] = coalesce(WalksTable.trackingId, stringLiteral(walkId.toString()))
        }

        if (updated == 0) return@transaction null

        findWalkDetailByWalker(walkerUserId, walkId)
    }

    fun isUserRelatedToWalk(userId: UUID, walkId: UUID): Boolean = transaction {
        val row = WalksTable
            .select(WalksTable.clientId, WalksTable.walkerId)
            .where { WalksTable.id eq walkId }
            .singleOrNull()
            ?: return@transaction false

        val clientId = row[WalksTable.clientId]
        val walkerId = row[WalksTable.walkerId]

        userId == clientId || userId == walkerId
    }
}