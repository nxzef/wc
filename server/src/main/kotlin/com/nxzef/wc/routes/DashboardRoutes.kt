package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.DashboardRepository
import com.nxzef.wc.shared.dto.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.dashboardRoutes(dashboardRepository: DashboardRepository) {
    route("/dashboard") {
        get("/stats") {
            val stats = dashboardRepository.getStats()
            call.respond(stats.toDto())
        }
    }
}