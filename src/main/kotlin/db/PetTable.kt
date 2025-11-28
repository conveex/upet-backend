package com.cnvx.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object PetTable : Table("pets") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val ownerId = uuid("owner_id").references(OwnerTable.id, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val breed = text("breed").nullable()
    val color = text("color").nullable()
    val size = text("size") // SMALL/MEDIUM/LARGE → lo validamos en app
    val age = integer("age").nullable()
    val behavior = text("behavior").nullable()
    val photoUrl = text("photo_url").nullable()
    val active = bool("active").default(true)

    override val primaryKey = PrimaryKey(id)
}