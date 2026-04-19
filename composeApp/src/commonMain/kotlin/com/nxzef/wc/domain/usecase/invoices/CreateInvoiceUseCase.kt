package com.nxzef.wc.domain.usecase.invoices

import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.model.CreateInvoiceRequest

class CreateInvoiceUseCase(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(
        request: CreateInvoiceRequest
    ) = repository.create(request)
}