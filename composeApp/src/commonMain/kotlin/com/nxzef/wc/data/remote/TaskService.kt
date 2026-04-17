package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.UpdateTaskRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TaskService(private val client: HttpClient) {

    suspend fun getByLeadId(leadId: String): List<Task> =
        client.get(
            "${ApiClient.BASE_URL}/tasks/lead/$leadId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getByBookingId(bookingId: String): List<Task> =
        client.get(
            "${ApiClient.BASE_URL}/tasks/booking/$bookingId"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getMyPending(): List<Task> =
        client.get(
            "${ApiClient.BASE_URL}/tasks/my/pending"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun create(request: CreateTaskRequest): Task =
        client.post("${ApiClient.BASE_URL}/tasks") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun markDone(id: String, done: Boolean): Task =
        client.put(
            "${ApiClient.BASE_URL}/tasks/$id/done"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(UpdateTaskRequest(isDone = done))
        }.body()
}