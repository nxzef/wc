package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
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
import java.time.LocalDate

fun Route.quoteRoutes(
    quoteRepository: QuoteRepository,
    leadRepository: LeadRepository,
    bookingRepository: BookingRepository,
    taskRepository: TaskRepository
) {
    route("/quotes") {

        // GET quotes by lead id
        get("/lead/{leadId}") {
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing leadId"
                )
            val quotes = quoteRepository.getByLeadId(leadId)
            call.respond(quotes.map { it.toDto() })
        }

        // GET quote by id
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val quote = quoteRepository.getById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Quote not found"
                )
            call.respond(quote.toDto())
        }

        // POST create quote
        post {
            val principal = call.principal<JWTPrincipal>()
            val createdBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    "Unauthorized"
                )
            val request = call.receive<CreateQuoteRequest>()
            val quote = quoteRepository.create(request, createdBy)
            call.respond(HttpStatusCode.Created, quote.toDto())
        }

        // PUT update quote status
        put("/{id}/status") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@put call.respond(
                    HttpStatusCode.Unauthorized,
                    "Unauthorized"
                )

            val request = call.receive<UpdateQuoteStatusRequest>()
            val quote = quoteRepository.updateStatus(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Quote not found"
                )

            // Automation: If quote is accepted, create a booking
            if (request.status == QuoteStatus.ACCEPTED) {
                val lead = leadRepository.getById(quote.leadId)
                if (lead != null) {
                    // Check if booking already exists for this lead to avoid duplicates
                    val existingBookings = bookingRepository.getAll()
                    val alreadyBooked = existingBookings.any { it.leadId == lead.id }

                    if (!alreadyBooked) {
                        val booking = bookingRepository.create(
                            CreateBookingRequest(
                                leadId = lead.id,
                                quoteId = quote.id,
                                eventDate = lead.eventDate ?: LocalDate.now().toString(),
                                eventType = lead.eventType.name,
                                location = lead.location ?: "TBD",
                                notes = "Automatically created from accepted quote #${quote.id.takeLast(4)}"
                            )
                        )

                        // Create default tasks for the new booking, assigned to the person who accepted it
                        taskRepository.createDefaultBookingTasks(
                            bookingId = booking.id,
                            assignedTo = userId,
                            createdBy = userId
                        )
                    }
                }
            }

            call.respond(quote.toDto())
        }
    }
}