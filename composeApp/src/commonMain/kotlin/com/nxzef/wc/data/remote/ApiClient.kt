package com.nxzef.wc.data.remote

import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.shared.dto.LoginResponseDto
import com.nxzef.wc.shared.dto.toDomain
import com.nxzef.wc.shared.model.RefreshRequest
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ApiClient : KoinComponent {

    private val tokenStorage: TokenStorage by inject()
    private val refreshMutex = Mutex()

    // Bare client — no interceptors; used only for /auth/refresh calls
    val plainClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    val client: HttpClient by lazy {
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) { level = LogLevel.BODY }
            install(HttpSend)
        }
        httpClient.plugin(HttpSend).intercept { requestBuilder ->
            val tokenBeforeCall = SessionManager.getToken()
            val originalCall = execute(requestBuilder)

            if (originalCall.response.status != HttpStatusCode.Unauthorized) {
                return@intercept originalCall
            }

            // 401 — attempt silent token refresh
            val newToken = refreshMutex.withLock {
                val currentToken = SessionManager.getToken()
                if (currentToken != tokenBeforeCall) {
                    currentToken // another coroutine already refreshed
                } else {
                    if (tryRefresh()) SessionManager.getToken() else null
                }
            }

            if (newToken == null) {
                SessionManager.clear()
                return@intercept originalCall
            }

            requestBuilder.headers[HttpHeaders.Authorization] = "Bearer $newToken"
            execute(requestBuilder)
        }
        httpClient
    }

    private suspend fun tryRefresh(): Boolean {
        val refreshToken = SessionManager.getRefreshToken() ?: return false
        return try {
            val dto: LoginResponseDto = plainClient.post("${AppConfig.BASE_URL}/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body()
            val response = dto.toDomain()
            SessionManager.save(response.token, response.refreshToken, response.user, response.team)
            tokenStorage.saveSession(
                token        = response.token,
                refreshToken = response.refreshToken,
                id           = response.user.id,
                name         = response.user.name,
                email        = response.user.email,
                role         = response.user.role.name,
                teamId       = response.team?.id ?: response.user.teamId,
                teamName     = response.team?.name,
                teamInviteCode = response.team?.inviteCode
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}
