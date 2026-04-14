package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.LeadService
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.Lead

class LeadRepositoryImpl(
    private val leadService: LeadService
) : LeadRepository {
    override suspend fun getAllLeads(): Result<List<Lead>> {
        return try {
            Result.success(leadService.getAllLeads())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLeadStatus(id: String, status: String, notes: String?): Result<Lead> {
        return try {
            Result.success(leadService.updateLeadStatus(id, status, notes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
