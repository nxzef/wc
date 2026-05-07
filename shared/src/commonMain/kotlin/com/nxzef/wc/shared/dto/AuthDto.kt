package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.Team
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val teamId: String? = null
)

@Serializable
data class TeamDto(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val refreshToken: String,
    val user: UserDto,
    val team: TeamDto? = null
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.OWNER },
        isActive = isActive,
        teamId = teamId
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        role = role.name,
        isActive = isActive,
        teamId = teamId
    )
}

fun TeamDto.toDomain(): Team = Team(
    id = id,
    name = name,
    ownerId = ownerId,
    inviteCode = inviteCode,
    isActive = isActive,
    createdAt = createdAt
)

fun Team.toDto(): TeamDto = TeamDto(
    id = id,
    name = name,
    ownerId = ownerId,
    inviteCode = inviteCode,
    isActive = isActive,
    createdAt = createdAt
)

fun LoginResponseDto.toDomain(): LoginResponse {
    return LoginResponse(
        token = token,
        refreshToken = refreshToken,
        user = user.toDomain(),
        team = team?.toDomain()
    )
}

fun LoginResponse.toDto(): LoginResponseDto {
    return LoginResponseDto(
        token = token,
        refreshToken = refreshToken,
        user = user.toDto(),
        team = team?.toDto()
    )
}
