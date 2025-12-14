package com.upet.data.db.tables

import com.upet.domain.model.WalkSource
import com.upet.domain.model.WalkStatus
import com.upet.domain.model.WalkType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object WalksTable : Table("walks") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val clientId = uuid("client_id").index()
    val walkerId = uuid("walker_id").nullable().index()
    val predefinedRouteId = uuid("predefined_route_id").nullable().index()

    val type = enumerationByName("type", 20, WalkType::class)
    val walkSource = enumerationByName("source", 20, WalkSource::class)
    val status = enumerationByName("status", 30, WalkStatus::class)

    val originLat = double("origin_lat").nullable()
    val originLng = double("origin_lng").nullable()
    val destinationLat = double("destination_lat").nullable()
    val destinationLng = double("destination_lng").nullable()

    val selectedRoutePolylineEncoded = text("selected_route_polyline_encoded").nullable()
    val estimatedDistanceMeters = integer("estimated_distance_meters").nullable()
    val estimatedDurationSeconds = integer("estimated_duration_seconds").nullable()

    val requestedStartTime = datetime("requested_start_time")
    val actualStartTime = datetime("actual_start_time").nullable()
    val actualEndTime = datetime("actual_end_time").nullable()

    val pickupLat = double("pickup_lat").nullable()
    val pickupLng = double("pickup_lng").nullable()
    val dropoffLat = double("dropoff_lat").nullable()
    val dropoffLng = double("dropoff_lng").nullable()

    val priceAmount = double("price_amount")
    val priceCurrency = varchar("price_currency", length = 10)

    val selectedPaymentMethodId = uuid("selected_payment_method_id").nullable()
    val agreedPaymentMethodId = uuid("agreed_payment_method_id").nullable()

    val chatThreadId = varchar("chat_thread_id", length = 255).nullable()
    val trackingId = varchar("tracking_id", length = 255).nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}