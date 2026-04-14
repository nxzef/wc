package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.DashboardStats

interface DashboardRepository {
    suspend fun getDashboardStats(): Result<DashboardStats>
}
