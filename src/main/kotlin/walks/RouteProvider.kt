package com.upet.walks

interface RouteProvider {
    suspend fun calculateRoutes(request: CalculateRouteRequest): List<CalculatedRouteDto>
}