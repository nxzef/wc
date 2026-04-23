package com.nxzef.wc.domain.usecase.invoices

import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.util.AppResult

class GetInvoiceByBookingUseCase(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(bookingId: String) =
        repository.getByBookingId(bookingId)
}