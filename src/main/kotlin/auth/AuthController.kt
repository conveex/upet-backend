package com.upet.auth

import com.upet.users.UserRepository
import com.upet.users.domain.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class AuthController(
    val authService: AuthService,
    private val userRepository: UserRepository
) {

    private fun toAuthUserResponse(user: User): AuthUserResponse =
        AuthUserResponse(
            id = user.id.toString(),
            email = user.email,
            name = user.name,
            phone = user.phone,
            mainAddress = user.mainAddress,
            photoUrl = user.photoUrl,
            emailVerified = user.emailVerified,
            isClient = user.isClient,
            isWalker = user.isWalker,
            isAdmin = user.isAdmin,
            status = user.status.name
        )

    suspend fun register(call: ApplicationCall) {
        val request = runCatching { call.receive<RegisterUserRequest>() }
            .getOrElse {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse(false, "Formato de petición inválido.")
                )
                return
            }

        val result = authService.register(request)

        result.onSuccess { auth ->
            val msg = if (auth.user.isWalker && auth.user.status.name == "PENDING_APPROVAL") {
                "Registro exitoso. Tu cuenta de paseador está pendiente de aprobación."
            } else {
                "Registro exitoso."
            }

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    success = true,
                    message = msg,
                    token = auth.token,
                    user = toAuthUserResponse(auth.user)
                )
            )
        }.onFailure { ex ->
            val statusCode = when (ex) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                is IllegalStateException -> HttpStatusCode.Conflict
                else -> HttpStatusCode.InternalServerError
            }

            call.respond(
                statusCode,
                AuthResponse(
                    success = false,
                    message = ex.message ?: "Error en el registro."
                )
            )
        }
    }

    suspend fun login(call: ApplicationCall) {
        val request = runCatching { call.receive<LoginRequest>() }
            .getOrElse {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse(false, "Formato de petición inválido.")
                )
                return
            }

        val result = authService.login(request)

        result.onSuccess { auth ->
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    success = true,
                    message = "Login exitoso.",
                    token = auth.token,
                    user = toAuthUserResponse(auth.user)
                )
            )
        }.onFailure { ex ->
            val statusCode = when (ex) {
                is IllegalArgumentException -> HttpStatusCode.Unauthorized
                is IllegalStateException -> HttpStatusCode.Forbidden
                else -> HttpStatusCode.InternalServerError
            }

            call.respond(
                statusCode,
                AuthResponse(
                    success = false,
                    message = ex.message ?: "Error en el login."
                )
            )
        }
    }

    suspend fun me(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                AuthResponse(false, "No se encontró el token de autenticación.")
            )
            return
        }

        val userIdStr = principal.getClaim("user_id", String::class)
            ?: principal.subject

        if (userIdStr == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                AuthResponse(false, "El token no contiene un user_id válido.")
            )
            return
        }

        val userId = runCatching { UUID.fromString(userIdStr) }.getOrNull()
        if (userId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                AuthResponse(false, "El token tiene un user_id con formato inválido.")
            )
            return
        }

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(
                HttpStatusCode.NotFound,
                AuthResponse(false, "Usuario no encontrado.")
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            AuthResponse(
                success = true,
                message = "Usuario autenticado.",
                user = toAuthUserResponse(user)
            )
        )
    }

    suspend fun registerWalker(call: ApplicationCall) {
        val request = runCatching { call.receive<RegisterWalkerRequest>() }
            .getOrElse {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse(false, "Formato de petición inválido.")
                )
                return
            }

        val result = authService.registerWalker(request)

        result.onSuccess { auth ->
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    success = true,
                    message = "Registro de paseador exitoso. Tu cuenta está pendiente de aprobación.",
                    token = auth.token,
                    user = toAuthUserResponse(auth.user)
                )
            )
        }.onFailure { ex ->
            val statusCode = when (ex) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                is IllegalStateException -> HttpStatusCode.Conflict
                else -> HttpStatusCode.InternalServerError
            }

            call.respond(
                statusCode,
                AuthResponse(
                    success = false,
                    message = ex.message ?: "Error en el registro de paseador."
                )
            )
        }
    }
}