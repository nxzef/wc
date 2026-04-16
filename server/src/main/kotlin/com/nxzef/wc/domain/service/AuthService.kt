package com.nxzef.wc.domain.service

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.plugins.generateToken
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import org.mindrot.jbcrypt.BCrypt

class AuthService(private val userRepository: UserRepository) {
    fun login(request: LoginRequest): Result<LoginResponse> {
        val result = userRepository.findByEmail(request.email) ?: 
            return Result.failure(Exception("Invalid credentials"))

        val (user, hash) = result

        if (!BCrypt.checkpw(request.password, hash)) {
            return Result.failure(Exception("Invalid credentials"))
        }

        if (!user.isActive) {
            return Result.failure(Exception("Account disabled"))
        }

        val token = generateToken(
            userId = user.id,
            email = user.email,
            role = user.role.name
        )

        return Result.success(LoginResponse(token = token, user = user))
    }
}