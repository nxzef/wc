package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateLeadStatusRequest
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

fun Route.leadStatusRoutes(repository: LeadStatusRepository) {
    route("/lead-statuses") {

        get {
            call.respond(repository.getAll().map { it.toDto() })
        }

        post {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()
            if (role != "OWNER") {
                return@post call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val request = call.receive<CreateLeadStatusRequest>()
            val status = repository.create(request.name, request.color)
            call.respond(HttpStatusCode.Created, status.toDto())
        }

        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()
            if (role != "OWNER") {
                return@delete call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
            val deleted = repository.delete(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.Conflict, "Cannot delete default status")
        }
    }
}
