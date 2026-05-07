package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.LeadStatusDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.CreateLeadStatusRequest
import com.nxzef.wc.shared.model.LeadStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LeadStatusService(private val client: HttpClient) {

    suspend fun getAll(): List<LeadStatus> {
        val dtos: List<LeadStatusDto> = client.get("${AppConfig.BASE_URL}/lead-statuses") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun create(name: String, color: String): LeadStatus {
        val dto: LeadStatusDto = client.post("${AppConfig.BASE_URL}/lead-statuses") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(CreateLeadStatusRequest(name = name, color = color))
        }.body()
        return dto.toDomain()
    }

    suspend fun delete(id: String) {
        client.delete("${AppConfig.BASE_URL}/lead-statuses/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }
    }
}
