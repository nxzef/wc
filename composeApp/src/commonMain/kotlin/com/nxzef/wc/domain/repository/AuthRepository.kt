package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.AppResult
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val isLoggedIn: StateFlow<Boolean>
    
    suspend fun login(email: String, password: String): AppResult<LoginResponse>
    suspend fun register(name: String, email: String, password: String, teamName: String): AppResult<LoginResponse>
    suspend fun joinTeam(
        email: String,
        inviteCode: String,
        newPassword: String,
        confirmPassword: String
    ): AppResult<LoginResponse>
    suspend fun logout()
}
