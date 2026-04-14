package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.DashboardService
import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.shared.model.DashboardStats

class DashboardRepositoryImpl(
    private val dashboardService: DashboardService
) : DashboardRepository {
    override suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            Result.success(dashboardService.getDashboardStats())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
