package com.nxzef.wc.data.repository

import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.remote.AuthService
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.AppResult
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override val currentUser: StateFlow<User?> = SessionManager.currentUser
    override val isLoggedIn: StateFlow<Boolean> = SessionManager.isLoggedIn

    private suspend fun persist(response: LoginResponse) {
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
    }

    override suspend fun login(email: String, password: String): AppResult<LoginResponse> {
        return try {
            val response = authService.login(email, password)
            persist(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        teamName: String
    ): AppResult<LoginResponse> {
        return try {
            val response = authService.register(name, email, password, teamName)
            persist(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun joinTeam(
        email: String,
        inviteCode: String,
        newPassword: String,
        confirmPassword: String
    ): AppResult<LoginResponse> {
        return try {
            val response = authService.joinTeam(email, inviteCode, newPassword, confirmPassword)
            persist(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun logout() {
        val refreshToken = SessionManager.getRefreshToken()
        if (refreshToken != null) {
            authService.logout(refreshToken)
        }
        SessionManager.clear()
        tokenStorage.clearSession()
    }
}
