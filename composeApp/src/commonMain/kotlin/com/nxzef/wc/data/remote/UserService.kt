package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserService(private val client: HttpClient) {

    suspend fun getTeam(): List<User> =
        client.get("${ApiClient.BASE_URL}/users/team") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun createMember(
        name: String,
        email: String,
        password: String,
        role: String
    ): User =
        client.post("${ApiClient.BASE_URL}/users") {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "role" to role
                )
            )
        }.body()
}