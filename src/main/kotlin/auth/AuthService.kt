package com.upet.auth

import com.upet.domain.model.UserStatus
import com.upet.users.UserRepository
import com.upet.users.domain.User
import com.upet.walkers.WalkerProfileRepository

data class AuthResult(
    val user: User,
    val token: String
)

class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val walkerProfileRepository: WalkerProfileRepository
) {

    fun register(request: RegisterUserRequest): Result<AuthResult> {
        if (request.email.isBlank() || request.password.length < 6 || request.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Email, nombre y contraseña son obligatorios (min 6 caracteres)."))
        }

        if (userRepository.existsByEmail(request.email)) {
            return Result.failure(IllegalStateException("Ya existe una cuenta registrada con este correo."))
        }

        val passwordHash = PasswordService.hash(request.password)

        val user = userRepository.createUser(
            request = request,
            passwordHash = passwordHash
        )

        val token = jwtProvider.generateToken(user)

        return Result.success(AuthResult(user, token))
    }

    fun login(request: LoginRequest): Result<AuthResult> {
        val user = userRepository.findByEmail(request.email)
            ?: return Result.failure(IllegalArgumentException("Credenciales inválidas."))

        if (!PasswordService.verify(request.password, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Credenciales inválidas."))
        }

        if (user.status == UserStatus.INACTIVE) {
            return Result.failure(IllegalStateException("La cuenta está inactiva."))
        }

        if (!user.emailVerified) {
            return Result.failure(IllegalStateException("Debes verificar tu correo electrónico antes de iniciar sesión."))
        }

        if (user.isWalker && user.status == UserStatus.PENDING_APPROVAL) {
            return Result.failure(
                IllegalStateException("Tu cuenta de paseador está pendiente de aprobación por un administrador.")
            )
        }

        val token = jwtProvider.generateToken(user)
        return Result.success(AuthResult(user, token))
    }

    fun registerWalker(request: RegisterWalkerRequest): Result<AuthResult> {
        if (request.email.isBlank() ||
            request.password.length < 6 ||
            request.name.isBlank()
        ) {
            return Result.failure(
                IllegalArgumentException("Email, nombre y contraseña son obligatorios (mínimo 6 caracteres).")
            )
        }

        if (request.serviceZoneLabel.isBlank()) {
            return Result.failure(
                IllegalArgumentException("La zona de servicio es obligatoria.")
            )
        }

        if (request.zoneRadiusKm <= 0.0) {
            return Result.failure(
                IllegalArgumentException("El radio de zona debe ser mayor a 0.")
            )
        }

        if (userRepository.existsByEmail(request.email)) {
            return Result.failure(
                IllegalStateException("Ya existe una cuenta registrada con este correo.")
            )
        }

        val passwordHash = PasswordService.hash(request.password)

        val user = userRepository.createUser(
            request = RegisterUserRequest(
                email = request.email,
                password = request.password,
                name = request.name,
                phone = request.phone,
                mainAddress = request.mainAddress,
                isClient = false,
                isWalker = true
            ),
            passwordHash = passwordHash,
            isAdmin = false
        )

        if (user.status != UserStatus.PENDING_APPROVAL) {

        }

        walkerProfileRepository.createProfileForUser(user.id, request)

        val token = jwtProvider.generateToken(user)

        return Result.success(AuthResult(user, token))
    }
}