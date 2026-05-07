package com.nxzef.wc.routes

import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.JoinTeamRequest
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.RefreshRequest
import com.nxzef.wc.shared.model.RegisterRequest
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
                        loginAttempts.remove(clientIp)
                        call.respond(response.toDto())
                    },
                    onFailure = { error ->
                        synchronized(attempts) { attempts.add(now) }
                        when (error.message) {
                            "Account disabled" ->
                                call.respondMessage(HttpStatusCode.Forbidden, "Account disabled")
                            "No password set" ->
                                call.respondMessage(
                                    HttpStatusCode.Forbidden,
                                    "Please join using your invite code first."
                                )
                            else ->
                                call.respondMessage(HttpStatusCode.Unauthorized, "Invalid email or password")
                        }
                    }
                )
            } catch (e: Exception) {
                println("⚠️  Login error: ${e.message}")
                call.respondMessage(HttpStatusCode.InternalServerError, "Login failed. Please try again.")
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
                                call.respondMessage(HttpStatusCode.Conflict, "Email already registered")
                            "All fields are required" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "All fields are required")
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
                                call.respondMessage(HttpStatusCode.BadRequest, "All fields are required")
                            "Passwords do not match" ->
                                call.respondMessage(HttpStatusCode.BadRequest, "Passwords do not match")
                            "Team not found" ->
                                call.respondMessage(HttpStatusCode.NotFound, "Team not found")
                            "User not found in this team" ->
                                call.respondMessage(HttpStatusCode.NotFound, "Account not found in this team")
                            "Already joined" ->
                                call.respondMessage(
                                    HttpStatusCode.Conflict,
                                    "Already joined. Please use Sign In instead."
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
    }
}
