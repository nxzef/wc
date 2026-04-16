package com.nxzef.wc.domain.usecase.dashboard

import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.shared.model.DashboardStats

class GetDashboardStatsUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): Result<DashboardStats> {
        return repository.getDashboardStats()
    }
}