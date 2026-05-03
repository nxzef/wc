package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
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

fun Route.bookingRoutes(
    bookingRepository: BookingRepository,
    leadRepository: LeadRepository,
    notificationService: NotificationService
) {
    route("/bookings") {

        // GET all bookings
        get {
            val bookings = bookingRepository.getAll()
            call.respond(bookings.map { it.toDto() })
        }

        // GET booking by id
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val booking = bookingRepository.getById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Booking not found"
                )
            call.respond(booking.toDto())
        }

        // GET bookings by photographer
        get("/photographer/{photographerId}") {
            val photographerId = call.parameters["photographerId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing photographerId"
                )
            val bookings = bookingRepository
                .getByPhotographer(photographerId)
            call.respond(bookings.map { it.toDto() })
        }

        // GET bookings by editor
        get("/editor/{editorId}") {
            val editorId = call.parameters["editorId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing editorId"
                )
            val bookings = bookingRepository.getByEditor(editorId)
            call.respond(bookings.map { it.toDto() })
        }

        // POST create booking (lead becomes WON)
        post {
            val principal = call.principal<JWTPrincipal>()
            val createdBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            val request = call.receive<CreateBookingRequest>()
            val booking = bookingRepository.create(request)

            call.respond(HttpStatusCode.Created, booking.toDto())
        }

        // PUT update booking
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val request = call.receive<UpdateBookingRequest>()
            
            // Get current booking to check for changes
            val oldBooking = bookingRepository.getById(id)
            
            val booking = bookingRepository.update(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Booking not found"
                )

            // Notifications
            val ownerId = notificationService.getOwnerId()

            // 1. Photographer assigned
            val newPhotographerId = request.photographerId
            if (newPhotographerId != null && newPhotographerId != oldBooking?.photographerId) {
                notificationService.notify(
                    userId = newPhotographerId,
                    title = "New Shoot Assigned",
                    message = "You have been assigned to shoot ${booking.eventType} on ${booking.eventDate} at ${booking.location}",
                    bookingId = booking.id
                )
            }

            // 2. Status changes
            if (request.status != null && request.status != oldBooking?.status) {
                when (request.status) {
                    BookingStatus.SHOOT_DONE -> {
                        // Notify Editors
                        notificationService.getEditors().forEach { editorId ->
                            notificationService.notify(
                                userId = editorId,
                                title = "New editing job",
                                message = "${booking.eventType} shoot is ready for editing",
                                bookingId = booking.id
                            )
                        }
                        // Notify Owner
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Shoot completed",
                                message = "Shoot completed for ${booking.eventType} on ${booking.eventDate}",
                                bookingId = booking.id
                            )
                        }
                    }
                    BookingStatus.DELIVERED -> {
                        // Notify Owner
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Gallery delivered",
                                message = "Gallery delivered for ${booking.eventType} on ${booking.eventDate}",
                                bookingId = booking.id
                            )
                        }
                        // Notify lead's assignedTo coordinator
                        val lead = leadRepository.getById(booking.leadId)
                        lead?.let {
                            notificationService.notify(
                                userId = it.assignedTo,
                                title = "Gallery delivered",
                                message = "Gallery delivered — collect final payment from client",
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