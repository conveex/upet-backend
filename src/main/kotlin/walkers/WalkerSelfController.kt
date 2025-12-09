package com.upet.walkers

import com.upet.users.UserRepository
import com.upet.users.domain.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class WalkerSelfController(
    private val userRepository: UserRepository,
    private val walkerProfileRepository: WalkerProfileRepository
) {
    suspend fun getMe(call: ApplicationCall, principal: JWTPrincipal?) {
        val userId = extractUserIdOrFail(call, principal) ?: return

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        if (!user.isWalker) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerSelfEnvelope(
                    success = false,
                    message = "El usuario autenticado no es paseador.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val profile = walkerProfileRepository.findProfileByUserId(userId)

        call.respond(
            HttpStatusCode.OK,
            WalkerSelfEnvelope(
                success = true,
                message = "Perfil de paseador.",
                user = user.toWalkerUserResponse(),
                profile = profile
            )
        )
    }

    suspend fun updateProfile(call: ApplicationCall, principal: JWTPrincipal?) {
        val userId = extractUserIdOrFail(call, principal) ?: return

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        if (!user.isWalker) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerSelfEnvelope(
                    success = false,
                    message = "El usuario autenticado no es paseador.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val request = try {
            call.receive<UpdateWalkerProfileRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Body inválido para actualización de perfil de paseador.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val updatedProfile = walkerProfileRepository.upsertProfileForUser(userId, request)

        call.respond(
            HttpStatusCode.OK,
            WalkerSelfEnvelope(
                success = true,
                message = "Perfil de paseador actualizado correctamente.",
                user = user.toWalkerUserResponse(),
                profile = updatedProfile
            )
        )
    }

    suspend fun updatePhoto(call: ApplicationCall, principal: JWTPrincipal?, userRepository: UserRepository) {
        val userId = extractUserIdOrFail(call, principal) ?: return

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        if (!user.isWalker) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerSelfEnvelope(
                    success = false,
                    message = "El usuario autenticado no es paseador.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val request = try {
            call.receive<UpdateWalkerPhotoRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Body inválido para actualización de foto.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val updatedUser = userRepository.updatePhoto(userId, request.photoUrl)
        if (updatedUser == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerSelfEnvelope(
                    success = false,
                    message = "Usuario no encontrado.",
                    user = null,
                    profile = null
                )
            )
            return
        }

        val profile = walkerProfileRepository.findProfileByUserId(userId)

        call.respond(
            HttpStatusCode.OK,
            WalkerSelfEnvelope(
                success = true,
                message = "Foto de paseador actualizada correctamente.",
                user = updatedUser.toWalkerUserResponse(),
                profile = profile
            )
        )
    }

    private suspend fun extractUserIdOrFail(
        call: ApplicationCall,
        principal: JWTPrincipal?
    ): UUID? {
        if (principal == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                WalkerSelfEnvelope(
                    success = false,
                    message = "No se encontró el token de autenticación.",
                    user = null,
                    profile = null
                )
            )
            return null
        }

        val userIdStr = principal.payload.getClaim("user_id").asString()
        if (userIdStr.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerSelfEnvelope(
                    success = false,
                    message = "El token no contiene user_id.",
                    user = null,
                    profile = null
                )
            )
            return null
        }

        return try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerSelfEnvelope(
                    success = false,
                    message = "user_id del token no es un UUID válido.",
                    user = null,
                    profile = null
                )
            )
            null
        }
    }
}

private fun User.toWalkerUserResponse(): WalkerUserResponse =
    WalkerUserResponse(
        id = id.toString(),
        email = email,
        name = name,
        photoUrl = photoUrl,
        status = status.name
    )