package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.DashboardStatsDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.DashboardStats
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class DashboardService(
    private val client: HttpClient,
    private val sessionManager: SessionManager
) {
    private val baseUrl = AppConfig.BASE_URL

    suspend fun getDashboardStats(): DashboardStats {
        val dto: DashboardStatsDto = client.get("$baseUrl/dashboard/stats") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
        }.body()
        return dto.toDomain()
    }
}
