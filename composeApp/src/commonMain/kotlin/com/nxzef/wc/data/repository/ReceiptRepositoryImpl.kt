package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.ReceiptService
import com.nxzef.wc.domain.repository.ReceiptRepository
import com.nxzef.wc.shared.model.Receipt
import com.nxzef.wc.shared.util.AppResult

class ReceiptRepositoryImpl(
    private val service: ReceiptService
) : ReceiptRepository {

    override suspend fun getByInvoiceId(invoiceId: String): AppResult<List<Receipt>> {
        return try {
            AppResult.Success(service.getByInvoiceId(invoiceId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getByBookingId(bookingId: String): AppResult<List<Receipt>> {
        return try {
            AppResult.Success(service.getByBookingId(bookingId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
