package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.InvoiceService
import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import com.nxzef.wc.shared.util.AppResult

class InvoiceRepositoryImpl(
    private val service: InvoiceService
) : InvoiceRepository {

    override suspend fun getAll(): AppResult<List<Invoice>> {
        return try {
            AppResult.Success(service.getAll())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByBookingId(bookingId: String): AppResult<Invoice> {
        return try {
            AppResult.Success(service.getByBookingId(bookingId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun create(request: CreateInvoiceRequest): AppResult<Invoice> {
        return try {
            AppResult.Success(service.create(request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun updatePayment(
        id: String,
        request: UpdatePaymentRequest
    ): AppResult<Pair<Invoice, Boolean>> {
        return try {
            AppResult.Success(service.updatePayment(id, request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}