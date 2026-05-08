package com.nxzef.wc.routes

import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.ForgotPasswordRequest
import com.nxzef.wc.shared.model.JoinTeamRequest
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.RefreshRequest
import com.nxzef.wc.shared.model.RegisterRequest
import com.nxzef.wc.shared.model.ResetPasswordRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.concurrent.ConcurrentHashMap

private val loginAttempts = ConcurrentHashMap<String, MutableList<Long>>()
private const val MAX_ATTEMPTS = 5
private const val WINDOW_MS = 5 * 60 * 1000L

private suspend fun ApplicationCall.respondMessage(status: HttpStatusCode, message: String) {
    respond(status, mapOf("message" to message))
}

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = try {
                call.receive<LoginRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respondMessage(HttpStatusCode.BadRequest, "Email and password are required")
                return@post
            }

            val clientIp = call.request.local.remoteAddress
            val now = System.currentTimeMillis()
            val attempts = loginAttempts.getOrPut(clientIp) { mutableListOf() }

            val tooMany = synchronized(attempts) {
                attempts.removeAll { now - it > WINDOW_MS }
                attempts.size >= MAX_ATTEMPTS
            }

            if (tooMany) {
                call.respondMessage(
                    HttpStatusCode.TooManyRequests,
                    "Too many login attempts. Please try again in 5 minutes."
                )
                return@post
            }

            try {
                authService.login(request).fold(
                    onSuccess = { response ->
                        if (response.user.role != com.nxzef.wc.shared.model.UserRole.OWNER && response.user.teamId == null) {
                            call.respondMessage(
                                HttpStatusCode.Forbidden,
                                "Please join your team first using your invite code."
                            )
                            return@fold
                        }
                        loginAttempts.remove(clientIp)
                        call.respond(response.toDto())
                    },
                    onFailure = { error ->
                        synchronized(attempts) { attempts.add(now) }
                        when (error.message) {
                            "User not found" ->
                                call.respondMessage(HttpStatusCode.NotFound, "No account found with this email.")
                            "Wrong password" ->
                                call.respondMessage(HttpStatusCode.Unauthorized, "Incorrect password.")
                            "No password set" ->
                                call.respondMessage(
                                    HttpStatusCode.Forbidden,
                                    "Please join your team first using your invite code."
                                )
                            "Account disabled" ->
                                call.respondMessage(HttpStatusCode.Forbidden, "Account disabled")
                            else ->
                                call.respondMessage(HttpStatusCode.Unauthorized, "Invalid email or password")
                        }
                    }
                )
            } catch (e: Exception) {
                println("⚠️  Login error: ${e.message}")
                call.respondMessage(HttpStatusCode.InternalServerError, "Something went wrong. Please try again.")
            }
        }

        post("/register") {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            try {
                authService.register(request).fold(
                    onSuccess = { response -> call.respond(HttpStatusCode.Created, response.toDto()) },
                    onFailure = { error ->
                        when (error.message) {
                            "Email already registered" ->
                                call.respondMessage(HttpStatusCode.Conflict, "An account with this email already exists.")
                            "Password too short" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "Password must be at least 6 characters.")
                            "All fields are required" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "All fields are required.")
                            else ->
                                call.respondMessage(HttpStatusCode.BadRequest, "Registration failed")
                        }
                    }
                )
            } catch (e: Exception) {
                println("⚠️  Registration error: ${e.message}")
                call.respondMessage(HttpStatusCode.InternalServerError, "Registration failed. Please try again.")
            }
        }

        post("/join") {
            val request = try {
                call.receive<JoinTeamRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            try {
                authService.joinTeam(request).fold(
                    onSuccess = { response -> call.respond(response.toDto()) },
                    onFailure = { error ->
                        when (error.message) {
                            "All fields are required" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "All fields are required.")
                            "Passwords do not match" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "Passwords do not match.")
                            "Password too short" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "Password must be at least 6 characters.")
                            "Team not found" ->
                                call.respondMessage(HttpStatusCode.NotFound, "Invalid invite code. Please check and try again.")
                            "User not found in this team" ->
                                call.respondMessage(HttpStatusCode.NotFound, "No invitation found for this email in that team.")
                            "Already joined" ->
                                call.respondMessage(
                                    HttpStatusCode.Conflict,
                                    "Already joined. Please sign in with your email and password."
                                )
                            "Account disabled" ->
                                call.respondMessage(HttpStatusCode.Forbidden, "Account disabled")
                            else ->
                                call.respondMessage(HttpStatusCode.Unauthorized, "Invalid credentials")
                        }
                    }
                )
            } catch (e: Exception) {
                println("⚠️  Join error: ${e.message}")
                call.respondMessage(HttpStatusCode.InternalServerError, "Could not join team. Please try again.")
            }
        }

        post("/refresh") {
            val request = try {
                call.receive<RefreshRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            if (request.refreshToken.isBlank()) {
                call.respondMessage(HttpStatusCode.BadRequest, "Refresh token is required")
                return@post
            }

            try {
                authService.refresh(request.refreshToken).fold(
                    onSuccess = { response -> call.respond(response.toDto()) },
                    onFailure = { call.respondMessage(HttpStatusCode.Unauthorized, "Session expired") }
                )
            } catch (e: Exception) {
                println("⚠️  Refresh error: ${e.message}")
                call.respondMessage(HttpStatusCode.Unauthorized, "Session expired")
            }
        }

        post("/logout") {
            val request = try {
                call.receive<RefreshRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.OK, "Logged out")
                return@post
            }
            try {
                if (request.refreshToken.isNotBlank()) {
                    authService.logout(request.refreshToken)
                }
            } catch (e: Exception) {
                println("⚠️  Logout error: ${e.message}")
            }
            call.respondMessage(HttpStatusCode.OK, "Logged out")
        }

        post("/forgot-password") {
            val request = try {
                call.receive<ForgotPasswordRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            try {
                authService.forgotPassword(request.email).fold(
                    onSuccess = { message -> call.respondMessage(HttpStatusCode.OK, message) },
                    onFailure = { error -> call.respondMessage(HttpStatusCode.InternalServerError, error.message ?: "Failed to send reset code") }
                )
            } catch (e: Exception) {
                call.respondMessage(HttpStatusCode.InternalServerError, "Failed to send reset code")
            }
        }

        post("/reset-password") {
            val request = try {
                call.receive<ResetPasswordRequest>()
            } catch (_: Exception) {
                call.respondMessage(HttpStatusCode.BadRequest, "Invalid request")
                return@post
            }

            try {
                authService.resetPassword(request).fold(
                    onSuccess = { message -> call.respondMessage(HttpStatusCode.OK, message) },
                    onFailure = { error ->
                        val status = when (error.message) {
                            "Invalid or expired reset code." -> HttpStatusCode.BadRequest
                            "Password too short" -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.InternalServerError
                        }
                        call.respondMessage(status, error.message ?: "Reset failed")
                    }
                )
            } catch (e: Exception) {
                call.respondMessage(HttpStatusCode.InternalServerError, "Reset failed")
            }
        }
    }
}
