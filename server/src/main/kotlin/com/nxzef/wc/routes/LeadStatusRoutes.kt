package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateLeadStatusRequest
import com.nxzef.wc.shared.model.ReorderLeadStatusesRequest
import com.nxzef.wc.shared.model.UpdateLeadStatusPatchRequest
import com.nxzef.wc.shared.model.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.leadStatusRoutes(repository: LeadStatusRepository) {
    route("/lead-statuses") {

        get {
            val teamId = call.requireTeamId() ?: return@get
            call.respond(repository.getAll(teamId).map { it.toDto() })
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            if (call.role() != UserRole.OWNER.name) {
                return@post call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val request = call.receive<CreateLeadStatusRequest>()
            val name = request.name.trim()
            if (name.isEmpty()) {
                return@post call.respond(HttpStatusCode.BadRequest, "Status name is required")
            }
            if (repository.findByName(name, teamId) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "A status with this name already exists")
            }
            val status = repository.create(name, request.color, teamId)
            call.respond(HttpStatusCode.Created, status.toDto())
        }

        put("/reorder") {
            val teamId = call.requireTeamId() ?: return@put
            if (call.role() != UserRole.OWNER.name) {
                return@put call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val request = call.receive<ReorderLeadStatusesRequest>()
            repository.reorder(request.orderedIds, teamId)
            call.respond(HttpStatusCode.OK)
        }

        put("/{id}") {
            val teamId = call.requireTeamId() ?: return@put
            if (call.role() != UserRole.OWNER.name) {
                return@put call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val request = call.receive<UpdateLeadStatusPatchRequest>()

            val reqName = request.name
            if (reqName != null) {
                val trimmed = reqName.trim()
                if (trimmed.isEmpty()) {
                    return@put call.respond(HttpStatusCode.BadRequest, "Status name cannot be empty")
                }
                val existing = repository.findByName(trimmed, teamId)
                if (existing != null && existing.id != id) {
                    return@put call.respond(HttpStatusCode.Conflict, "A status with this name already exists")
                }
            }

            val updated = repository.update(id, reqName?.trim(), request.color, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Status not found")
            call.respond(updated.toDto())
        }

        delete("/{id}") {
            val teamId = call.requireTeamId() ?: return@delete
            if (call.role() != UserRole.OWNER.name) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Owner only")
            }
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
            when (repository.delete(id, teamId)) {
                "ok" -> call.respond(HttpStatusCode.NoContent)
                "is_default" -> call.respond(
                    HttpStatusCode.Forbidden,
                    "The default status cannot be deleted. You can rename it instead."
                )
                "only_one" -> call.respond(
                    HttpStatusCode.Forbidden,
                    "You must have at least one status."
                )
                "not_found" -> call.respond(HttpStatusCode.NotFound, "Status not found")
                else -> call.respond(HttpStatusCode.InternalServerError, "Delete failed")
            }
        }
    }
}
