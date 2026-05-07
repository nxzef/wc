package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.ReceiptDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.Receipt
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class ReceiptService(private val client: HttpClient) {

    suspend fun getByInvoiceId(invoiceId: String): List<Receipt> {
        val dtos: List<ReceiptDto> = client.get(
            "${AppConfig.BASE_URL}/receipts/invoice/$invoiceId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getByBookingId(bookingId: String): List<Receipt> {
        val dtos: List<ReceiptDto> = client.get(
            "${AppConfig.BASE_URL}/receipts/booking/$bookingId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }
}
