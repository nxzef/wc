package com.nxzef.wc.domain.usecase.quotes

import com.nxzef.wc.domain.repository.QuoteRepository
import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.util.AppResult

class CreateQuoteUseCase(private val repository: QuoteRepository) {
    suspend operator fun invoke(request: CreateQuoteRequest): AppResult<Quote> {
        return repository.create(request)
    }
}
