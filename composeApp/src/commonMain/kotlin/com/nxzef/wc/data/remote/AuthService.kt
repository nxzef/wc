package com.nxzef.wc.data.remote

import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthService(private val client: HttpClient) {
    private val baseUrl = ApiClient.BASE_URL

    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }
}
