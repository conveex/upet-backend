package com.upet.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object WalkerPaymentMethodsTable : Table("walker_payment_methods") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val userId = uuid("user_id")
    val paymentMethodId = uuid("payment_method_id")
    val extraDetails = text("extra_details")
        .nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    init {
        index(isUnique = true, columns = arrayOf(userId, paymentMethodId))
    }
}