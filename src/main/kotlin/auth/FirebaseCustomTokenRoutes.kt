package com.upet.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.upet.domain.model.UserStatus
import com.upet.users.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class FirebaseCustomTokenResponse(
    val success: Boolean,
    val message: String,
    val firebaseCustomToken: String? = null
)

fun Route.firebaseCustomTokenRoutes(
    userRepository: UserRepository
) {
    authenticate("auth-jwt") {

        get("/api/v1/auth/firebase-token") {

            // 1. Obtener el UUID del JWT de Ktor
            val userIdString = call.principal<JWTPrincipal>()
                ?.payload
                ?.getClaim("user_id")
                ?.asString()

            val userId = userIdString?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    FirebaseCustomTokenResponse(
                        success = false,
                        message = "Token de usuario inválido."
                    )
                )
                return@get
            }

            // 2. Consultar el usuario real en PostgreSQL
            val user = userRepository.findById(userId)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    FirebaseCustomTokenResponse(
                        success = false,
                        message = "Usuario no encontrado."
                    )
                )
                return@get
            }

            // 3. Validar que tenga acceso autorizado
            if (user.status != UserStatus.ACTIVE ||
                !user.emailVerified
            ) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    FirebaseCustomTokenResponse(
                        success = false,
                        message = "Usuario no habilitado para Firebase."
                    )
                )
                return@get
            }

            // 4. Comprobar disponibilidad de Firebase Admin
            if (FirebaseApp.getApps().isEmpty()) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    FirebaseCustomTokenResponse(
                        success = false,
                        message = "Firebase no está disponible."
                    )
                )
                return@get
            }

            // 5. Generar un Custom Token con la identidad de UPet
            val firebaseToken = try {
                FirebaseAuth.getInstance()
                    .createCustomToken(user.id.toString())
            } catch (e: Exception) {
                call.application.log.error(
                    "No fue posible generar el Firebase Custom Token.",
                    e
                )

                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    FirebaseCustomTokenResponse(
                        success = false,
                        message = "No fue posible generar la sesión Firebase."
                    )
                )
                return@get
            }

            // 6. Entregar el token al usuario autenticado
            call.respond(
                HttpStatusCode.OK,
                FirebaseCustomTokenResponse(
                    success = true,
                    message = "Firebase Custom Token generado correctamente.",
                    firebaseCustomToken = firebaseToken
                )
            )
        }
    }
}