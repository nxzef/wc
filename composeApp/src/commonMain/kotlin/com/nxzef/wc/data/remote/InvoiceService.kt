package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.InvoiceDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InvoiceService(private val client: HttpClient) {

    suspend fun getAll(): List<Invoice> {
        val dtos: List<InvoiceDto> = client.get("${AppConfig.BASE_URL}/invoices") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getByBookingId(bookingId: String): Invoice {
        val dto: InvoiceDto = client.get(
            "${AppConfig.BASE_URL}/invoices/booking/$bookingId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dto.toDomain()
    }

    suspend fun create(request: CreateInvoiceRequest): Invoice {
        val dto: InvoiceDto = client.post("${AppConfig.BASE_URL}/invoices") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }

    suspend fun updatePayment(
        id: String,
        request: UpdatePaymentRequest
    ): Invoice {
        val dto: InvoiceDto = client.put(
            "${AppConfig.BASE_URL}/invoices/$id/payment"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }
}