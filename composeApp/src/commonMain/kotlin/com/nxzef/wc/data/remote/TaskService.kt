package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.TaskDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.UpdateTaskRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TaskService(private val client: HttpClient) {

    suspend fun getByLeadId(leadId: String): List<Task> {
        val dtos: List<TaskDto> = client.get(
            "${ApiClient.BASE_URL}/tasks/lead/$leadId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getByBookingId(bookingId: String): List<Task> {
        val dtos: List<TaskDto> = client.get(
            "${ApiClient.BASE_URL}/tasks/booking/$bookingId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getMyPending(): List<Task> {
        val dtos: List<TaskDto> = client.get(
            "${ApiClient.BASE_URL}/tasks/my/pending"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun getByAssignedUser(userId: String): List<Task> {
        val dtos: List<TaskDto> = client.get(
            "${ApiClient.BASE_URL}/tasks/assigned/$userId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun create(request: CreateTaskRequest): Task {
        val dto: TaskDto = client.post("${ApiClient.BASE_URL}/tasks") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return dto.toDomain()
    }

    suspend fun markDone(id: String, done: Boolean): Task {
        val dto: TaskDto = client.put(
            "${ApiClient.BASE_URL}/tasks/$id/done"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(UpdateTaskRequest(isDone = done))
        }.body()
        return dto.toDomain()
    }

    suspend fun delete(id: String) {
        client.delete("${ApiClient.BASE_URL}/tasks/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }
    }
}