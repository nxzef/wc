package com.nxzef.wc.domain.usecase.quotes

import com.nxzef.wc.domain.repository.QuoteRepository
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import com.nxzef.wc.shared.util.AppResult

class UpdateQuoteStatusUseCase(private val repository: QuoteRepository) {
    suspend operator fun invoke(id: String, request: UpdateQuoteStatusRequest): AppResult<Quote> {
        return repository.updateStatus(id, request)
    }
}
