package com.nxzef.wc.domain.usecase.auth

import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.util.AppResult

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<LoginResponse> {
        // Here you can add business logic/validation before calling the repository
        if (email.isBlank() || password.isBlank()) {
            return AppResult.Failure(Exception("Email and password cannot be empty"))
        }
        return repository.login(email, password)
    }
}