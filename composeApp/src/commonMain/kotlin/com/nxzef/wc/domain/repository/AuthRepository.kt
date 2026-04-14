package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val isLoggedIn: StateFlow<Boolean>
    
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun logout()
}
