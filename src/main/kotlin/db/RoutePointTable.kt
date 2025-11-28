package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object RoutePointTable : Table("route_points") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val routeId = uuid("route_id").references(RouteTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val sequence = integer("sequence")
    val lat = double("lat")
    val lng = double("lng")

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, routeId, sequence) // similar a idx_route_points_route_sequence
    }
}