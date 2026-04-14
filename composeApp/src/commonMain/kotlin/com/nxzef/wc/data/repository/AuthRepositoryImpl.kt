package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.AuthService
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val sessionManager: SessionManager
) : AuthRepository {
    override val currentUser: StateFlow<User?> = sessionManager.currentUser
    override val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authService.login(email, password)
            sessionManager.save(response.token, response.user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clear()
    }
}
