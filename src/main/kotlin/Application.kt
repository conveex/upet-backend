package com.cnvx

import com.cnvx.config.DatabaseFactory
import com.cnvx.config.JwtConfig
import com.cnvx.owners.ownerRoutes
import com.cnvx.pets.petRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {

    val dbUrl = environment.config.propertyOrNull("ktor.database.url")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: error("DATABASE_URL no configurado")

    DatabaseFactory.init(dbUrl)
    JwtConfig.init(environment)

    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        anyHost()
        allowHeader("Authorization")
        allowHeader("Content-Type")
    }

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt {
            JwtConfig.configureKtor(this)
        }
    }

    routing {
        get("/health") {
            call.respondText("OK")
        }

        ownerRoutes()
        petRoutes()
    }
}