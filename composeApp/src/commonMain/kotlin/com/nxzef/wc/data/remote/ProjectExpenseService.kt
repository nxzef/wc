package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.ProjectExpenseDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.model.ProjectExpense
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProjectExpenseService(private val client: HttpClient) {

    suspend fun getByBookingId(bookingId: String): List<ProjectExpense> {
        val dtos: List<ProjectExpenseDto> = client.get(
            "${ApiClient.BASE_URL}/expenses/booking/$bookingId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun create(request: CreateProjectExpenseRequest): ProjectExpense {
        val dto: ProjectExpenseDto = client.post("${ApiClient.BASE_URL}/expenses") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }

    suspend fun delete(id: String) {
        client.delete("${ApiClient.BASE_URL}/expenses/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }
    }
}
