package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.AppResult

interface LeadRepository {
    suspend fun getAllLeads(): AppResult<List<Lead>>
    suspend fun updateLeadStatus(id: String, status: String, notes: String?): AppResult<Lead>
}
