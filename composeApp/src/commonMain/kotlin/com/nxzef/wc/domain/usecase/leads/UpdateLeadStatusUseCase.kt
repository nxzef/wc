package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.Lead

class UpdateLeadStatusUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(
        id: String,
        status: String,
        notes: String? = null
    ): Result<Lead> {
        return repository.updateLeadStatus(id, status, notes)
    }
}