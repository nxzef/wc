package com.nxzef.wc.data.remote

import com.nxzef.wc.data.model.LoginRequest
import com.nxzef.wc.data.model.LoginResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.BASE_URL

    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }
}