package com.upet.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.upet.users.domain.User
import io.ktor.server.config.ApplicationConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class JwtProvider(config: ApplicationConfig) {

    private val jwtConfig = config.config("jwt")

    private val issuer = jwtConfig.property("domain").getString()
    private val audience = jwtConfig.property("audience").getString()
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret-change-me"

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(user: User): String {
        val now = Instant.now()
        val expiresAt = Date.from(now.plus(7, ChronoUnit.DAYS))

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(user.id.toString())
            .withClaim("user_id", user.id.toString())
            .withClaim("is_client", user.isClient)
            .withClaim("is_walker", user.isWalker)
            .withClaim("is_admin", user.isAdmin)
            .withClaim("status", user.status.name)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}