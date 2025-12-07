package com.upet.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object WalkerProfilesTable : Table("walker_profiles") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val userId = uuid("user_id").uniqueIndex()
    val bio = text("bio").nullable()
    val experience = text("experience").nullable()
    val serviceZoneLabel = varchar("service_zone_label", 100)

    val ratingAverage = double("average_rating").default(0.0)
    val totalReviews = integer("total_reviews").default(0)
    val maxDogs = integer("max_dogs_per_walk").default(1)

    val serviceCenterLat = double("service_center_lat")
    val serviceCenterLng = double("service_center_lng")
    val zoneRadiusKm = double("zone_radius_km")

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}