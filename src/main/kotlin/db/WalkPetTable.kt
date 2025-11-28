package com.cnvx.db

import org.jetbrains.exposed.sql.Table

object WalkPetTable : Table("walk_pets") {
    val walkId = uuid("walk_id").references(WalkTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val petId = uuid("pet_id").references(PetTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT)

    override val primaryKey = PrimaryKey(walkId, petId)
}