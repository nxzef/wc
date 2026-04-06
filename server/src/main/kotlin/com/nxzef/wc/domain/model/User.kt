package com.nxzef.wc.domain.model

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
    val isActive: Boolean
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: User
)