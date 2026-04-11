package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.domain.model.CreateBookingRequest
import com.nxzef.wc.domain.model.UpdateBookingRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.bookingRoutes(bookingRepository: BookingRepository) {
    route("/bookings") {

        // GET all bookings
        get {
            val bookings = bookingRepository.getAll()
            call.respond(bookings)
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
            call.respond(booking)
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
            call.respond(bookings)
        }

        // GET bookings by editor
        get("/editor/{editorId}") {
            val editorId = call.parameters["editorId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing editorId"
                )
            val bookings = bookingRepository.getByEditor(editorId)
            call.respond(bookings)
        }

        // POST create booking (lead becomes WON)
        post {
            val request = call.receive<CreateBookingRequest>()
            val booking = bookingRepository.create(request)
            call.respond(HttpStatusCode.Created, booking)
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
            call.respond(booking)
        }
    }
}