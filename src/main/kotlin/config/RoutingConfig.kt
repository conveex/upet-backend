package com.upet.config

import com.upet.auth.AuthController
import com.upet.auth.AuthService
import com.upet.auth.JwtProvider
import com.upet.auth.authRoutes
import com.upet.payments.ClientPaymentMethodRepository
import com.upet.payments.ClientPaymentMethodsController
import com.upet.payments.PaymentMethodRepository
import com.upet.payments.WalkerPaymentMethodRepository
import com.upet.payments.WalkerPaymentMethodsController
import com.upet.payments.clientPaymentMethodsRoutes
import com.upet.payments.paymentMethodRoutes
import com.upet.payments.walkerPaymentMethodsRoutes
import com.upet.pets.PetController
import com.upet.pets.PetRepository
import com.upet.pets.PetService
import com.upet.pets.petRoutes
import com.upet.routes.dbHealthRoutes
import com.upet.routes.firebaseHealthRoutes
import com.upet.routes.healthRoutes
import com.upet.users.UserRepository
import com.upet.users.UsersController
import com.upet.users.userRoutes
import com.upet.walkers.WalkerAdminController
import com.upet.walkers.WalkerProfileRepository
import com.upet.walkers.WalkerSelfController
import com.upet.walkers.walkerAdminRoutes
import com.upet.walkers.walkerRoutes
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

    val petRepository = PetRepository()
    val petService = PetService(petRepository)
    val petController = PetController(petService)

    val usersController = UsersController(userRepository, authService)
    val walkerSelfController = WalkerSelfController(userRepository, walkerProfileRepository)

    val clientPaymentMethodRepository = ClientPaymentMethodRepository()
    val walkerPaymentMethodRepository = WalkerPaymentMethodRepository()
    val clientPaymentMethodsController = ClientPaymentMethodsController(clientPaymentMethodRepository)
    val walkerPaymentMethodsController = WalkerPaymentMethodsController(walkerPaymentMethodRepository)

    routing {
        healthRoutes()
        dbHealthRoutes()
        firebaseHealthRoutes()

        authRoutes(authController)
        walkerAdminRoutes(walkerAdminController)
        paymentMethodRoutes(paymentMethodRepository)

        petRoutes(petController)
        userRoutes(usersController)
        walkerRoutes(walkerSelfController, userRepository)

        clientPaymentMethodsRoutes(clientPaymentMethodsController)
        walkerPaymentMethodsRoutes(walkerPaymentMethodsController)
    }
}
