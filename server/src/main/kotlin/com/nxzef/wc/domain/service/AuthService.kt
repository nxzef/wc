package com.nxzef.wc.domain.service

import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.data.repository.RefreshTokenRepository
import com.nxzef.wc.data.repository.TeamRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.plugins.generateRefreshToken
import com.nxzef.wc.plugins.generateToken
import com.nxzef.wc.shared.model.JoinTeamRequest
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import com.nxzef.wc.shared.model.RegisterRequest
import com.nxzef.wc.shared.model.UserRole
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

private const val REFRESH_EXPIRY_SECONDS = 90L * 24 * 60 * 60 // 90 days

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val teamRepository: TeamRepository,
    private val leadStatusRepository: LeadStatusRepository
) {

    fun login(request: LoginRequest): Result<LoginResponse> {
        val result = userRepository.findByEmail(request.email)
            ?: return Result.failure(Exception("Invalid credentials"))

        val (user, hash) = result

        if (hash.isNullOrBlank()) {
            return Result.failure(Exception("No password set"))
        }

        if (!BCrypt.checkpw(request.password, hash)) {
            return Result.failure(Exception("Invalid credentials"))
        }

        if (!user.isActive) {
            return Result.failure(Exception("Account disabled"))
        }

        val team = user.teamId?.let { teamRepository.getById(it) }
        val token = generateToken(user.id, user.email, user.role.name, user.teamId)
        val refreshToken = generateRefreshToken()
        refreshTokenRepository.save(user.id, refreshToken, Instant.now().plusSeconds(REFRESH_EXPIRY_SECONDS))

        return Result.success(LoginResponse(token = token, refreshToken = refreshToken, user = user, team = team))
    }

    fun register(request: RegisterRequest): Result<LoginResponse> {
        if (request.email.isBlank() || request.password.isBlank() || request.name.isBlank() || request.teamName.isBlank()) {
            return Result.failure(Exception("All fields are required"))
        }
        if (userRepository.emailExists(request.email)) {
            return Result.failure(Exception("Email already registered"))
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val owner = userRepository.createUser(
            name = request.name,
            email = request.email,
            passwordHash = passwordHash,
            role = UserRole.OWNER.name,
            teamId = null
        )
        val team = teamRepository.createTeam(owner.id, request.teamName)
        userRepository.assignTeam(owner.id, team.id)
        leadStatusRepository.seedDefaultForTeam(team.id)

        val withTeam = owner.copy(teamId = team.id)
        val token = generateToken(withTeam.id, withTeam.email, withTeam.role.name, team.id)
        val refreshToken = generateRefreshToken()
        refreshTokenRepository.save(withTeam.id, refreshToken, Instant.now().plusSeconds(REFRESH_EXPIRY_SECONDS))

        return Result.success(LoginResponse(token = token, refreshToken = refreshToken, user = withTeam, team = team))
    }

    fun joinTeam(request: JoinTeamRequest): Result<LoginResponse> {
        if (request.email.isBlank() ||
            request.inviteCode.isBlank() ||
            request.newPassword.isBlank() ||
            request.confirmPassword.isBlank()
        ) {
            return Result.failure(Exception("All fields are required"))
        }
        if (request.newPassword != request.confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }

        val team = teamRepository.getByInviteCode(request.inviteCode)
            ?: return Result.failure(Exception("Team not found"))

        val match = userRepository.findByEmailInTeam(request.email, team.id)
            ?: return Result.failure(Exception("User not found in this team"))

        val (user, existingHash) = match
        if (!existingHash.isNullOrBlank()) {
            return Result.failure(Exception("Already joined"))
        }
        if (!user.isActive) {
            return Result.failure(Exception("Account disabled"))
        }

        val passwordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
        userRepository.updatePassword(user.id, passwordHash)

        val token = generateToken(user.id, user.email, user.role.name, team.id)
        val refreshToken = generateRefreshToken()
        refreshTokenRepository.save(user.id, refreshToken, Instant.now().plusSeconds(REFRESH_EXPIRY_SECONDS))

        return Result.success(LoginResponse(token = token, refreshToken = refreshToken, user = user, team = team))
    }

    fun refresh(refreshToken: String): Result<LoginResponse> {
        val record = refreshTokenRepository.find(refreshToken)
            ?: return Result.failure(Exception("Invalid refresh token"))

        if (record.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken)
            return Result.failure(Exception("Refresh token expired"))
        }

        val user = userRepository.findById(record.userId)
            ?: return Result.failure(Exception("User not found"))

        if (!user.isActive) {
            return Result.failure(Exception("Account disabled"))
        }

        // Rotate: revoke old, issue new
        refreshTokenRepository.delete(refreshToken)
        val newRefreshToken = generateRefreshToken()
        refreshTokenRepository.save(user.id, newRefreshToken, Instant.now().plusSeconds(REFRESH_EXPIRY_SECONDS))

        val team = user.teamId?.let { teamRepository.getById(it) }
        val newToken = generateToken(user.id, user.email, user.role.name, user.teamId)
        return Result.success(LoginResponse(token = newToken, refreshToken = newRefreshToken, user = user, team = team))
    }

    fun logout(refreshToken: String) {
        refreshTokenRepository.delete(refreshToken)
    }
}
