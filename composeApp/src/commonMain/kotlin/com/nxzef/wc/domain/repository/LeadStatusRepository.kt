package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.AppResult

interface LeadStatusRepository {
    suspend fun getAll(): AppResult<List<LeadStatus>>
    suspend fun create(name: String, color: String): AppResult<LeadStatus>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun update(id: String, name: String?, color: String?): AppResult<LeadStatus>
    suspend fun reorder(orderedIds: List<String>): AppResult<Unit>
}
