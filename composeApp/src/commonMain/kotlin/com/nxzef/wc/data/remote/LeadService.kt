package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.model.Lead
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LeadService(
    private val client: HttpClient,
    private val sessionManager: SessionManager
) {
    private val baseUrl = ApiClient.BASE_URL

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
