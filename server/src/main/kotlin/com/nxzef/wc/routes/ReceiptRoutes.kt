package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.ReceiptRepository
import com.nxzef.wc.shared.dto.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.receiptRoutes(receiptRepository: ReceiptRepository) {
    route("/receipts") {

        get("/invoice/{invoiceId}") {
            val invoiceId = call.parameters["invoiceId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing invoiceId")
            call.respond(receiptRepository.getByInvoiceId(invoiceId).map { it.toDto() })
        }

        get("/booking/{bookingId}") {
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            call.respond(receiptRepository.getByBookingId(bookingId).map { it.toDto() })
        }
    }
}
