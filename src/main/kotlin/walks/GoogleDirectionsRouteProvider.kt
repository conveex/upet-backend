package com.upet.walks

import com.upet.domain.model.WalkType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

class GoogleDirectionsRouteProvider(
    private val httpClient: HttpClient,
    private val apiKey: String
) : RouteProvider {

    private val json = Json { ignoreUnknownKeys = true }

    // Constantes para el algoritmo de generación de rutas, modificables
    // Ajustable, velocidad promedio caminando
    private val walkingSpeedKmH = 4.5

    // Candidatos generados y Final para candidatos de alto costo de procesamiento
    private val maxCandidateWaypoints = 42
    private val maxFinalEvaluations = 12

    // Diversidad, cuantos sectores se usarán para intentar generar rutas distintas
    private val sectorCount = 6 // 60° por sector

    // Holguras para las rutas de tiempo y distancia, son +/-
    private val timeSlackMin = 10
    private val distanceSlackKm = 0.1

    // Rutas diferentes sugeridas, máximo 3 inicialmente
    private val maxSuggestedRoutes = 3

    // Places: Límites para controlar el costo
    private val maxPlacesCandidates = 24
    private val placesRadiusMinMeters = 300
    private val placesRadiusMaxMeters = 5000

    override suspend fun calculateRoutes(request: CalculateRouteRequest): List<CalculatedRouteDto> {
        return when (request.type) {
            WalkType.A_TO_B, WalkType.PREDEFINED -> {
                val dest = request.destination
                    ?: throw IllegalArgumentException("El destino es requerido para rutas tipo A to B/Predefined.")

                val routes = callDirectionsAlternatives(
                    origin = request.origin,
                    dest = dest,
                    maxRoutes = maxSuggestedRoutes
                )

                if(routes.isEmpty()) {
                    throw IllegalStateException("No se pudo calcular ninguna ruta A to B con Google Directions.")
                }

                routes
            }

            WalkType.TIME -> {
                val minutes = request.timeMinutes
                    ?: throw IllegalArgumentException("El tiempo es requerido para paseos de tipo Time.")

                val minAccept = (minutes - timeSlackMin).coerceAtLeast(5)
                val maxAccept = minutes + timeSlackMin

                val routes = generateLoopRoutesByTime(
                    origin = request.origin,
                    targetMin = minutes,
                    minAccept = minAccept,
                    maxAccept = maxAccept
                )

                if(routes.isEmpty()) {
                    throw IllegalStateException("No se pudo calcular ninguna ruta tipo TIME dentro de la tolerancia.")
                }

                routes
            }

            WalkType.DISTANCE -> {
                val distanceKm = request.distanceKm
                    ?: throw IllegalArgumentException("La distancia en Km es requerida para las rutas de tipo Distance.")

                val minAccept = (distanceKm - distanceSlackKm).coerceAtLeast(0.2)
                val maxAccept = distanceKm + distanceSlackKm

                val routes = generateLoopRoutesByDistance(
                    origin = request.origin,
                    targetKm = distanceKm,
                    minAccept = minAccept,
                    maxAccept = maxAccept
                )

                if (routes.isEmpty()) {
                    throw IllegalStateException("No se pudo calcular ninguna ruta tipo DISTANCE dentro de tolerancia.")
                }

                routes
            }
        }
    }


    /*
    * Función para generar A TO B
    * con al menos 3 alternativas
    * */
    private suspend fun callDirectionsAlternatives(
        origin: LatLngDto,
        dest: LatLngDto,
        maxRoutes: Int
    ) : List<CalculatedRouteDto> {
        val originParam = "${origin.lat},${origin.lng}"
        val destParam = "${dest.lat},${dest.lng}"

        val responseText: String = httpClient.get("https://maps.googleapis.com/maps/api/directions/json") {
            parameter("origin", originParam)
            parameter("destination", destParam)
            parameter("mode", "walking")
            parameter("alternatives", "true")
            parameter("key", apiKey)
        }.body()

        val root = json.parseToJsonElement(responseText).jsonObject
        val status = root["status"]?.jsonPrimitive?.content
        if(status != "OK") return emptyList()

        val routesJson = root["routes"]?.jsonArray ?: return emptyList()
        if (routesJson.isEmpty()) return emptyList()

        return routesJson.take(maxRoutes).mapNotNull { routeEl ->
            parseDirectionsRouteToDto(routeEl.jsonObject)
        }
    }

    /*
    * Función para generar rutas por duración.
    * Genera loops A-W-A W calculado A origen.
    * Candidatos por Places con fallback sintético.
    * */
    private suspend fun generateLoopRoutesByTime(
        origin: LatLngDto,
        targetMin: Int,
        minAccept: Int,
        maxAccept: Int
    ) : List<CalculatedRouteDto> {

        // Estimamos distancia de ida para un loop
        val oneWayHours = (targetMin.toDouble() / 2.0) / 60.0
        val baseOneWayKm = walkingSpeedKmH * oneWayHours

        // Generamos candidatos alrededor de la distancia calculada
        // Primero Places, luego fallback sintético
        val candidates = generateCandidateWaypointsPlacesFirst(
            origin = origin,
            baseRadiusKm = baseOneWayKm,
            strategy = PlacesStrategy.TIME
        )

        // Primera iteración simple, nos quedamos con los que están cerca al objetivo
        val scored = mutableListOf<CandidateScore>()

        for (w in candidates) {
            val oneWay = callOneWayRoute(origin, w) ?: continue
            val estTotalMin = (oneWay.durationMin * 2).coerceAtLeast(1)
            val estError = abs(estTotalMin - targetMin).toDouble()
            scored.add(
                CandidateScore(
                    waypoint = w,
                    error = estError,
                    estDurationMin = estTotalMin,
                    estDistanceKm = oneWay.distanceKm * 2
                )
            )
        }

        if(scored.isEmpty()) return emptyList()

        // Tomamos los mejores para el filtro pesado
        val finalists = pickFinalistsBySector(origin, scored, maxFinalEvaluations)

        val realLoops = mutableListOf<LoopResult>()
        for (c in finalists) {
            val loop = callLoopRoute(origin, c.waypoint) ?: continue
            realLoops.add(
                LoopResult(
                    dto = loop,
                    sector = sectorIndex(origin, c.waypoint),
                    error = abs(loop.durationMin - targetMin).toDouble()
                )
            )
        }

        val filtered = realLoops
            .filter { it.dto.durationMin in minAccept..maxAccept }
            .sortedBy { it.error }

        val rankedForPick = filtered.ifEmpty { realLoops.sortedBy { it.error } }

        return pickDiverseTopK(rankedForPick.map { it.dto to it.sector }, maxSuggestedRoutes)
    }

    /*
    * Genera loops pero por distancia directamente
    * */
    private suspend fun generateLoopRoutesByDistance(
        origin: LatLngDto,
        targetKm: Double,
        minAccept: Double,
        maxAccept: Double
    ) : List<CalculatedRouteDto> {
        val baseOneWayKm = targetKm / 2.0

        val candidates = generateCandidateWaypointsPlacesFirst(
            origin = origin,
            baseRadiusKm = baseOneWayKm,
            strategy = PlacesStrategy.DISTANCE
        )

        val scored = mutableListOf<CandidateScore>()

        for(w in candidates) {
            val oneWay = callOneWayRoute(origin, w) ?: continue
            val estTotalKm = oneWay.distanceKm * 2.0
            val estError = abs(estTotalKm - targetKm)
            scored.add(
                CandidateScore(
                    waypoint = w,
                    error = estError,
                    estDurationMin = oneWay.durationMin * 2,
                    estDistanceKm = estTotalKm
                )
            )
        }

        if(scored.isEmpty()) return emptyList()

        val finalists = pickFinalistsBySector(origin, scored, maxFinalEvaluations)

        val realLoops = mutableListOf<LoopResult>()
        for (c in finalists) {
            val loop = callLoopRoute(origin, c.waypoint) ?: continue
            realLoops.add(
                LoopResult(
                    dto = loop,
                    sector = sectorIndex(origin, c.waypoint),
                    error = abs(loop.distanceKm - targetKm)
                )
            )
        }

        val filtered = realLoops
            .filter { it.dto.distanceKm in minAccept..maxAccept }
            .sortedBy { it.error }

        val rankedForPick = filtered.ifEmpty { realLoops.sortedBy { it.error } }

        return pickDiverseTopK(rankedForPick.map { it.dto to it.sector }, maxSuggestedRoutes)
    }

    /*
    * Funcion helper para convertir una direccion json dada en
    * nuestro Dto con Polyline duracion estimada y tiempo
    * */
    private fun parseDirectionsRouteToDto(routeObj: JsonObject): CalculatedRouteDto? {
        val overviewPolyline = routeObj["overview_polyline"]?.jsonObject
        val encoded = overviewPolyline?.get("points")?.jsonPrimitive?.content ?: return null

        val legs = routeObj["legs"]?.jsonArray ?: return null
        if(legs.isEmpty()) return null

        var distanceMetersTotal = 0
        var durationSecondsTotal = 0

        for(legEl in legs) {
            val leg = legEl.jsonObject
            val d = leg["distance"]?.jsonObject?.get("value")?.jsonPrimitive?.int ?: return null
            val t = leg["duration"]?.jsonObject?.get("value")?.jsonPrimitive?.int ?: return null
            distanceMetersTotal += d
            durationSecondsTotal += t
        }

        val distanceKm = distanceMetersTotal / 1000.0
        val durationMin = (durationSecondsTotal / 60.0).roundToInt().coerceAtLeast(1)

        return CalculatedRouteDto(
            polylineEncoded = encoded,
            distanceKm = distanceKm,
            durationMin = durationMin
        )
    }

    /*
    * Funcion para generar candidatos W
    * Waypoints de puntos destino para ciertas distancias aproximadas pero con
    * variaciones para dar opciones al cliente.
    * Generamos puntos en varios ángulos y varios radios alrededor de baseRadiusKm
    * que es la distancia aproximada.
    * */
    private fun generateCandidateWaypoints(
        origin: LatLngDto,
        baseRadiusKm: Double
    ) : List<LatLngDto> {

        // Anillos generados alrededor del radio base, mas cercano hasta mas lejano
        val ringMultipliers = listOf(0.65, 0.80, 1.00, 1.20, 1.40)

        // Ángulos de 30° en 30° osea 12 direcciones
        val bearings = (0 until 360 step 30).map { it.toDouble() }

        val raw = mutableListOf<LatLngDto>()

        for (m in ringMultipliers) {
            val rKm = (baseRadiusKm * m).coerceAtLeast(0.15) // Aplicamos los anillos
            for (b in bearings) {
                raw.add(destinationFromBearing(origin, rKm, b)) // Obtenemos el destino y lo añadimos a la lista
                if (raw.size >= maxCandidateWaypoints) break // Limitamos los candidatos
            }
            if(raw.size >= maxCandidateWaypoints) break // Limitamos pero a capa de anillo
        }

        return raw // Devolvemos TODOS los candidatos
    }

    /*
    * Intenta sacar W reales con Places (Parques) en un radio derivado del
    * base dado, si no alcanza a llenar las rutas mínimas, las rellena
    * con candidatos sintéticos. Dedupe para evitar repetidos.
    * */
    private suspend fun generateCandidateWaypointsPlacesFirst(
        origin: LatLngDto,
        baseRadiusKm: Double,
        strategy: PlacesStrategy
    ) : List<LatLngDto> {

        val multiplier = when (strategy) {
            PlacesStrategy.TIME -> 1.6
            PlacesStrategy.DISTANCE -> 1.2
        }

        val radiusMeters = ((baseRadiusKm * 1000.0) * multiplier).roundToInt()
            .coerceIn(placesRadiusMinMeters, placesRadiusMaxMeters)

        val parks = getNearbyPlacesCandidates(
            origin = origin,
            radiusMeters = radiusMeters,
            type = "park",
            keyword = null,
            maxResults = maxPlacesCandidates
        )

        val dogParks = if (parks.size < 10) {
            getNearbyPlacesCandidates(
                origin = origin,
                radiusMeters = radiusMeters,
                type = null,
                keyword = "dog park",
                maxResults = maxPlacesCandidates
            )
        } else emptyList()

        val synthetic = generateCandidateWaypoints(
            origin = origin,
            baseRadiusKm = baseRadiusKm
        )

        return dedupeLatLng(parks + dogParks + synthetic).take(maxCandidateWaypoints)
    }

    /*
    * Places Nearby Search, devuelve una lista de puntos de resultados cercanos
    * */
    private suspend fun getNearbyPlacesCandidates(
        origin: LatLngDto,
        radiusMeters: Int,
        type: String?,
        keyword: String?,
        maxResults: Int
    ): List<LatLngDto> {
        val locationParam = "${origin.lat},${origin.lng}"

        val responseText: String = httpClient.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json") {
            parameter("location", locationParam)
            parameter("radius", radiusMeters.toString())
            if (type != null) parameter("type", type)
            if (keyword != null) parameter("keyword", keyword)
            parameter("key", apiKey)
        }.body()

        val root = json.parseToJsonElement(responseText).jsonObject
        val status = root["status"]?.jsonPrimitive?.content ?: return emptyList()

        if (status != "OK" && status != "ZERO_RESULTS") return emptyList()

        val results = root["results"]?.jsonArray ?: return emptyList()

        val out = mutableListOf<LatLngDto>()
        for (el in results) {
            val obj = el.jsonObject
            val geom = obj["geometry"]?.jsonObject ?: continue
            val loc = geom["location"]?.jsonObject ?: continue
            val lat = loc["lat"]?.jsonPrimitive?.double ?: continue
            val lng = loc["lng"]?.jsonPrimitive?.double ?: continue
            out.add(LatLngDto(lat = lat, lng = lng))
            if (out.size >= maxResults) break
        }
        return out
    }

    /*
    * Dedupe aproximado: Redondea el punto a 5 decimales para evitar
    * repetidos cuando Places y sintéticos caen cerca.
    *
    * */
    private fun dedupeLatLng(points: List<LatLngDto>): List<LatLngDto> {
        val seen = HashSet<String>()
        val out = ArrayList<LatLngDto>(points.size)

        for (point in points) {
            val key = "${round5(point.lat)}:${round5(point.lng)}"
            if(seen.add(key)) out.add(point)
        }
        return out
    }

    private fun round5(x: Double): Double = (x * 1e5).roundToInt() / 1e5

    /*
    * Calcula el destino por bearing + distancia
    * Es decir toma el ángulo y la distancia y calcula el fin de dicho vector
    * Lógicamente la tierra es esférica por lo que debemos de usar el
    * problema geodésico directo (forward geodesic problem) que son los calculos que se ven.
    * */
    private fun destinationFromBearing(
        origin: LatLngDto,
        distanceKm: Double,
        bearing: Double
    ) : LatLngDto {
        val radiusEarthKm = 6371.0
        val bearingRad = Math.toRadians(bearing)
        val lat1 = Math.toRadians(origin.lat)
        val lon1 = Math.toRadians(origin.lng)
        val dByR = distanceKm / radiusEarthKm

        val lat2 = asin(
            sin(lat1) * cos(dByR) + cos(lat1) * sin(dByR) * cos(bearingRad)
        )
        val lon2 = lon1 + atan2(
            sin(bearingRad) * sin(dByR) * cos(lat1),
            cos(dByR) - sin(lat1) * sin(lat2)
        )

        return LatLngDto(
            lat = Math.toDegrees(lat2),
            lng = Math.toDegrees(lon2)
        )
    }

    /*
    * Llamada a directions barata, primer iteracion de descarte
    * para los candidatos de waypoints A->W
    * */
    private suspend fun callOneWayRoute(
        origin: LatLngDto,
        dest: LatLngDto
    ) : CalculatedRouteDto? {
        return callDirectionsSingleRoute(
            origin = origin,
            dest = dest,
            waypoints = null
        )
    }

    /*
    * Llamada a directions cara, iteracion pesada de descarte para los
    * candidatos de waypoints A->W->A mas preciso
    * */
    private suspend fun callLoopRoute(
        origin: LatLngDto,
        waypoint: LatLngDto
    ) : CalculatedRouteDto? {
        val wParam = "${waypoint.lat},${waypoint.lng}"
        return callDirectionsSingleRoute(
            origin = origin,
            dest = origin,
            waypoints = wParam
        )
    }

    private suspend fun callDirectionsSingleRoute(
        origin: LatLngDto,
        dest: LatLngDto,
        waypoints: String?
    ) : CalculatedRouteDto? {
        val originParam = "${origin.lat},${origin.lng}"
        val destParam = "${dest.lat},${dest.lng}"

        val responseText: String = httpClient.get("https://maps.googleapis.com/maps/api/directions/json") {
            parameter("origin", originParam)
            parameter("destination", destParam)
            parameter("mode", "walking")
            parameter("alternatives", "false")
            if (waypoints != null) parameter("waypoints", waypoints) // Aqui se diferencia el filtro pesado del ligero
            parameter("key", apiKey)
        }.body()

        val root = json.parseToJsonElement(responseText).jsonObject
        val status = root["status"]?.jsonPrimitive?.content
        if (status != "OK") return null

        val routesJson = root["routes"]?.jsonArray ?: return null
        if (routesJson.isEmpty()) return null

        val route0 = routesJson[0].jsonObject
        return parseDirectionsRouteToDto(route0)
    }

    /*
    * Para asegurar la diversidad de las rutas seleccionadas tomamos el angulo
    * de A a W y lo metemos en sectorCount sectores.
    * */
    private fun sectorIndex(
        origin: LatLngDto,
        waypoint: LatLngDto
    ): Int {
        val angle = bearingDegrees(origin, waypoint)
        val sectorSize = 360.0 / sectorCount
        return floor(angle / sectorSize).toInt().coerceIn(0, sectorCount - 1)
    }

    private fun bearingDegrees(
        a: LatLngDto,
        b: LatLngDto
    ) : Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLon = Math.toRadians(b.lng - a.lng)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val brng = atan2(y, x)
        val deg = (Math.toDegrees(brng) + 360.0) % 360.0
        return deg
    }

    /*
    * Dado el listado de (dto, sector) ordenado por mejor calidad, se elige de cada
    * sector hasta k intentando variedad de sectores, k debe de ser menor a la cantidad de sectores
    * hay que tener cuidado con los parámetros iniciales y que tengan sentido, incialmente lo tienen
    * funcionaria igualmente, pero no como se espera a mayor k se esperan mayor cantidad de sectores.
    * */
    private fun pickDiverseTopK(
        ranked: List<Pair<CalculatedRouteDto, Int>>,
        k: Int
    ): List<CalculatedRouteDto> {
        if(ranked.isEmpty()) return emptyList()

        val chosen = mutableListOf<CalculatedRouteDto>()
        val usedSectors = mutableSetOf<Int>()

        for((dto, sector) in ranked) {
            if(sector !in usedSectors) {
                chosen.add(dto)
                usedSectors.add(sector)
                if(chosen.size == k) return chosen
            }
        }

        // Si no se llega a k se completa con lo que quede
        for((dto, _) in ranked) {
            if(chosen.size == k) break
            if(dto !in chosen) chosen.add(dto)
        }

        return chosen.take(k)
    }

    /*
    * Helper para obtener finalistas por sector
    * */
    private fun pickFinalistsBySector(
        origin: LatLngDto,
        scored: List<CandidateScore>,
        maxFinal: Int
    ): List<CandidateScore> {
        val bySector = scored.groupBy { sectorIndex(origin, it.waypoint) }
        val perSector = 2

        val picked = mutableListOf<CandidateScore>()
        val sectorsOrdered = bySector.keys.sorted()

        while (picked.size < maxFinal) {
            var added = false
            for (s in sectorsOrdered) {
                val list = bySector[s].orEmpty().sortedBy { it.error }
                val already = picked.count { sectorIndex(origin, it.waypoint) == s }
                if (already < perSector) {
                    val next = list.getOrNull(already) ?: continue
                    if (next !in picked) {
                        picked.add(next)
                        added = true
                        if (picked.size == maxFinal) break
                    }
                }
            }
            if (!added) break
        }

        // relleno por error si faltó
        if (picked.size < maxFinal) {
            picked.addAll(
                scored.sortedBy { it.error }
                    .filter { it !in picked }
                    .take(maxFinal - picked.size
                    )
            )
        }
        return picked.take(maxFinal)
    }

    // Modelos internos para puntaje de candidatos y selección
    private data class CandidateScore(
        val waypoint: LatLngDto,
        val error: Double,
        val estDurationMin: Int,
        val estDistanceKm: Double
    )

    private data class LoopResult(
        val dto: CalculatedRouteDto,
        val sector: Int,
        val error: Double
    )

    private enum class PlacesStrategy { TIME, DISTANCE }
}