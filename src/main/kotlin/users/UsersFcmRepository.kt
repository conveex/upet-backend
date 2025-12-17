package com.upet.users

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.SetOptions
import com.google.firebase.cloud.FirestoreClient
import com.upet.data.db.tables.UsersTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

class UsersFcmRepository {

    fun updateFcmToken(userId: UUID, token: String): Boolean = transaction {
        val updated = UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.fcmToken] = token
            it[UsersTable.updatedAt] = nowUtc()
        }

        runCatching {
            FirestoreClient.getFirestore()
                .collection("users")
                .document(userId.toString())
                .set(
                    mapOf(
                        "fcmToken" to token,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
        }
        updated > 0
    }

    fun getFcmToken(userId: UUID): String? = transaction {
        UsersTable
            .select(UsersTable.fcmToken)
            .where { UsersTable.id eq userId }
            .singleOrNull()
            ?.get(UsersTable.fcmToken)
    }
}