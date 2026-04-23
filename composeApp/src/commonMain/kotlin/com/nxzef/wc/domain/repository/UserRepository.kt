package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.AppResult

interface UserRepository {
    suspend fun getTeam(): AppResult<List<User>>
    suspend fun createMember(
        name: String,
        email: String,
        password: String,
        role: String
    ): AppResult<User>
}