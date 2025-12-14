package com.upet.routes

import com.upet.domain.model.WalkType
import com.upet.walks.CalculateRouteRequest
import com.upet.walks.GoogleDirectionsRouteProvider
import com.upet.walks.LatLngDto
import com.upet.walks.RouteProvider
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable


fun Route.mapsHealthRoutes(routeProvider: RouteProvider, hasGoogleKey: Boolean) {
    get("/maps-health") {
        val origin = LatLngDto(19.4326, -99.1332)
        val dest   = LatLngDto(19.4270, -99.1677)

        val providerName = when (routeProvider) {
            is GoogleDirectionsRouteProvider -> "google"
            else -> "dummy"
        }

        val response = try {
            val routes = routeProvider.calculateRoutes(
                CalculateRouteRequest(
                    type = WalkType.A_TO_B,
                    origin = origin,
                    destination = dest
                )
            )

            MapsHealthResponse(
                provider = providerName,
                hasApiKey = hasGoogleKey,
                ok = true,
                routesCount = routes.size,
                firstRoute = routes.firstOrNull()?.let {
                    FirstRouteInfo(
                        distanceKm = it.distanceKm,
                        durationMin = it.durationMin
                    )
                }
            )
        } catch (e: Exception) {
            MapsHealthResponse(
                provider = providerName,
                hasApiKey = hasGoogleKey,
                ok = false,
                error = e.message ?: e::class.simpleName ?: "Unknown error"
            )
        }

        call.respond(response)
    }
}

@Serializable
data class MapsHealthResponse(
    val provider: String,
    val hasApiKey: Boolean,
    val ok: Boolean,
    val routesCount: Int? = null,
    val firstRoute: FirstRouteInfo? = null,
    val error: String? = null
)

@Serializable
data class FirstRouteInfo(
    val distanceKm: Double,
    val durationMin: Int
)