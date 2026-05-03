package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.MonthlyGoal
import com.nxzef.wc.shared.model.UpsertMonthlyGoalRequest
import com.nxzef.wc.shared.util.AppResult

interface MonthlyGoalRepository {
    suspend fun upsert(request: UpsertMonthlyGoalRequest): AppResult<MonthlyGoal>
}
