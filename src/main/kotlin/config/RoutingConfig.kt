package com.upet.config

import com.upet.auth.AuthController
import com.upet.auth.AuthService
import com.upet.auth.JwtProvider
import com.upet.auth.authRoutes
import com.upet.media.MediaFilesRepository
import com.upet.notifications.NotificationService
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
import com.upet.routes.mapsHealthRoutes
import com.upet.tracking.FirestoreTrackingRepository
import com.upet.tracking.TrackingAccessService
import com.upet.tracking.TrackingController
import com.upet.tracking.TrackingService
import com.upet.tracking.WalkTrackSummariesRepository
import com.upet.tracking.trackingRoutes
import com.upet.users.UserRepository
import com.upet.users.UsersController
import com.upet.users.UsersFcmController
import com.upet.users.UsersFcmRepository
import com.upet.users.userFcmRoutes
import com.upet.users.userRoutes
import com.upet.walkers.WalkerAdminController
import com.upet.walkers.WalkerProfileRepository
import com.upet.walkers.WalkerSelfController
import com.upet.walkers.walkerAdminRoutes
import com.upet.walkers.walkerRoutes
import com.upet.walks.DummyRouteProvider
import com.upet.walks.GoogleDirectionsRouteProvider
import com.upet.walks.RouteProvider
import com.upet.walks.WalkController
import com.upet.walks.WalkExecutionController
import com.upet.walks.WalkExecutionService
import com.upet.walks.WalkRepository
import com.upet.walks.WalkService
import com.upet.walks.walkExecutionRoutes
import com.upet.walks.walkRoutes
import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(httpClient: HttpClient) {
    val apiKey = environment.config
        .propertyOrNull("upet.googleMaps.apiKey")
        ?.getString()

    val useGoogle = environment.config
        .propertyOrNull("upet.routes.provider")
        ?.getString()
        ?.equals("google", ignoreCase = true) == true

    val routeProvider: RouteProvider =
        if (useGoogle && !apiKey.isNullOrBlank()) {
            GoogleDirectionsRouteProvider(httpClient, apiKey)
        } else {
            DummyRouteProvider()
        }

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

    val usersFcmRepository = UsersFcmRepository()
    val usersFcmController = UsersFcmController(usersFcmRepository)
    val notificationService = NotificationService()

    val walkRepository = WalkRepository()
    val walkService = WalkService(walkRepository, routeProvider, usersFcmRepository, notificationService)
    val walkController = WalkController(walkService)

    val mediaFilesRepository = MediaFilesRepository()
    val summariesRepo = WalkTrackSummariesRepository()
    val firestoreTrackingRepo = FirestoreTrackingRepository()

    val trackingService = TrackingService(firestoreTrackingRepo, summariesRepo)
    val trackingAccessService = TrackingAccessService(walkRepository, summariesRepo)

    val walkExecutionService = WalkExecutionService(
        walkRepository = walkRepository,
        mediaFilesRepository = mediaFilesRepository,
        trackingService = trackingService,
        usersFcmRepository = usersFcmRepository,
        notificationService = notificationService
    )

    val walkExecutionController = WalkExecutionController(walkService, walkExecutionService)
    val trackingController = TrackingController(trackingService, trackingAccessService)

    routing {
        healthRoutes()
        dbHealthRoutes()
        firebaseHealthRoutes()
        mapsHealthRoutes(routeProvider, !apiKey.isNullOrBlank())

        authRoutes(authController)
        walkerAdminRoutes(walkerAdminController)
        paymentMethodRoutes(paymentMethodRepository)

        petRoutes(petController)
        userRoutes(usersController)
        userFcmRoutes(usersFcmController)
        walkerRoutes(walkerSelfController, userRepository)

        clientPaymentMethodsRoutes(clientPaymentMethodsController)
        walkerPaymentMethodsRoutes(walkerPaymentMethodsController)

        walkRoutes(walkController)

        walkExecutionRoutes(walkExecutionController)
        trackingRoutes(trackingController)
    }
}
