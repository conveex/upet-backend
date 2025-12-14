package com.upet.data.db.tables

import org.jetbrains.exposed.sql.Table

object WalkPaymentMethodsTable : Table("walk_payment_methods") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)

    val walkId = uuid("walk_id").index()
    val paymentMethodId = uuid("payment_method_id").index()
}