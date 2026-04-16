package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.Lead

class GetAllLeadsUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(): Result<List<Lead>> {
        return repository.getAllLeads()
    }
}