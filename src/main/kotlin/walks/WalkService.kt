package com.upet.walks

import com.upet.domain.model.WalkType
import java.util.UUID
import kotlin.math.roundToInt

class WalkValidationException(message: String) : RuntimeException(message)

class WalkService(
    private val walkRepository: WalkRepository,
    private val routeProvider: RouteProvider
) {
    private val baseFareMxn = 25.0
    private val perKmMxn = 12.0

    private fun priceForDistanceKm(distanceKm: Double): Double {
        val raw = baseFareMxn + (distanceKm * perKmMxn)
        return (raw * 100.0).roundToInt() / 100.0
    }

    suspend fun calculateRoute(request: CalculateRouteRequest): List<CalculatedRouteDto> {
        when (request.type) {
            WalkType.A_TO_B -> if (request.destination == null) {
                throw WalkValidationException("Para paseos A_TO_B se requiere destino.")
            }
            WalkType.DISTANCE -> if (request.distanceKm == null || request.distanceKm <= 0.0) {
                throw WalkValidationException("Para paseos por DISTANCE se requiere una distancia válida.")
            }
            WalkType.TIME -> if (request.timeMinutes == null || request.timeMinutes <= 0) {
                throw WalkValidationException("Para paseos por TIME se requiere un tiempo válido.")
            }
            WalkType.PREDEFINED -> if (request.destination == null) {
                throw WalkValidationException("Para paseos PREDEFINED se requiere destino.")
            }
        }

        val providerRequest = when (request.type) {
            WalkType.TIME -> {
                val half = (request.timeMinutes!! / 2).coerceAtLeast(1)
                request.copy(timeMinutes = half)
            }
            WalkType.DISTANCE -> {
                val half = (request.distanceKm!! / 2.0).coerceAtLeast(0.1)
                request.copy(distanceKm = half)
            }
            else -> request
        }

        val routes = try {
            routeProvider.calculateRoutes(providerRequest)
        } catch (e: Exception) {
            throw WalkValidationException(e.message ?: "No fue posible calcular rutas.")
        }

        return routes.map { r ->
            val totalDistanceKm = r.distanceKm * 2.0
            val price = priceForDistanceKm(totalDistanceKm) * 1.5
            r.copy(priceAmount = price, priceCurrency = "MXN")
        }
    }

    fun createWalk(clientId: UUID, request: CreateWalkRequest): WalkDetailResponse {
        if (request.petIds.isEmpty()) throw WalkValidationException("Debes seleccionar al menos una mascota.")
        if (request.paymentMethodIds.isEmpty()) throw WalkValidationException("Debes seleccionar al menos un método de pago.")
        if (request.selectedRoutePolylineEncoded.isBlank()) throw WalkValidationException("Debes enviar selectedRoutePolylineEncoded.")

        val distM = request.estimatedDistanceMeters
        val durS = request.estimatedDurationSeconds
        if (distM == null || distM <= 0) throw WalkValidationException("Debes enviar estimatedDistanceMeters (>0).")
        if (durS == null || durS <= 0) throw WalkValidationException("Debes enviar estimatedDurationSeconds (>0).")

        when (request.type) {
            WalkType.A_TO_B -> {
                if (request.origin == null || request.destination == null) {
                    throw WalkValidationException("Para paseos A_TO_B se requieren origen y destino.")
                }
            }
            WalkType.DISTANCE -> if (request.origin == null) {
                throw WalkValidationException("Para paseos por DISTANCE se requiere origen.")
            }
            WalkType.TIME -> if (request.origin == null) {
                throw WalkValidationException("Para paseos por TIME se requiere origen.")
            }
            WalkType.PREDEFINED -> {
                if (request.predefinedRouteId == null) {
                    throw WalkValidationException("Para paseos PREDEFINED se requiere predefinedRouteId.")
                }
            }
        }

        val distanceKm = distM.toDouble() / 1000.0
        val serverPrice = priceForDistanceKm(distanceKm)

        return walkRepository.createWalk(
            clientId = clientId,
            request = request,
            priceAmount = serverPrice,
            priceCurrency = "MXN"
        )
    }

    fun getPendingSummariesByClient(clientId: UUID): List<WalkSummaryResponse> =
        walkRepository.findPendingSummariesByClient(clientId)

    fun getWalkDetailById(clientId: UUID, walkId: UUID): WalkDetailResponse? =
        walkRepository.findWalkDetailById(clientId, walkId)

    fun cancelPendingWalk(clientId: UUID, walkId: UUID): WalkDetailResponse? =
        walkRepository.cancelPendingWalk(clientId, walkId)
}