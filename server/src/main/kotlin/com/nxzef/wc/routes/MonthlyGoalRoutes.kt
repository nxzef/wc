package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.MonthlyGoalRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.UpsertMonthlyGoalRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.monthlyGoalRoutes(goalRepository: MonthlyGoalRepository) {
    route("/goals") {

        get("/{year}/{month}") {
            val year  = call.parameters["year"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid year")
            val month = call.parameters["month"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid month")
            val goal = goalRepository.getByMonthYear(month, year)
                ?: return@get call.respond(HttpStatusCode.NotFound, "No goal set")
            call.respond(goal.toDto())
        }

        post {
            val request = call.receive<UpsertMonthlyGoalRequest>()
            val goal = goalRepository.upsert(request.month, request.year, request.targetRevenue, request.targetProfit)
            call.respond(goal.toDto())
        }
    }
}
