package com.nxzef.wc.domain.service

import com.nxzef.wc.data.db.DatabaseFactory
import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.data.repository.PasswordResetRepository
import com.nxzef.wc.data.repository.RefreshTokenRepository
import com.nxzef.wc.data.repository.TeamRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.shared.model.JoinTeamRequest
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.RegisterRequest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var teamRepository: TeamRepository

    @Before
    fun setup() {
        val dbName = "auth_test_${UUID.randomUUID().toString().replace("-", "")}"
        DatabaseFactory.init("jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")

        userRepository = UserRepository()
        teamRepository = TeamRepository()

        authService = AuthService(
            userRepository = userRepository,
            refreshTokenRepository = RefreshTokenRepository(),
            teamRepository = teamRepository,
            leadStatusRepository = LeadStatusRepository(),
            passwordResetRepository = PasswordResetRepository(),
            emailService = EmailService()
        )
    }

    private fun reg(name: String, email: String, pass: String = "pass1234", team: String = "$name Studio") =
        RegisterRequest(name = name, email = email, password = pass, teamName = team)

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    fun `register creates owner, team, and returns tokens`() {
        val result = authService.register(reg("Alice", "alice@test.com"))

        assertTrue(result.isSuccess)
        val r = result.getOrThrow()
        assertEquals("alice@test.com", r.user.email)
        assertNotNull(r.user.teamId)
        assertNotNull(r.team)
        assertEquals("Alice Studio", r.team?.name)
        assertTrue(r.token.isNotBlank())
        assertTrue(r.refreshToken.isNotBlank())
    }

    @Test
    fun `register fails when email already exists`() {
        authService.register(reg("Bob", "bob@test.com"))
        val dupe = authService.register(reg("Bob2", "bob@test.com"))
        assertTrue(dupe.isFailure)
        assertEquals("Email already registered", dupe.exceptionOrNull()?.message)
    }

    @Test
    fun `register fails when password is too short`() {
        val result = authService.register(reg("Carol", "carol@test.com", pass = "abc"))
        assertTrue(result.isFailure)
        assertEquals("Password too short", result.exceptionOrNull()?.message)
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    fun `owner can login after registering`() {
        authService.register(reg("Dave", "dave@test.com", pass = "dave1234"))
        val result = authService.login(LoginRequest("dave@test.com", "dave1234"))
        assertTrue(result.isSuccess)
        assertEquals("dave@test.com", result.getOrThrow().user.email)
    }

    @Test
    fun `login fails with wrong password`() {
        authService.register(reg("Eve", "eve@test.com", pass = "correct1"))
        val result = authService.login(LoginRequest("eve@test.com", "wrongpass"))
        assertTrue(result.isFailure)
        assertEquals("Wrong password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails for unknown email`() {
        val result = authService.login(LoginRequest("nobody@test.com", "pass"))
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails for team member who has not joined yet`() {
        val owner = authService.register(reg("Owner", "owner@test.com")).getOrThrow()

        // Pre-created by TeamScreen — no password yet
        userRepository.createUser(
            name = "Invitee",
            email = "invitee@test.com",
            passwordHash = null,
            role = "LEAD_MANAGER",
            teamId = owner.user.teamId
        )

        val result = authService.login(LoginRequest("invitee@test.com", "anything"))
        assertTrue(result.isFailure)
        assertEquals("No password set", result.exceptionOrNull()?.message)
    }

    // ── JoinTeam ──────────────────────────────────────────────────────────────

    @Test
    fun `team member can join and then login via same endpoint as owner`() {
        val owner = authService.register(reg("Owner2", "owner2@test.com")).getOrThrow()
        val inviteCode = owner.team!!.inviteCode

        userRepository.createUser(
            name = "Member",
            email = "member@test.com",
            passwordHash = null,
            role = "LEAD_MANAGER",
            teamId = owner.user.teamId
        )

        val join = authService.joinTeam(
            JoinTeamRequest("member@test.com", inviteCode, "mem1234", "mem1234")
        )
        assertTrue(join.isSuccess)
        assertEquals("member@test.com", join.getOrThrow().user.email)

        // MemberLoginScreen uses the same /auth/login endpoint — verify it works
        val login = authService.login(LoginRequest("member@test.com", "mem1234"))
        assertTrue(login.isSuccess)
        assertEquals("member@test.com", login.getOrThrow().user.email)
    }

    @Test
    fun `joinTeam fails with wrong invite code`() {
        authService.register(reg("Owner3", "owner3@test.com"))
        val result = authService.joinTeam(
            JoinTeamRequest("anyone@test.com", "BADCOD", "pass1234", "pass1234")
        )
        assertTrue(result.isFailure)
        assertEquals("Team not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `joinTeam fails when member tries to join a second time`() {
        val owner = authService.register(reg("Owner4", "owner4@test.com")).getOrThrow()
        val inviteCode = owner.team!!.inviteCode

        userRepository.createUser("AlreadyIn", "alreadyin@test.com", null, "MARKETING", owner.user.teamId)

        authService.joinTeam(JoinTeamRequest("alreadyin@test.com", inviteCode, "pass1234", "pass1234"))

        val second = authService.joinTeam(
            JoinTeamRequest("alreadyin@test.com", inviteCode, "newpass1", "newpass1")
        )
        assertTrue(second.isFailure)
        assertEquals("Already joined", second.exceptionOrNull()?.message)
    }

    @Test
    fun `joinTeam fails when passwords do not match`() {
        val owner = authService.register(reg("Owner5", "owner5@test.com")).getOrThrow()
        userRepository.createUser("Mismatch", "mismatch@test.com", null, "PHOTOGRAPHER", owner.user.teamId)

        val result = authService.joinTeam(
            JoinTeamRequest("mismatch@test.com", owner.team!!.inviteCode, "abc1234", "abc5678")
        )
        assertTrue(result.isFailure)
        assertEquals("Passwords do not match", result.exceptionOrNull()?.message)
    }

    @Test
    fun `joinTeam fails when email is not in the team`() {
        val owner = authService.register(reg("Owner6", "owner6@test.com")).getOrThrow()
        val result = authService.joinTeam(
            JoinTeamRequest("notinteam@test.com", owner.team!!.inviteCode, "pass1234", "pass1234")
        )
        assertTrue(result.isFailure)
        assertEquals("User not found in this team", result.exceptionOrNull()?.message)
    }
}
