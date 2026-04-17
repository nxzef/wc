package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest

interface InvoiceRepository {
    suspend fun getAll(): Result<List<Invoice>>
    suspend fun getByBookingId(bookingId: String): Result<Invoice>
    suspend fun create(request: CreateInvoiceRequest): Result<Invoice>
    suspend fun updatePayment(id: String, request: UpdatePaymentRequest): Result<Invoice>
}