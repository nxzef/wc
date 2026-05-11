package com.nxzef.wc

import com.nxzef.wc.domain.usecase.auth.JoinTeamUseCase
import com.nxzef.wc.shared.util.AppResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests client-side validation in JoinTeamUseCase.
 */
class JoinTeamUseCaseTest {

    private val useCase = JoinTeamUseCase(FakeAuthRepository())

    @Test
    fun `returns failure when any field is blank`() = runTest {
        val result = useCase(email = "", inviteCode = "ABC123", newPassword = "pass", confirmPassword = "pass")
        assertTrue(result is AppResult.Failure)
        assertEquals("All fields are required", result.exception.message)
    }

    @Test
    fun `returns failure when invite code is not 6 characters`() = runTest {
        val result = useCase(email = "m@test.com", inviteCode = "AB", newPassword = "pass123", confirmPassword = "pass123")
        assertTrue(result is AppResult.Failure)
        assertEquals("Invite code must be 6 characters", result.exception.message)
    }

    @Test
    fun `returns failure when passwords do not match`() = runTest {
        val result = useCase(email = "m@test.com", inviteCode = "ABC123", newPassword = "pass123", confirmPassword = "pass456")
        assertTrue(result is AppResult.Failure)
        assertEquals("Passwords do not match", result.exception.message)
    }

    @Test
    fun `passes to repository when all fields are valid`() = runTest {
        val result = useCase(email = "m@test.com", inviteCode = "ABC123", newPassword = "pass123", confirmPassword = "pass123")
        assertTrue(result is AppResult.Failure)
        assertEquals("stub", result.exception.message)
    }
}
