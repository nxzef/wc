package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.InvoiceWithClient
import com.nxzef.wc.data.repository.ReceiptRepository
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.ReceiptType
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.time.LocalDate

fun Route.invoiceRoutes(
    invoiceRepository: InvoiceRepository,
    receiptRepository: ReceiptRepository,
    emailService: EmailService
) {
    route("/invoices") {

        get {
            val teamId = call.requireTeamId() ?: return@get
            val invoices = invoiceRepository.getAll(teamId)
            call.respond(invoices.map { it.toDto() })
        }

        get("/booking/{bookingId}") {
            val teamId = call.requireTeamId() ?: return@get
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            val invoice = invoiceRepository.getByBookingId(bookingId, teamId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Invoice not found")
            call.respond(invoice.toDto())
        }

        put("/{id}/payment") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")

            val request = call.receive<UpdatePaymentRequest>()

            val details = invoiceRepository.getInvoiceWithClientDetails(id, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Invoice not found")
            val existing = details.invoice
            val today = LocalDate.now().toString()

            if (request.depositPaid == true && !existing.depositPaid) {
                val receipt = receiptRepository.create(
                    invoiceId = id,
                    bookingId = existing.bookingId,
                    type      = ReceiptType.ADVANCE,
                    amount    = existing.depositAmount,
                    paidDate  = request.depositPaidDate ?: today,
                    teamId    = teamId
                )
                sendReceiptEmail(
                    emailService = emailService,
                    details = details,
                    amount = existing.depositAmount,
                    paymentType = ReceiptType.ADVANCE.name,
                    receiptId = receipt.id
                )
            }

            if (request.finalPaid == true && !existing.finalPaid) {
                val receipt = receiptRepository.create(
                    invoiceId = id,
                    bookingId = existing.bookingId,
                    type      = ReceiptType.FINAL,
                    amount    = existing.remainingAmount,
                    paidDate  = request.finalPaidDate ?: today,
                    teamId    = teamId
                )
                sendReceiptEmail(
                    emailService = emailService,
                    details = details,
                    amount = existing.remainingAmount,
                    paymentType = ReceiptType.FINAL.name,
                    receiptId = receipt.id
                )
            }

            val invoice = invoiceRepository.updatePayment(id, request, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Invoice not found")

            call.respond(invoice.toDto())
        }
    }
}

private suspend fun sendReceiptEmail(
    emailService: EmailService,
    details: InvoiceWithClient,
    amount: Double,
    paymentType: String,
    receiptId: String
) {
    val email = details.clientEmail
    if (email.isNullOrBlank()) {
        println("⚠️  Skipping receipt email — lead ${details.clientName} has no email on file (invoice ${details.invoice.id}, $paymentType).")
        return
    }
    emailService.sendPaymentReceiptEmail(
        to = email,
        clientName = details.clientName,
        amount = amount,
        paymentType = paymentType,
        eventType = details.eventType,
        eventDate = details.eventDate,
        receiptId = receiptId
    )
}
