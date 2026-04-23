package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import com.nxzef.wc.shared.util.AppResult

interface QuoteRepository {
    suspend fun getByLeadId(leadId: String): AppResult<List<Quote>>
    suspend fun getById(id: String): AppResult<Quote>
    suspend fun create(request: CreateQuoteRequest): AppResult<Quote>
    suspend fun updateStatus(id: String, request: UpdateQuoteStatusRequest): AppResult<Quote>
}
