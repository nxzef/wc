package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    OWNER,
    LEAD_MANAGER,
    MARKETING,
    PHOTOGRAPHER,
    EDITOR
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val teamId: String? = null
)

@Serializable
data class Team(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: User,
    val team: Team? = null
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val teamName: String
)

@Serializable
data class JoinTeamRequest(
    val email: String,
    val inviteCode: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
