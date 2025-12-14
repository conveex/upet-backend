package com.upet

import com.upet.config.configureCors
import com.upet.config.configureDatabase
import com.upet.config.configureFirebase
import com.upet.config.configureHttpClient
import com.upet.config.configureMonitoring
import com.upet.config.configureRouting
import com.upet.config.configureSecurity
import com.upet.config.configureSerialization
import com.upet.config.httpClient
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>): Unit =
    EngineMain.main(args)

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureCors()
    configureSecurity()
    configureDatabase()
    configureFirebase()

    val httpClient = configureHttpClient()
    configureRouting(httpClient)
}