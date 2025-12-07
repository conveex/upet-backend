package com.upet.routes

import com.upet.data.db.tables.UsersTable
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.dbHealthRoutes() {
    get("/db-health") {
        val count = transaction {
            UsersTable.selectAll().count()
        }

        call.respondText("DB OK - Users count: $count")
    }
}