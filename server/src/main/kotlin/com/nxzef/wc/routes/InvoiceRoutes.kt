package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.ReceiptRepository
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
    receiptRepository: ReceiptRepository
) {
    route("/invoices") {

        // GET all invoices
        get {
            val invoices = invoiceRepository.getAll()
            call.respond(invoices.map { it.toDto() })
        }

        // GET invoice by booking id
        get("/booking/{bookingId}") {
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            val invoice = invoiceRepository.getByBookingId(bookingId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Invoice not found")
            call.respond(invoice.toDto())
        }

        // PUT update payment status — auto-creates receipt on each payment
        put("/{id}/payment") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")

            val request = call.receive<UpdatePaymentRequest>()

            val existing = invoiceRepository.getById(id)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Invoice not found")

            val today = LocalDate.now().toString()

            // Auto-create ADVANCE receipt when deposit is first marked paid
            if (request.depositPaid == true && !existing.depositPaid) {
                receiptRepository.create(
                    invoiceId = id,
                    bookingId = existing.bookingId,
                    type      = ReceiptType.ADVANCE,
                    amount    = existing.depositAmount,
                    paidDate  = request.depositPaidDate ?: today
                )
            }

            // Auto-create FINAL receipt when balance is first marked paid
            if (request.finalPaid == true && !existing.finalPaid) {
                receiptRepository.create(
                    invoiceId = id,
                    bookingId = existing.bookingId,
                    type      = ReceiptType.FINAL,
                    amount    = existing.remainingAmount,
                    paidDate  = request.finalPaidDate ?: today
                )
            }

            val invoice = invoiceRepository.updatePayment(id, request)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Invoice not found")

            call.respond(invoice.toDto())
        }
    }
}
