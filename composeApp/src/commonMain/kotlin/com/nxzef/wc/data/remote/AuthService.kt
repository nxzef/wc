package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.shared.dto.LoginResponseDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.JoinTeamRequest
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.RefreshRequest
import com.nxzef.wc.shared.model.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthService(private val client: HttpClient) {
    private val baseUrl = AppConfig.BASE_URL

    suspend fun login(email: String, password: String): LoginResponse {
        val dto: LoginResponseDto = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
        return dto.toDomain()
    }

    suspend fun register(name: String, email: String, password: String, teamName: String): LoginResponse {
        val dto: LoginResponseDto = client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(name, email, password, teamName))
        }.body()
        return dto.toDomain()
    }

    suspend fun joinTeam(
        email: String,
        inviteCode: String,
        newPassword: String,
        confirmPassword: String
    ): LoginResponse {
        val dto: LoginResponseDto = client.post("$baseUrl/auth/join") {
            contentType(ContentType.Application.Json)
            setBody(
                JoinTeamRequest(
                    email = email,
                    inviteCode = inviteCode.trim().uppercase(),
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
        }.body()
        return dto.toDomain()
    }

    suspend fun logout(refreshToken: String) {
        try {
            client.post("$baseUrl/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }
        } catch (_: Exception) {
            // Best-effort — local session is cleared regardless
        }
    }
}
