package com.upet.walks

import com.upet.domain.model.WalkType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class DummyRouteProvider : RouteProvider {

    private val defaultSpeedKmPerHour = 4.0

    override suspend fun calculateRoutes(request: CalculateRouteRequest): List<CalculatedRouteDto> {
        when (request.type) {
            WalkType.A_TO_B -> {
                require(request.destination != null) { "Para paseos A_TO_B se requiere destino." }
            }
            WalkType.DISTANCE -> {
                require(request.distanceKm != null && request.distanceKm > 0.0) {
                    "Para paseos por DISTANCE se requiere una distancia válida."
                }
            }
            WalkType.TIME -> {
                require(request.timeMinutes != null && request.timeMinutes > 0) {
                    "Para paseos por TIME se requiere un tiempo válido."
                }
            }
            WalkType.PREDEFINED -> {
                require(request.destination != null) { "Para paseos PREDEFINED se requiere destino." }
            }
        }

        val baseDistanceKm = when (request.type) {
            WalkType.A_TO_B, WalkType.PREDEFINED -> {
                val dest = request.destination!!
                haversineKm(
                    request.origin.lat,
                    request.origin.lng,
                    dest.lat,
                    dest.lng
                )
            }
            WalkType.DISTANCE -> request.distanceKm!!
            WalkType.TIME -> {
                val minutes = request.timeMinutes!!.toDouble()
                (defaultSpeedKmPerHour * (minutes / 60.0))
            }
        }.coerceAtLeast(0.1)

        val baseDurationMin = when (request.type) {
            WalkType.TIME -> request.timeMinutes!!
            else -> ((baseDistanceKm / defaultSpeedKmPerHour) * 60.0)
                .roundToInt()
                .coerceAtLeast(5)
        }

        return listOf(0.9, 1.0, 1.1).map { factor ->
            val points = listOf(
                RoutePointDto(request.origin.lat, request.origin.lng),
                RoutePointDto(
                    lat = request.origin.lat + 0.001 * factor,
                    lng = request.origin.lng + 0.001 * factor
                ),
                if (request.destination != null) {
                    RoutePointDto(request.destination.lat, request.destination.lng)
                } else {
                    RoutePointDto(
                        lat = request.origin.lat + 0.002 * factor,
                        lng = request.origin.lng + 0.002 * factor
                    )
                }
            )

            CalculatedRouteDto(
                polylineEncoded = encodePolyline(points),
                distanceKm = baseDistanceKm * factor,
                durationMin = (baseDurationMin * factor).roundToInt().coerceAtLeast(5)
            )
        }
    }

    private fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) * cos(rLat1) * cos(rLat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }


    private fun encodePolyline(points: List<RoutePointDto>): String {
        var lastLat = 0
        var lastLng = 0
        val result = StringBuilder()

        for (p in points) {
            val lat = (p.lat * 1e5).roundToInt()
            val lng = (p.lng * 1e5).roundToInt()

            val dLat = lat - lastLat
            val dLng = lng - lastLng

            encodeSignedNumber(dLat, result)
            encodeSignedNumber(dLng, result)

            lastLat = lat
            lastLng = lng
        }

        return result.toString()
    }

    private fun encodeSignedNumber(num: Int, builder: StringBuilder) {
        var s = num shl 1
        if (num < 0) {
            s = s.inv()
        }
        encodeUnsignedNumber(s, builder)
    }

    private fun encodeUnsignedNumber(num: Int, builder: StringBuilder) {
        var n = num
        while (n >= 0x20) {
            val nextValue = (0x20 or (n and 0x1f)) + 63
            builder.append(nextValue.toChar())
            n = n shr 5
        }
        n += 63
        builder.append(n.toChar())
    }
}