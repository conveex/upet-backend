package com.upet.payments

import com.upet.data.db.tables.ClientPaymentMethodsTable
import com.upet.data.db.tables.PaymentMethodsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ClientPaymentMethodRepository {
    fun listForUser(userId: UUID): List<UserPaymentMethodResponse> = transaction {
        ClientPaymentMethodsTable
            .join(
                PaymentMethodsTable,
                JoinType.INNER,
                additionalConstraint = {
                    ClientPaymentMethodsTable.paymentMethodId eq PaymentMethodsTable.id
                }
            )
            .selectAll().where { ClientPaymentMethodsTable.userId eq userId }
            .map { row ->
                UserPaymentMethodResponse(
                    id = row[ClientPaymentMethodsTable.id].toString(),
                    paymentMethodId = row[ClientPaymentMethodsTable.paymentMethodId].toString(),
                    code = row[PaymentMethodsTable.code],
                    displayName = row[PaymentMethodsTable.displayName],
                    description = row[PaymentMethodsTable.description],
                    extraDetails = row[ClientPaymentMethodsTable.extraDetails]
                )
            }
    }

    fun addForUser(
        userId: UUID,
        paymentMethodId: UUID,
        extraDetails: String?
    ): List<UserPaymentMethodResponse> = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val exists = ClientPaymentMethodsTable
            .selectAll().where {
                (ClientPaymentMethodsTable.userId eq userId) and
                        (ClientPaymentMethodsTable.paymentMethodId eq paymentMethodId)
            }
            .any()

        if (!exists) {
            ClientPaymentMethodsTable.insert { row ->
                row[ClientPaymentMethodsTable.id] = UUID.randomUUID()
                row[ClientPaymentMethodsTable.userId] = userId
                row[ClientPaymentMethodsTable.paymentMethodId] = paymentMethodId
                row[ClientPaymentMethodsTable.extraDetails] = extraDetails
                row[ClientPaymentMethodsTable.createdAt] = now
                row[ClientPaymentMethodsTable.updatedAt] = now
            }
        }

        listForUser(userId)
    }

    fun deleteForUser(
        userId: UUID,
        paymentMethodId: UUID
    ): List<UserPaymentMethodResponse> = transaction {
        ClientPaymentMethodsTable.deleteWhere {
            (ClientPaymentMethodsTable.userId eq userId) and
                    (ClientPaymentMethodsTable.paymentMethodId eq paymentMethodId)
        }

        listForUser(userId)
    }
}