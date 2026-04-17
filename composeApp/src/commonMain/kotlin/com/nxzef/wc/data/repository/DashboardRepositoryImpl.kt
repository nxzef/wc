package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.DashboardService
import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.util.AppResult

class DashboardRepositoryImpl(
    private val dashboardService: DashboardService
) : DashboardRepository {
    override suspend fun getDashboardStats(): AppResult<DashboardStats> {
        return try {
            AppResult.Success(dashboardService.getDashboardStats())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
