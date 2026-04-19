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

    override val currentUser: StateFlow<User?> =
        SessionManager.currentUser

    override val isLoggedIn: StateFlow<Boolean> =
        SessionManager.isLoggedIn

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<LoginResponse> {
        return try {
            val response = authService.login(email, password)
            SessionManager.save(response.token, response.user)
            tokenStorage.saveSession(
                token = response.token,
                id = response.user.id,
                name = response.user.name,
                email = response.user.email,
                role = response.user.role.name
            )
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun logout() {
        SessionManager.clear()
        tokenStorage.clearSession()
    }
}