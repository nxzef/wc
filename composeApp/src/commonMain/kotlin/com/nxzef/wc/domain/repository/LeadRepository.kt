package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.AppResult

interface LeadRepository {
    suspend fun getAll(): AppResult<List<Lead>>
    suspend fun updateLeadStatus(id: String, customStatusId: String, notes: String?): AppResult<Lead>
    suspend fun create(request: CreateLeadRequest): AppResult<Lead>
}
