package com.cnvx.pets

import org.jetbrains.exposed.sql.Table

object PetTable : Table("pets") {
    val id = uuid("id")
    val ownerId = uuid("owner_id")
    val name = text("name")
    val breed = text("breed").nullable()
    val color = text("color").nullable()
    val size = text("size")
    val age = integer("age").nullable()
    val behavior = text("behavior").nullable()
    val photoUrl = text("photo_url").nullable()
    val active = bool("active")

    override val primaryKey = PrimaryKey(id)
}