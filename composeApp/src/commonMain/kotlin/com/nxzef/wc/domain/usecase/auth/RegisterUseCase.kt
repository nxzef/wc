package com.nxzef.wc.domain.usecase.auth

import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.util.AppResult

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        teamName: String
    ): AppResult<LoginResponse> {
        if (name.isBlank() || email.isBlank() || password.isBlank() || teamName.isBlank()) {
            return AppResult.Failure(Exception("All fields are required"))
        }
        if (password.length < 6) {
            return AppResult.Failure(Exception("Password must be at least 6 characters"))
        }
        return repository.register(name.trim(), email.trim(), password, teamName.trim())
    }
}
