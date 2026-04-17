package com.nxzef.wc.domain.usecase.dashboard

import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.util.AppResult

class GetDashboardStatsUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): AppResult<DashboardStats> {
        return repository.getDashboardStats()
    }
}