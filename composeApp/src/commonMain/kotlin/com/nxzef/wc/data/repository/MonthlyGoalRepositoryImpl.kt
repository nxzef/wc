package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.MonthlyGoalService
import com.nxzef.wc.domain.repository.MonthlyGoalRepository
import com.nxzef.wc.shared.model.MonthlyGoal
import com.nxzef.wc.shared.model.UpsertMonthlyGoalRequest
import com.nxzef.wc.shared.util.AppResult

class MonthlyGoalRepositoryImpl(
    private val service: MonthlyGoalService
) : MonthlyGoalRepository {

    override suspend fun upsert(request: UpsertMonthlyGoalRequest): AppResult<MonthlyGoal> {
        return try {
            AppResult.Success(service.upsert(request))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
