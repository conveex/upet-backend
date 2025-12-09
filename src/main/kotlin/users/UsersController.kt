package com.upet.users

import com.upet.auth.AuthService
import com.upet.users.domain.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class UsersController(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {
    suspend fun getMe(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserProfileEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación.",
                    user = null
                )
            )
            return
        }

        val userIdStr = principal.payload.getClaim("user_id").asString()
        if (userIdStr.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "El token no contiene user_id.",
                    user = null
                )
            )
            return
        }

        val userId = try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido.",
                    user = null
                )
            )
            return
        }

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(
                HttpStatusCode.NotFound,
                UserProfileEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null
                )
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            UserProfileEnvelope(
                success = true,
                message = "Perfil de usuario.",
                user = user.toUserProfileResponse()
            )
        )
    }

    suspend fun updatePhoto(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserProfileEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación.",
                    user = null
                )
            )
            return
        }

        val userIdStr = principal.payload.getClaim("user_id").asString()
        if (userIdStr.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "El token no contiene user_id.",
                    user = null
                )
            )
            return
        }

        val userId = try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido.",
                    user = null
                )
            )
            return
        }

        val request = try {
            call.receive<UpdateUserPhotoRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "Body inválido para actualización de foto.",
                    user = null
                )
            )
            return
        }

        val updatedUser = userRepository.updatePhoto(userId, request.photoUrl)
        if (updatedUser == null) {
            call.respond(
                HttpStatusCode.NotFound,
                UserProfileEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null
                )
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            UserProfileEnvelope(
                success = true,
                message = "Foto actualizada correctamente.",
                user = updatedUser.toUserProfileResponse()
            )
        )
    }

    suspend fun updateProfile(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserProfileEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación.",
                    user = null
                )
            )
            return
        }

        val userIdStr = principal.payload.getClaim("user_id").asString()
        if (userIdStr.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "El token no contiene user_id.",
                    user = null
                )
            )
            return
        }

        val userId = try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido.",
                    user = null
                )
            )
            return
        }

        val request = try {
            call.receive<UpdateUserProfileRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "Body inválido para actualización de perfil.",
                    user = null
                )
            )
            return
        }

        val updatedUser = userRepository.updateProfile(userId, request)
        if (updatedUser == null) {
            call.respond(
                HttpStatusCode.NotFound,
                UserProfileEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null
                )
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            UserProfileEnvelope(
                success = true,
                message = "Perfil actualizado correctamente.",
                user = updatedUser.toUserProfileResponse()
            )
        )
    }

    suspend fun deleteMe(call: ApplicationCall, principal: JWTPrincipal?) {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                UserProfileEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación.",
                    user = null
                )
            )
            return
        }

        val userIdStr = principal.payload.getClaim("user_id").asString()
        if (userIdStr.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "El token no contiene user_id.",
                    user = null
                )
            )
            return
        }

        val userId = try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                UserProfileEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido.",
                    user = null
                )
            )
            return
        }

        val deletedUser = userRepository.softDelete(userId)
        if (deletedUser == null) {
            call.respond(
                HttpStatusCode.NotFound,
                UserProfileEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null
                )
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            UserProfileEnvelope(
                success = true,
                message = "Cuenta desactivada correctamente.",
                user = deletedUser.toUserProfileResponse()
            )
        )
    }
}

private fun User.toUserProfileResponse(): UserProfileResponse =
    UserProfileResponse(
        id = id.toString(),
        email = email,
        name = name,
        phone = phone,
        mainAddress = mainAddress,
        photoUrl = photoUrl,
        emailVerified = emailVerified,
        isClient = isClient,
        isWalker = isWalker,
        isAdmin = isAdmin,
        status = status.name
    )