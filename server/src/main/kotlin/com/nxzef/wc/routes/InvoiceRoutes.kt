package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.invoiceRoutes(invoiceRepository: InvoiceRepository) {
    route("/invoices") {

        // GET all invoices
        get {
            val invoices = invoiceRepository.getAll()
            call.respond(invoices)
        }

        // GET invoice by booking id
        get("/booking/{bookingId}") {
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing bookingId"
                )
            val invoice = invoiceRepository.getByBookingId(bookingId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Invoice not found"
                )
            call.respond(invoice)
        }

        // POST create invoice
        post {
            val request = call.receive<CreateInvoiceRequest>()
            val invoice = invoiceRepository.create(request)
            call.respond(HttpStatusCode.Created, invoice)
        }

        // PUT update payment status
        put("/{id}/payment") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing id"
                )
            val request = call.receive<UpdatePaymentRequest>()
            val invoice = invoiceRepository.updatePayment(id, request)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound,
                    "Invoice not found"
                )
            call.respond(invoice)
        }
    }
}