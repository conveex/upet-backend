package com.upet.payments

import com.upet.data.db.tables.PaymentMethodsTable
import com.upet.data.db.tables.WalkerPaymentMethodsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class WalkerPaymentMethodRepository {
    fun listForUser(userId: UUID): List<UserPaymentMethodResponse> = transaction {
        (WalkerPaymentMethodsTable innerJoin PaymentMethodsTable)
            .selectAll()
            .where { WalkerPaymentMethodsTable.userId eq userId }
            .map { row ->
                UserPaymentMethodResponse(
                    id = row[WalkerPaymentMethodsTable.id].toString(),
                    paymentMethodId = row[WalkerPaymentMethodsTable.paymentMethodId].toString(),
                    code = row[PaymentMethodsTable.code],
                    displayName = row[PaymentMethodsTable.displayName],
                    description = row[PaymentMethodsTable.description],
                    extraDetails = row[WalkerPaymentMethodsTable.extraDetails]
                )
            }
    }

    fun addForUser(
        userId: UUID,
        paymentMethodId: UUID,
        extraDetails: String?
    ): List<UserPaymentMethodResponse> = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.Companion.UTC)

        val exists = WalkerPaymentMethodsTable
            .selectAll().where {
                (WalkerPaymentMethodsTable.userId eq userId) and
                        (WalkerPaymentMethodsTable.paymentMethodId eq paymentMethodId)
            }
            .any()

        if (!exists) {
            WalkerPaymentMethodsTable.insert { row ->
                row[WalkerPaymentMethodsTable.id] = UUID.randomUUID()
                row[WalkerPaymentMethodsTable.userId] = userId
                row[WalkerPaymentMethodsTable.paymentMethodId] = paymentMethodId
                row[WalkerPaymentMethodsTable.extraDetails] = extraDetails
                row[WalkerPaymentMethodsTable.createdAt] = now
                row[WalkerPaymentMethodsTable.updatedAt] = now
            }
        }

        listForUser(userId)
    }

    fun deleteForUser(
        userId: UUID,
        paymentMethodId: UUID
    ): List<UserPaymentMethodResponse> = transaction {
        WalkerPaymentMethodsTable.deleteWhere {
            (WalkerPaymentMethodsTable.userId eq userId) and
                    (WalkerPaymentMethodsTable.paymentMethodId eq paymentMethodId)
        }

        listForUser(userId)
    }
}