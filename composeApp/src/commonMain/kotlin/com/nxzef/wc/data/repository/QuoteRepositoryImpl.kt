package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.QuoteService
import com.nxzef.wc.domain.repository.QuoteRepository
import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import com.nxzef.wc.shared.util.AppResult

class QuoteRepositoryImpl(private val service: QuoteService) : QuoteRepository {
    override suspend fun getByLeadId(leadId: String): AppResult<List<Quote>> = try {
        AppResult.Success(service.getByLeadId(leadId))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun getById(id: String): AppResult<Quote> = try {
        AppResult.Success(service.getById(id))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun create(request: CreateQuoteRequest): AppResult<Quote> = try {
        AppResult.Success(service.create(request))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun updateStatus(id: String, request: UpdateQuoteStatusRequest): AppResult<Quote> = try {
        AppResult.Success(service.updateStatus(id, request))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }
}
