package com.nxzef.wc.domain.usecase.quotes

import com.nxzef.wc.domain.repository.QuoteRepository
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.util.AppResult

class GetQuotesByLeadIdUseCase(private val repository: QuoteRepository) {
    suspend operator fun invoke(leadId: String): AppResult<List<Quote>> {
        return repository.getByLeadId(leadId)
    }
}
