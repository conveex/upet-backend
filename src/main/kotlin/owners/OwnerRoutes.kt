package com.cnvx.owners

import com.cnvx.auth.AuthResponse
import com.cnvx.auth.LoginRequest
import com.cnvx.auth.OwnerResponse
import com.cnvx.auth.PasswordHasher
import com.cnvx.auth.RegisterOwnerRequest
import com.cnvx.config.JwtConfig
import com.cnvx.db.OwnerTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.ownerRoutes() {
    route("/api/owners") {
        post ("/register"){
            val body = call.receive<RegisterOwnerRequest>()

            val existing = transaction {
                OwnerTable.select { OwnerTable.email eq body.email }.singleOrNull()
            }
            if(existing != null) {
                call.respond(HttpStatusCode.Conflict, "Email ya registrado")
                return@post
            }

            val ownerId = UUID.randomUUID()
            val passwordHash = PasswordHasher.hash(body.password)

            transaction {
                OwnerTable.insert {
                    it[id] = ownerId
                    it[name] = body.name
                    it[email] = body.email
                    it[phone] = body.phone
                    it[OwnerTable.passwordHash] = passwordHash
                    it[mainAddress] = null
                    it[profilePhotoUrl] = null
                }
            }

            val response = OwnerResponse(
                id = ownerId.toString(),
                name = body.name,
                email = body.email,
                phone = body.phone
            )

            val token = JwtConfig.generateToken(ownerId.toString())
            call.respond(HttpStatusCode.Created, AuthResponse(response, token))
        }

        post ("/login"){
            val body = call.receive<LoginRequest>()

            val row = transaction {
                OwnerTable.select { OwnerTable.email eq body.email }.singleOrNull()
            }

            if (row == null) {
                call.respond(HttpStatusCode.NotFound, "Email no encontrado")
                return@post
            }

            val hash = row[OwnerTable.passwordHash]
            val ok = PasswordHasher.verify(body.password, hash)
            if (!ok) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                return@post
            }

            val ownerId = row[OwnerTable.id].toString()

            val response = OwnerResponse(
                id = ownerId,
                name = row[OwnerTable.name],
                email = row[OwnerTable.email],
                phone = row[OwnerTable.phone],
                mainAddress = row[OwnerTable.mainAddress],
                profilePhotoUrl = row[OwnerTable.profilePhotoUrl]
            )

            val token = JwtConfig.generateToken(ownerId)
            call.respond(AuthResponse(response, token))
        }

        authenticate {
            get ("/me"){
                val principal = call.principal<JWTPrincipal>()
                val ownerId = principal!!.payload.getClaim("ownerId").asString()

                val row = transaction {
                    OwnerTable.select { OwnerTable.id eq UUID.fromString(ownerId) }.singleOrNull()
                } ?: run {
                    call.respond(HttpStatusCode.NotFound, "Owner no encontrado")
                    return@get
                }

                val response = OwnerResponse(
                    id = ownerId,
                    name = row[OwnerTable.name],
                    email = row[OwnerTable.email],
                    phone = row[OwnerTable.phone],
                    mainAddress = row[OwnerTable.mainAddress],
                    profilePhotoUrl = row[OwnerTable.profilePhotoUrl]
                )

                call.respond(response)
            }

            get ("/{ownerId}"){
                val ownerIdParam = call.parameters["ownerId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val ownerId = UUID.fromString(ownerIdParam)

                val row = transaction {
                    OwnerTable.select { OwnerTable.id eq ownerId }.singleOrNull()
                } ?: return@get call.respond(HttpStatusCode.NotFound)

                val response = OwnerResponse(
                    id = ownerId.toString(),
                    name = row[OwnerTable.name],
                    email = row[OwnerTable.email],
                    phone = row[OwnerTable.phone],
                    mainAddress = row[OwnerTable.mainAddress],
                    profilePhotoUrl = row[OwnerTable.profilePhotoUrl]
                )

                call.respond(response)
            }
        }
    }
}