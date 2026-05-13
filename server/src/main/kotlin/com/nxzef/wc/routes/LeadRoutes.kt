package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateBookingRequest
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
import java.time.LocalDate

fun Route.leadRoutes(
    leadRepository: LeadRepository,
    bookingRepository: BookingRepository,
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

            // When manually moved to WON — create a booking if one doesn't already exist.
            // A booking may already exist if the lead had an accepted quote earlier.
            if (lead.statusName.equals("WON", ignoreCase = true)) {
                val alreadyBooked = bookingRepository.getAll(teamId).any { it.leadId == lead.id }
                if (!alreadyBooked) {
                    bookingRepository.create(
                        CreateBookingRequest(
                            leadId = lead.id,
                            eventDate = lead.eventDate ?: LocalDate.now().toString(),
                            eventType = lead.eventType.name,
                            location = lead.location ?: "TBD",
                            notes = "Automatically created when lead was marked WON"
                        ),
                        teamId = teamId
                    )
                }
            }

            call.respond(lead.toDto())
        }
    }
}
