package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.LeadDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.UpdateLeadStatusRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
        val dtos: List<LeadDto> = client.get("$baseUrl/leads") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun updateLeadStatus(
        id: String,
        customStatusId: String,
        notes: String? = null
    ): Lead {
        val dto: LeadDto = client.put("$baseUrl/leads/$id/status") {
            header("Authorization", "Bearer ${sessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(UpdateLeadStatusRequest(customStatusId = customStatusId, notes = notes))
        }.body()
        return dto.toDomain()
    }

    suspend fun create(request: CreateLeadRequest): Lead {
        val dto: LeadDto = client.post("${ApiClient.BASE_URL}/leads") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }
}
