package com.upet.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object PaymentMethodsTable : Table("payment_methods") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val code = varchar("code", 50).uniqueIndex()
    val displayName = varchar("display_name", 100)
    val description = text("description").nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}