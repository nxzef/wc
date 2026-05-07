package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.UpdateLeadStatusRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.leadRoutes(
    leadRepository: LeadRepository,
    notificationService: NotificationService
) {
    route("/leads") {

        get {
            val teamId = call.requireTeamId() ?: return@get
            val leads = leadRepository.getAll(teamId)
            call.respond(leads.map { it.toDto() })
        }

        get("/{id}") {
            val teamId = call.requireTeamId() ?: return@get
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val lead = leadRepository.getById(id, teamId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Lead not found")
            call.respond(lead.toDto())
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            val addedBy = call.requireUserId() ?: return@post
            val request = call.receive<CreateLeadRequest>()
            val lead = leadRepository.create(request, addedBy, teamId)

            notificationService.notify(
                userId = request.assignedTo,
                title = "New Lead Assigned",
                message = "${lead.fullName} has been assigned to you",
                teamId = teamId
            )

            call.respond(HttpStatusCode.Created, lead.toDto())
        }

        put("/{id}/status") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val request = call.receive<UpdateLeadStatusRequest>()
            val lead = leadRepository.updateStatus(id, request, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Lead not found")
            call.respond(lead.toDto())
        }
    }
}
