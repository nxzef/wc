package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val user: UserDto
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.OWNER },
        isActive = isActive
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        role = role.name,
        isActive = isActive
    )
}

fun LoginResponseDto.toDomain(): LoginResponse {
    return LoginResponse(
        token = token,
        user = user.toDomain()
    )
}

fun LoginResponse.toDto(): LoginResponseDto {
    return LoginResponseDto(
        token = token,
        user = user.toDto()
    )
}