package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.InvoiceService
import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.UpdatePaymentRequest

class InvoiceRepositoryImpl(
    private val service: InvoiceService
) : InvoiceRepository {

    override suspend fun getAll() =
        runCatching { service.getAll() }

    override suspend fun getByBookingId(bookingId: String) =
        runCatching { service.getByBookingId(bookingId) }

    override suspend fun create(request: CreateInvoiceRequest) =
        runCatching { service.create(request) }

    override suspend fun updatePayment(
        id: String,
        request: UpdatePaymentRequest
    ) = runCatching { service.updatePayment(id, request) }
}