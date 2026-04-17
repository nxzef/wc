package com.nxzef.wc.domain.repository

import com.nxzef.wc.shared.model.User

interface UserRepository {
    suspend fun getTeam(): Result<List<User>>
    suspend fun createMember(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<User>
}