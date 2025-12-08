package com.upet.payments

import com.upet.data.db.tables.PaymentMethodsTable
import com.upet.payments.domain.PaymentMethod
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class PaymentMethodRepository {
    private fun rowToPaymentMethod(row: ResultRow): PaymentMethod =
        PaymentMethod(
            id = row[PaymentMethodsTable.id],
            code = row[PaymentMethodsTable.code],
            displayName = row[PaymentMethodsTable.displayName],
            description = row[PaymentMethodsTable.description],
            createdAt = row[PaymentMethodsTable.createdAt],
            updatedAt = row[PaymentMethodsTable.updatedAt]
        )

    fun findAll(): List<PaymentMethod> = transaction {
        PaymentMethodsTable
            .selectAll()
            .map(::rowToPaymentMethod)
    }
}