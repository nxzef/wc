package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Lead

interface LeadRepository {
    suspend fun getAllLeads(): Result<List<Lead>>
    suspend fun updateLeadStatus(id: String, status: String, notes: String?): Result<Lead>
}
