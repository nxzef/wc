package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.SendQuoteRequest
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import io.ktor.http.HttpStatusCode
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
    invoiceRepository: InvoiceRepository,
    notificationService: NotificationService,
    emailService: EmailService
) {
    route("/quotes") {

        get("/lead/{leadId}") {
            val teamId = call.requireTeamId() ?: return@get
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing leadId")
            val quotes = quoteRepository.getByLeadId(leadId, teamId)
            call.respond(quotes.map { it.toDto() })
        }

        get("/{id}") {
            val teamId = call.requireTeamId() ?: return@get
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val quote = quoteRepository.getById(id, teamId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Quote not found")
            call.respond(quote.toDto())
        }

        post("/send") {
            val teamId = call.requireTeamId() ?: return@post
            val createdBy = call.requireUserId() ?: return@post

            val request = call.receive<SendQuoteRequest>()

            val quote = quoteRepository.sendQuote(
                leadId = request.leadId,
                createdByUserId = createdBy,
                fileName = request.fileName,
                totalAmount = request.totalAmount,
                notes = request.notes,
                teamId = teamId
            )

            val lead = leadRepository.getById(quote.leadId, teamId)

            emailService.sendQuoteEmail(
                to = request.clientEmail,
                clientName = lead?.fullName ?: "Client",
                pdfBase64 = request.fileBase64,
                fileName = request.fileName,
                notes = request.notes
            )

            val ownerId = notificationService.getOwnerId(teamId)
            if (lead != null) {
                ownerId?.let {
                    notificationService.notify(
                        userId = it,
                        title = "Quote Sent to ${lead.fullName}",
                        message = "A quote PDF was emailed to ${request.clientEmail}",
                        teamId = teamId
                    )
                }
            }

            call.respond(HttpStatusCode.Created, quote.toDto())
        }

        put("/{id}/status") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            call.requireUserId() ?: return@put

            val request = call.receive<UpdateQuoteStatusRequest>()
            val quote = quoteRepository.updateStatus(id, request, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Quote not found")

            val lead = leadRepository.getById(quote.leadId, teamId)
            val ownerId = notificationService.getOwnerId(teamId)

            if (lead != null) {
                when (request.status) {
                    QuoteStatus.SENT -> {
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Quote Sent to ${lead.fullName}",
                                message = "A quote for ${lead.fullName} has been marked as SENT",
                                teamId = teamId
                            )
                        }
                    }
                    QuoteStatus.ACCEPTED -> {
                        ownerId?.let {
                            notificationService.notify(
                                userId = it,
                                title = "Quote Accepted — ${lead.fullName}",
                                message = "The quote for ${lead.fullName} was accepted. A booking has been created.",
                                teamId = teamId
                            )
                        }
                        notificationService.notify(
                            userId = lead.assignedTo,
                            title = "Quote accepted for ${lead.fullName}",
                            message = "The quote was accepted and a booking has been generated.",
                            teamId = teamId
                        )
                    }
                    else -> {}
                }
            }

            if (request.status == QuoteStatus.ACCEPTED) {
                if (lead != null) {
                    val existingBookings = bookingRepository.getAll(teamId)
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
                            ),
                            teamId = teamId
                        )

                        invoiceRepository.create(
                            CreateInvoiceRequest(
                                bookingId = booking.id,
                                totalAmount = quote.totalAmount,
                                depositAmount = quote.totalAmount * 0.5,
                                notes = "Automatically generated from accepted quote"
                            ),
                            teamId = teamId
                        )
                    }
                }
            }

            call.respond(quote.toDto())
        }
    }
}
