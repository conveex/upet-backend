package com.upet.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    val jwtConfig = environment.config.config("jwt")

    val domain = jwtConfig.property("domain").getString()
    val audience = jwtConfig.property("audience").getString()
    val realm = jwtConfig.property("realm").getString()
    val secret = System.getenv("JWT_SECRET") ?: "dev-secret-change-me"

    authentication {
        jwt("auth-jwt") {
            this.realm = realm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(domain)
                    .withAudience(audience)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("user_id").asString()
                if (userId.isNullOrBlank()) null else JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Token no válido o no existente."
                )
            }
        }
    }
}