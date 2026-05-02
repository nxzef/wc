package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.LeadStatusService
import com.nxzef.wc.domain.repository.LeadStatusRepository
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.AppResult

class LeadStatusRepositoryImpl(private val service: LeadStatusService) : LeadStatusRepository {

    override suspend fun getAll(): AppResult<List<LeadStatus>> = try {
        AppResult.Success(service.getAll())
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun create(name: String, color: String): AppResult<LeadStatus> = try {
        AppResult.Success(service.create(name, color))
    } catch (e: Exception) {
        AppResult.Failure(e)
    }

    override suspend fun delete(id: String): AppResult<Unit> = try {
        service.delete(id)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Failure(e)
    }
}
