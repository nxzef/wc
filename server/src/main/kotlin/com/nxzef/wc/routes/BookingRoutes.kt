package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.shared.dto.toDto
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
    taskRepository: TaskRepository
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

            // Auto create default tasks for this booking
            taskRepository.createDefaultBookingTasks(
                bookingId = booking.id,
                assignedTo = createdBy,
                createdBy = createdBy
            )

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
            val booking = bookingRepository.update(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Booking not found"
                )
            call.respond(booking.toDto())
        }
    }
}