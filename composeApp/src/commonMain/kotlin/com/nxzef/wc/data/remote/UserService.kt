package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.UserDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserService(private val client: HttpClient) {

    suspend fun getTeam(): List<User> {
        val dtos: List<UserDto> = client.get("${AppConfig.BASE_URL}/users/team") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()
        return dtos.map { it.toDomain() }
    }

    suspend fun createMember(
        name: String,
        email: String,
        role: String
    ): User {
        val dto: UserDto = client.post("${AppConfig.BASE_URL}/users") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "role" to role
                )
            )
        }.body()
        return dto.toDomain()
    }

    suspend fun removeMember(id: String) {
        client.delete("${AppConfig.BASE_URL}/users/$id") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }
    }
}