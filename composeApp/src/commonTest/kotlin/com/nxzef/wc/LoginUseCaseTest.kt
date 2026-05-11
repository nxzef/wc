package com.nxzef.wc

import com.nxzef.wc.domain.usecase.auth.LoginUseCase
import com.nxzef.wc.shared.util.AppResult
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests the client-side validation in LoginUseCase.
 * Network/repository behaviour is tested in the server AuthServiceTest.
 */
class LoginUseCaseTest {

    private val useCase = LoginUseCase(FakeAuthRepository())

    @Test
    fun `returns failure when email is blank`() = runTest {
        val result = useCase(email = "", password = "secret")
        assertTrue(result is AppResult.Failure, "Expected Failure but got $result")
    }

    @Test
    fun `returns failure when password is blank`() = runTest {
        val result = useCase(email = "user@example.com", password = "")
        assertTrue(result is AppResult.Failure, "Expected Failure but got $result")
    }

    @Test
    fun `returns failure when both fields are blank`() = runTest {
        val result = useCase(email = "   ", password = "   ")
        assertTrue(result is AppResult.Failure, "Expected Failure but got $result")
    }

    @Test
    fun `passes to repository when both fields are non-blank`() = runTest {
        // FakeAuthRepository always returns Failure("stub") for non-empty credentials —
        // this confirms the useCase DID reach the repository rather than short-circuiting.
        val result = useCase(email = "user@example.com", password = "pass123")
        assertTrue(result is AppResult.Failure)
        val msg = result.exception.message
        assertTrue(msg == "stub", "Expected repository stub message, got: $msg")
    }
}
