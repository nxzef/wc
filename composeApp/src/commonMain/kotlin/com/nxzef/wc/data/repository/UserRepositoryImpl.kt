package com.nxzef.wc.data.repository

import com.nxzef.wc.data.remote.UserService
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.AppResult

class UserRepositoryImpl(
    private val service: UserService
) : UserRepository {

    override suspend fun getTeam(): AppResult<List<User>> {
        return try {
            AppResult.Success(service.getTeam())
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun createMember(
        name: String,
        email: String,
        password: String,
        role: String
    ): AppResult<User> {
        return try {
            AppResult.Success(service.createMember(name, email, password, role))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun removeMember(id: String): AppResult<Unit> {
        return try {
            service.removeMember(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}