package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class ApiService(private val client: HttpClient) {
    private val baseUrl = AppConfig.BASE_URL

    suspend fun checkHealth(): Boolean {
        return try {
            val response = client.get("$baseUrl/health")
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }

    suspend fun changePassword(current: String, new: String) {
        client.post("$baseUrl/auth/change-password") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(mapOf("current" to current, "new" to new))
        }
    }
}