package com.nxzef.wc.domain.usecase.invoices

import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.shared.model.UpdatePaymentRequest

class UpdatePaymentUseCase(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(
        id: String,
        request: UpdatePaymentRequest
    ) = repository.updatePayment(id, request)
}