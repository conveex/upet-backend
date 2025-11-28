package com.cnvx.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.Date

object JwtConfig {

    private lateinit var secret: String
    private const val issuer = "upet-backend"
    private const val audience = "upet-app"

    fun init(env: ApplicationEnvironment) {
        secret = env.config.property("ktor.security.jwt.secret").getString()
    }

    fun configureKtor(config: JWTAuthenticationProvider.Config) {
        config.realm = "upet"
        config.verifier(
            JWT
                .require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
        )
        config.validate { credential ->
            val ownerId = credential.payload.getClaim("ownerId").asString()
            if (ownerId.isNullOrBlank()) null else JWTPrincipal(credential.payload)
        }
    }

    fun generateToken(ownerId: String): String {
        val now = System.currentTimeMillis()
        val expiry = now + 1000L * 60L * 60L * 24L // 24 horas

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("ownerId", ownerId)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(expiry))
            .sign(Algorithm.HMAC256(secret))
    }
}