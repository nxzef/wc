package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest

interface QuoteRepository {
    suspend fun getByLeadId(leadId: String): Result<List<Quote>>
    suspend fun getById(id: String): Result<Quote>
    suspend fun create(request: CreateQuoteRequest): Result<Quote>
    suspend fun updateStatus(id: String, request: UpdateQuoteStatusRequest): Result<Quote>
}