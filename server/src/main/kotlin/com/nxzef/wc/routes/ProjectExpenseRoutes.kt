package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.ProjectExpenseRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.projectExpenseRoutes(expenseRepository: ProjectExpenseRepository) {
    route("/expenses") {

        get("/booking/{bookingId}") {
            val teamId = call.requireTeamId() ?: return@get
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            call.respond(expenseRepository.getByBookingId(bookingId, teamId).map { it.toDto() })
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            val userId = call.requireUserId() ?: return@post
            val request = call.receive<CreateProjectExpenseRequest>()
            val expense = expenseRepository.create(request, userId, teamId)
            call.respond(HttpStatusCode.Created, expense.toDto())
        }

        delete("/{id}") {
            val teamId = call.requireTeamId() ?: return@delete
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
            expenseRepository.delete(id, teamId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
