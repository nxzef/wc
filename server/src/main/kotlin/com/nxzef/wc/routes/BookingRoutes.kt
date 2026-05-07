package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.bookingRoutes(
    bookingRepository: BookingRepository,
    leadRepository: LeadRepository,
    notificationService: NotificationService
) {
    route("/bookings") {

        get {
            val teamId = call.requireTeamId() ?: return@get
            val bookings = bookingRepository.getAll(teamId)
            call.respond(bookings.map { it.toDto() })
        }

        get("/{id}") {
            val teamId = call.requireTeamId() ?: return@get
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val booking = bookingRepository.getById(id, teamId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Booking not found")
            call.respond(booking.toDto())
        }

        get("/photographer/{photographerId}") {
            val teamId = call.requireTeamId() ?: return@get
            val photographerId = call.parameters["photographerId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing photographerId")
            val bookings = bookingRepository.getByPhotographer(photographerId, teamId)
            call.respond(bookings.map { it.toDto() })
        }

        get("/editor/{editorId}") {
            val teamId = call.requireTeamId() ?: return@get
            val editorId = call.parameters["editorId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing editorId")
            val bookings = bookingRepository.getByEditor(editorId, teamId)
            call.respond(bookings.map { it.toDto() })
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            call.requireUserId() ?: return@post
            val request = call.receive<CreateBookingRequest>()
            val booking = bookingRepository.create(request, teamId)
            call.respond(HttpStatusCode.Created, booking.toDto())
        }

        put("/{id}") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val request = call.receive<UpdateBookingRequest>()

            val oldBooking = bookingRepository.getById(id, teamId)

            val booking = bookingRepository.update(id, request, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Booking not found")

            val ownerId = notificationService.getOwnerId(teamId)

            val newPhotographerId = request.photographerId
            if (newPhotographerId != null && newPhotographerId != oldBooking?.photographerId) {
                notificationService.notify(
                    userId = newPhotographerId,
                    title = "New Shoot Assigned",
                    message = "You have been assigned to shoot ${booking.eventType} on ${booking.eventDate} at ${booking.location}",
                    teamId = teamId,
                    bookingId = booking.id
                )
            }

            if (request.status != null && request.status != oldBooking?.status) {
                when (request.status) {
                    BookingStatus.SHOOT_DONE -> {
                        notificationService.getEditors(teamId).forEach { editorId ->
                            notificationService.notify(
                                userId = editorId,
                                title = "New editing job",
                                message = "${booking.eventType} shoot is ready for editing",
                                teamId = teamId,
                                bookingId = booking.id
                            )
                        }
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Shoot completed",
                                message = "Shoot completed for ${booking.eventType} on ${booking.eventDate}",
                                teamId = teamId,
                                bookingId = booking.id
                            )
                        }
                    }
                    BookingStatus.DELIVERED -> {
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Gallery delivered",
                                message = "Gallery delivered for ${booking.eventType} on ${booking.eventDate}",
                                teamId = teamId,
                                bookingId = booking.id
                            )
                        }
                        val lead = leadRepository.getById(booking.leadId, teamId)
                        lead?.let {
                            notificationService.notify(
                                userId = it.assignedTo,
                                title = "Gallery delivered",
                                message = "Gallery delivered — collect final payment from client",
                                teamId = teamId,
                                bookingId = booking.id
                            )
                        }
                    }
                    else -> {}
                }
            }

            call.respond(booking.toDto())
        }
    }
}
