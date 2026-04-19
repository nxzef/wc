package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.LeadService
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.AppResult

class LeadRepositoryImpl(
    private val leadService: LeadService
) : LeadRepository {
    override suspend fun getAll(): AppResult<List<Lead>> {
        return try {
            AppResult.Success(leadService.getAllLeads())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun updateLeadStatus(
        id: String,
        status: String,
        notes: String?
    ): AppResult<Lead> {
        return try {
            AppResult.Success(leadService.updateLeadStatus(id, status, notes))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun create(request: CreateLeadRequest) =
        runCatching { leadService.create(request) }
}
