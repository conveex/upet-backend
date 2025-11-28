package com.cnvx.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.time
import java.util.UUID

object AvailabilityTable : Table("availabilities") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val walkerId = uuid("walker_id").references(WalkerTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val weekday = integer("weekday") // 0..6, lo validamos en lógica
    val startTime = time("start_time")
    val endTime = time("end_time")

    override val primaryKey = PrimaryKey(id)
}