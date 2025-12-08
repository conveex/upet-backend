package com.upet.walkers

import com.upet.domain.model.UserStatus
import com.upet.users.UserRepository
import com.upet.users.domain.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WalkerSummaryResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val status: String
)

@Serializable
data class WalkerAdminActionResponse(
    val success: Boolean,
    val message: String,
    val walker: WalkerSummaryResponse? = null
)

class WalkerAdminController(
    private val userRepository: UserRepository
) {
    private fun toWalkerSummary(user: User): WalkerSummaryResponse =
        WalkerSummaryResponse(
            id = user.id.toString(),
            email = user.email,
            name = user.name,
            phone = user.phone,
            status = user.status.name
        )

    private fun isAdmin(principal: JWTPrincipal?): Boolean {
        if (principal == null) return false
        val isAdminClaim = principal.getClaim("is_admin", Boolean::class)
        return isAdminClaim == true
    }

    suspend fun listPending(call: ApplicationCall, principal: JWTPrincipal?) {
        if (!isAdmin(principal)) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerAdminActionResponse(
                    success = false,
                    message = "No tienes permisos de administrador."
                )
            )
            return
        }

        val walkers = userRepository.findPendingWalkers()
        val summaries = walkers.map(::toWalkerSummary)

        call.respond(HttpStatusCode.OK, summaries)
    }

    suspend fun approve(call: ApplicationCall, principal: JWTPrincipal?) {
        if (!isAdmin(principal)) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerAdminActionResponse(
                    success = false,
                    message = "No tienes permisos de administrador."
                )
            )
            return
        }

        val idParam = call.parameters["id"]
        val walkerId = runCatching { UUID.fromString(idParam) }.getOrNull()
        if (walkerId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerAdminActionResponse(false, "ID de paseador inválido.")
            )
            return
        }

        val updated = userRepository.updateWalkerStatus(walkerId, UserStatus.ACTIVE)
        if (updated == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerAdminActionResponse(false, "Paseador no encontrado.")
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            WalkerAdminActionResponse(
                success = true,
                message = "Paseador aprobado correctamente.",
                walker = toWalkerSummary(updated)
            )
        )
    }

    suspend fun reject(call: ApplicationCall, principal: JWTPrincipal?) {
        if (!isAdmin(principal)) {
            call.respond(
                HttpStatusCode.Forbidden,
                WalkerAdminActionResponse(
                    success = false,
                    message = "No tienes permisos de administrador."
                )
            )
            return
        }

        val idParam = call.parameters["id"]
        val walkerId = runCatching { UUID.fromString(idParam) }.getOrNull()
        if (walkerId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                WalkerAdminActionResponse(false, "ID de paseador inválido.")
            )
            return
        }

        val updated = userRepository.updateWalkerStatus(walkerId, UserStatus.INACTIVE)
        if (updated == null) {
            call.respond(
                HttpStatusCode.NotFound,
                WalkerAdminActionResponse(false, "Paseador no encontrado.")
            )
            return
        }

        call.respond(
            HttpStatusCode.OK,
            WalkerAdminActionResponse(
                success = true,
                message = "Paseador rechazado / desactivado.",
                walker = toWalkerSummary(updated)
            )
        )
    }
}