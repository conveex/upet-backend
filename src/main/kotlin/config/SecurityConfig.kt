package com.upet.config

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "upet-audience"
    val jwtDomain = environment.config.propertyOrNull("jwt.domain")?.getString() ?: "upet-domain"
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "upet-realm"
    val jwtSecret = System.getenv("JWT_SECRET") ?: "dev-secret-CHANGE-ME"

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                com.auth0.jwt.JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString().isNullOrEmpty()) null
                else JWTPrincipal(credential.payload)
            }
        }
    }
}