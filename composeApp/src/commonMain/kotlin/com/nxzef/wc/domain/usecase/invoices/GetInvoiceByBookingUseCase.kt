package com.nxzef.wc.domain.usecase.invoices

import com.nxzef.wc.domain.repository.InvoiceRepository

class GetInvoiceByBookingUseCase(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(bookingId: String) =
        repository.getByBookingId(bookingId)
}