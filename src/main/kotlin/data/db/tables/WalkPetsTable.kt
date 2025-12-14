package com.upet.data.db.tables

import org.jetbrains.exposed.sql.Table

object WalkPetsTable : Table("walk_pets") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val walkId = uuid("walk_id").index()
    val petId = uuid("pet_id").index()
}