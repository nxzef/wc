package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.ProjectExpenseRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            call.respond(expenseRepository.getByBookingId(bookingId).map { it.toDto() })
        }

        post {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            val request = call.receive<CreateProjectExpenseRequest>()
            val expense = expenseRepository.create(request, userId)
            call.respond(HttpStatusCode.Created, expense.toDto())
        }

        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
            expenseRepository.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
