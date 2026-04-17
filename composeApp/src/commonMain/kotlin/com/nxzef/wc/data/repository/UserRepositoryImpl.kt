package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.UserService
import com.nxzef.wc.domain.repository.UserRepository

class UserRepositoryImpl(
    private val service: UserService
) : UserRepository {

    override suspend fun getTeam() =
        runCatching { service.getTeam() }

    override suspend fun createMember(
        name: String,
        email: String,
        password: String,
        role: String
    ) = runCatching {
        service.createMember(name, email, password, role)
    }
}