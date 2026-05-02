package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.UpdateLeadStatusRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.leadRoutes(
    leadRepository: LeadRepository,
    taskRepository: TaskRepository,
    notificationService: NotificationService
) {
    route("/leads") {

        get {
            val leads = leadRepository.getAll()
            call.respond(leads.map { it.toDto() })
        }

        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val lead = leadRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Lead not found")
            call.respond(lead.toDto())
        }

        post {
            val principal = call.principal<JWTPrincipal>()
            val addedBy = principal?.payload?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            val request = call.receive<CreateLeadRequest>()
            val lead = leadRepository.create(request, addedBy)

            taskRepository.createDefaultLeadTasks(
                leadId = lead.id,
                assignedTo = request.assignedTo,
                createdBy = addedBy
            )

            notificationService.notify(
                userId = request.assignedTo,
                title = "New Lead Assigned",
                message = "${lead.fullName} has been assigned to you"
            )

            call.respond(HttpStatusCode.Created, lead.toDto())
        }

        put("/{id}/status") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val request = call.receive<UpdateLeadStatusRequest>()
            val lead = leadRepository.updateStatus(id, request)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Lead not found")
            call.respond(lead.toDto())
        }
    }
}
