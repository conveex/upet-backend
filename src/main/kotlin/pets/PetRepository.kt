package com.upet.pets

import com.upet.data.db.tables.PetsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

private fun nowUtc(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.UTC)


class PetRepository {
    private fun rowToPet(row: ResultRow): PetResponse =
        PetResponse(
            id = row[PetsTable.id].toString(),
            ownerId = row[PetsTable.ownerId].toString(),
            name = row[PetsTable.name],
            species = row[PetsTable.species],
            breed = row[PetsTable.breed],
            color = row[PetsTable.color],
            size = row[PetsTable.size],
            age = row[PetsTable.age],
            behavior = row[PetsTable.behavior],
            specialConditions = row[PetsTable.specialConditions],
            photoUrl = row[PetsTable.photoUrl],
            createdAt = row[PetsTable.createdAt],
            updatedAt = row[PetsTable.updatedAt]
        )

    fun create(ownerId: UUID, request: CreatePetRequest): PetResponse = transaction {
        val now = nowUtc()

        val inserted = PetsTable.insert { row ->
            row[id] = UUID.randomUUID()
            row[PetsTable.ownerId] = ownerId
            row[name] = request.name
            row[species] = request.species
            row[breed] = request.breed
            row[color] = request.color
            row[size] = request.size
            row[age] = request.age
            row[behavior] = request.behavior
            row[specialConditions] = request.specialConditions
            row[photoUrl] = request.photoUrl
            row[createdAt] = now
            row[updatedAt] = now
        }.resultedValues!!.single()

        rowToPet(inserted)
    }

    fun findByOwner(ownerId: UUID): List<PetResponse> = transaction {
        PetsTable
            .selectAll().where { PetsTable.ownerId eq ownerId }
            .orderBy(PetsTable.createdAt, SortOrder.ASC)
            .map(::rowToPet)
    }

    fun findById(ownerId: UUID, petId: UUID): PetResponse? = transaction {
        PetsTable
            .selectAll().where { (PetsTable.id eq petId) and (PetsTable.ownerId eq ownerId) }
            .singleOrNull()
            ?.let(::rowToPet)
    }

    fun update(ownerId: UUID, petId: UUID, request: UpdatePetRequest): PetResponse? = transaction {
        val now = nowUtc()

        val updatedRows = PetsTable.update(
            where = { (PetsTable.id eq petId) and (PetsTable.ownerId eq ownerId) }
        ) { row ->
            request.name?.let { row[name] = it }
            request.species?.let { row[species] = it }
            request.breed?.let { row[breed] = it }
            request.color?.let { row[color] = it }
            request.size?.let { row[size] = it }
            request.age?.let { row[age] = it }
            request.behavior?.let { row[behavior] = it }
            request.specialConditions?.let { row[specialConditions] = it }
            request.photoUrl?.let { row[photoUrl] = it }
            row[updatedAt] = now
        }

        if (updatedRows == 0) {
            null
        } else {
            PetsTable
                .selectAll().where { (PetsTable.id eq petId) and (PetsTable.ownerId eq ownerId) }
                .singleOrNull()
                ?.let(::rowToPet)
        }
    }

    fun delete(ownerId: UUID, petId: UUID): Boolean = transaction {
        PetsTable.deleteWhere {
            (PetsTable.id eq petId) and (PetsTable.ownerId eq ownerId)
        } > 0
    }
}