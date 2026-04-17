package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.util.AppResult

interface DashboardRepository {
    suspend fun getDashboardStats(): AppResult<DashboardStats>
}
