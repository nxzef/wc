package com.nxzef.wc.domain.usecase.invoices

import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.util.AppResult

class GetAllInvoicesUseCase(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke() = repository.getAll()
}