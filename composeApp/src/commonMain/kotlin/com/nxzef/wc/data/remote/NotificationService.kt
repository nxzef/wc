package com.nxzef.wc.data.remote

import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.model.Notification
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put

class NotificationService(private val client: HttpClient) {

    suspend fun getMyNotifications(): List<Notification> =
        client.get(
            "${ApiClient.BASE_URL}/notifications"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body()

    suspend fun getUnreadCount(): Int {
        val response = client.get(
            "${ApiClient.BASE_URL}/notifications/unread/count"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body<Map<String, Int>>()
        return response["count"] ?: 0
    }

    suspend fun markAsRead(id: String): Boolean =
        client.put(
            "${ApiClient.BASE_URL}/notifications/$id/read"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body<Map<String, Boolean>>()["success"] ?: false

    suspend fun markAllAsRead(): Int =
        client.put(
            "${ApiClient.BASE_URL}/notifications/read/all"
        ) {
            header("Authorization", "Bearer ${SessionManager.getToken()}")
        }.body<Map<String, Int>>()["marked"] ?: 0
}