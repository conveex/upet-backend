package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object RouteTable : Table("routes") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val originLat = double("origin_lat")
    val originLng = double("origin_lng")
    val destinationLat = double("destination_lat")
    val destinationLng = double("destination_lng")
    val distanceMeters = integer("distance_meters")
    val estimatedTimeSeconds = integer("estimated_time_seconds")

    // type: DISTANCE, TIME, A_B → lo dejamos como text y lo validamos en app
    val type = text("type")
    val routeSource = text("source")

    // En lugar de jsonb, para simplificar: text nullable
    val requestParams = text("request_params").nullable()

    override val primaryKey = PrimaryKey(id)
}