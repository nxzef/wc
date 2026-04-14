package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiService(
    private val sessionManager: SessionManager
) {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.BASE_URL

    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun getDashboardStats(): DashboardStats {
        return client.get("$baseUrl/dashboard/stats") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
        }.body()
    }

    suspend fun getAllLeads(): List<Lead> {
        return client.get("$baseUrl/leads") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
        }.body()
    }

    suspend fun updateLeadStatus(
        id: String,
        status: String,
        notes: String? = null
    ): Lead {
        return client.put("$baseUrl/leads/$id/status") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "status" to status,
                    "notes" to (notes ?: "")
                )
            )
        }.body()
    }

}