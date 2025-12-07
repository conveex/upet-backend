package com.upet.config

import com.upet.data.db.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.config.propertyOrNull

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL")
        ?: environment.config.propertyOrNull("database.url")?.getString()
        ?: error("DATABASE_URL or database.url not set")

    val dbUser = System.getenv("DATABASE_USER")
        ?: environment.config.propertyOrNull("database.user")?.getString()
        ?: error("DATABASE_USER or database.user not set")

    val dbPassword = System.getenv("DATABASE_PASSWORD")
        ?: environment.config.propertyOrNull("database.password")?.getString()
        ?: error("DATABASE_PASSWORD or database.password not set")

    DatabaseFactory.init(dbUrl, dbUser, dbPassword)
}