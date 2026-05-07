package com.nxzef.wc.domain.usecase.auth

import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.util.AppResult

class JoinTeamUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        inviteCode: String,
        newPassword: String,
        confirmPassword: String
    ): AppResult<LoginResponse> {
        if (email.isBlank() || inviteCode.isBlank() ||
            newPassword.isBlank() || confirmPassword.isBlank()
        ) {
            return AppResult.Failure(Exception("All fields are required"))
        }
        if (inviteCode.trim().length != 6) {
            return AppResult.Failure(Exception("Invite code must be 6 characters"))
        }
        if (newPassword != confirmPassword) {
            return AppResult.Failure(Exception("Passwords do not match"))
        }
        return repository.joinTeam(
            email = email.trim(),
            inviteCode = inviteCode.trim().uppercase(),
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
    }
}
