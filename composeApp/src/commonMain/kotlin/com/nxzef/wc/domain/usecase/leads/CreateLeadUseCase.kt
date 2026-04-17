package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead

class CreateLeadUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(
        request: CreateLeadRequest
    ): Result<Lead> = repository.create(request)
}