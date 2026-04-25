package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.AppResult

class UpdateLeadStatusUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(
        id: String,
        status: LeadStatus,
        notes: String? = null
    ): AppResult<Lead> {
        return repository.updateLeadStatus(id, status, notes)
    }
}