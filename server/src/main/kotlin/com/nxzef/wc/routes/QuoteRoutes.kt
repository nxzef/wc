package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.SendQuoteRequest
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
    taskRepository: TaskRepository,
    invoiceRepository: InvoiceRepository,
    notificationService: NotificationService,
    emailService: EmailService
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

        // POST send quote (PDF via base64 + email)
        post("/send") {
            val principal = call.principal<JWTPrincipal>()
            val createdBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")

            val request = call.receive<SendQuoteRequest>()
            val pdfBytes = java.util.Base64.getDecoder().decode(request.fileBase64)

            emailService.sendQuoteEmail(
                to = request.clientEmail,
                fileName = request.fileName,
                pdfBytes = pdfBytes
            )

            val quote = quoteRepository.sendQuote(
                leadId = request.leadId,
                createdByUserId = createdBy,
                fileName = request.fileName
            )

            val lead = leadRepository.getById(quote.leadId)
            val ownerId = notificationService.getOwnerId()
            if (lead != null) {
                ownerId?.let {
                    notificationService.notify(
                        userId = it,
                        title = "Quote Sent to ${lead.fullName}",
                        message = "A quote PDF was emailed to ${request.clientEmail}"
                    )
                }
            }

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

            val lead = leadRepository.getById(quote.leadId)
            val ownerId = notificationService.getOwnerId()

            if (lead != null) {
                when (request.status) {
                    QuoteStatus.SENT -> {
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Quote Sent to ${lead.fullName}",
                                message = "A quote for ${lead.fullName} has been marked as SENT"
                            )
                        }
                    }

                    QuoteStatus.ACCEPTED -> {
                        // Notify Owner
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Quote Accepted — ${lead.fullName} is now a client",
                                message = "The quote for ${lead.fullName} was accepted."
                            )
                        }
                        // Notify Coordinator (lead's assignedTo)
                        notificationService.notify(
                            userId = lead.assignedTo,
                            title = "Quote accepted. Booking created for ${lead.fullName}",
                            message = "The quote was accepted. A new booking has been generated."
                        )
                    }

                    else -> {}
                }
            }

            // Automation: If quote is accepted, create a booking
            if (request.status == QuoteStatus.ACCEPTED) {
                if (lead != null) {
                    // Check if booking already exists for this lead to avoid duplicates
                    val existingBookings = bookingRepository.getAll()
                    val alreadyBooked = existingBookings.any { it.leadId == lead.id }

                    if (!alreadyBooked) {
                        // 1. Create Booking
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

                        // 2. Create Invoice (Total from quote, 50% deposit)
                        invoiceRepository.create(
                            CreateInvoiceRequest(
                                bookingId = booking.id,
                                totalAmount = quote.totalAmount,
                                depositAmount = quote.totalAmount * 0.5,
                                notes = "Automatically generated from accepted quote"
                            )
                        )

                        // 3. Create default tasks for the new booking, assigned to the person who accepted it
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