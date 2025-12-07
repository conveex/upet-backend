package com.upet.routes

import com.upet.data.db.tables.PaymentMethodsTable
import com.upet.data.db.tables.PetsTable
import com.upet.data.db.tables.UsersTable
import com.upet.data.db.tables.WalkerProfilesTable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.dbHealthRoutes() {
    get("/db-health") {
        val stats = transaction {
            mapOf(
                "users" to UsersTable.selectAll().count(),
                "walker_profiles" to WalkerProfilesTable.selectAll().count(),
                "pets" to PetsTable.selectAll().count(),
                "payment_methods" to PaymentMethodsTable.selectAll().count()
            )
        }

        call.respond(stats)
    }
}