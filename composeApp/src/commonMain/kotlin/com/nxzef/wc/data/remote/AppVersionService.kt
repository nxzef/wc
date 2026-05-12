package com.nxzef.wc.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import com.nxzef.wc.config.AppConfig

class AppVersionService(private val client: HttpClient) {
    suspend fun checkVersion(): Map<String, String>? {
        return try {
            client.get("${AppConfig.BASE_URL}/app/version").body()
        } catch (e: Exception) {
            null
        }
    }
}
