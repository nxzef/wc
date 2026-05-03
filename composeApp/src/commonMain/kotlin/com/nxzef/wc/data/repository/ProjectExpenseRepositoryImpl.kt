package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.ProjectExpenseService
import com.nxzef.wc.domain.repository.ProjectExpenseRepository
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.model.ProjectExpense
import com.nxzef.wc.shared.util.AppResult

class ProjectExpenseRepositoryImpl(
    private val service: ProjectExpenseService
) : ProjectExpenseRepository {

    override suspend fun getByBookingId(bookingId: String): AppResult<List<ProjectExpense>> {
        return try {
            AppResult.Success(service.getByBookingId(bookingId))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun create(request: CreateProjectExpenseRequest): AppResult<ProjectExpense> {
        return try {
            AppResult.Success(service.create(request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        return try {
            service.delete(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
