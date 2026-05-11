package com.nxzef.wc

import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Stub that always returns Failure("stub") so use-case validation tests can check
 *  whether control reached the repository or was caught by earlier validation. */
class FakeAuthRepository : AuthRepository {
    override val currentUser: StateFlow<User?> = MutableStateFlow(null)
    override val isLoggedIn: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun login(email: String, password: String): AppResult<LoginResponse> =
        AppResult.Failure(Exception("stub"))

    override suspend fun register(
        name: String, email: String, password: String, teamName: String
    ): AppResult<LoginResponse> = AppResult.Failure(Exception("stub"))

    override suspend fun joinTeam(
        email: String, inviteCode: String, newPassword: String, confirmPassword: String
    ): AppResult<LoginResponse> = AppResult.Failure(Exception("stub"))

    override suspend fun logout() {}
}
