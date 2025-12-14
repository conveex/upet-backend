package com.upet.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.util.AttributeKey

private const val HTTP_CLIENT_KEY = "UPET_CLIENT_KEY"

fun Application.configureHttpClient(): HttpClient {
    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }

        defaultRequest {
            accept(ContentType.Application.Json)
        }
    }

    attributes.put(AttributeKey(HTTP_CLIENT_KEY), client)

    environment.monitor.subscribe(ApplicationStopped) {
        client.close()
    }

    return client
}

fun Application.httpClient(): HttpClient =
    attributes[AttributeKey(HTTP_CLIENT_KEY)]