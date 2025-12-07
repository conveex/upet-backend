package com.upet.data.db.tables

import com.upet.domain.model.PetSize
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object PetsTable : Table("pets") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val ownerId = uuid("owner_id").index()
    val name = varchar("name", 100)
    val species = varchar("species", 50)
    val breed = varchar("breed", 255).nullable()
    val color = varchar("color", 255).nullable()
    val size = enumerationByName("size", 20, PetSize::class)
    val age = integer("age")

    val behavior = varchar("behavior", 255).nullable()
    val specialConditions = text("special_conditions").nullable()
    val photoUrl = varchar("photo_url", 255).nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}