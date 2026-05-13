package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import com.nxzef.wc.shared.util.AppResult

interface InvoiceRepository {
    suspend fun getAll(): AppResult<List<Invoice>>
    suspend fun getByBookingId(bookingId: String): AppResult<Invoice>
    suspend fun create(request: CreateInvoiceRequest): AppResult<Invoice>
    suspend fun updatePayment(id: String, request: UpdatePaymentRequest): AppResult<Pair<Invoice, Boolean>>
}