package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.AppResult

class GetAllLeadsUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(): AppResult<List<Lead>> {
        return repository.getAllLeads()
    }
}