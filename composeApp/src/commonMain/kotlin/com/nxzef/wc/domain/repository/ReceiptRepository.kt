package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Receipt
import com.nxzef.wc.shared.util.AppResult

interface ReceiptRepository {
    suspend fun getByInvoiceId(invoiceId: String): AppResult<List<Receipt>>
    suspend fun getByBookingId(bookingId: String): AppResult<List<Receipt>>
}
