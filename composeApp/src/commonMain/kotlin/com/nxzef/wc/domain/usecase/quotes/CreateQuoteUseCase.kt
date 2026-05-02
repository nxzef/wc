package com.nxzef.wc.domain.usecase.quotes

import com.nxzef.wc.domain.repository.QuoteRepository
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.SendQuoteRequest
import com.nxzef.wc.shared.util.AppResult

class SendQuoteUseCase(private val repository: QuoteRepository) {
    suspend operator fun invoke(request: SendQuoteRequest): AppResult<Quote> {
        return repository.sendQuote(request)
    }
}
