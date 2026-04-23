package com.nxzef.wc.domain.usecase.leads

import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead

import com.nxzef.wc.shared.util.AppResult

class CreateLeadUseCase(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(
        request: CreateLeadRequest
    ): AppResult<Lead> = repository.create(request)
}