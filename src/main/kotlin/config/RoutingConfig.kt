package com.upet.config

import com.upet.auth.AuthController
import com.upet.auth.AuthService
import com.upet.auth.JwtProvider
import com.upet.auth.authRoutes
import com.upet.payments.PaymentMethodRepository
import com.upet.payments.paymentMethodRoutes
import com.upet.routes.dbHealthRoutes
import com.upet.routes.firebaseHealthRoutes
import com.upet.routes.healthRoutes
import com.upet.users.UserRepository
import com.upet.walkers.WalkerAdminController
import com.upet.walkers.WalkerProfileRepository
import com.upet.walkers.walkerAdminRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val walkerProfileRepository = WalkerProfileRepository()
    val jwtProvider = JwtProvider(environment.config)

    val authService = AuthService(userRepository, jwtProvider, walkerProfileRepository)
    val authController = AuthController(authService, userRepository)

    val walkerAdminController = WalkerAdminController(userRepository)
    val paymentMethodRepository = PaymentMethodRepository()

    routing {
        healthRoutes()
        dbHealthRoutes()
        firebaseHealthRoutes()

        authRoutes(authController)
        walkerAdminRoutes(walkerAdminController)
        paymentMethodRoutes(paymentMethodRepository)
    }
}
