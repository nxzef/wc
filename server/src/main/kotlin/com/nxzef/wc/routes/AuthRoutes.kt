package com.nxzef.wc.routes

import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.LoginRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.concurrent.ConcurrentHashMap

private val loginAttempts = ConcurrentHashMap<String, MutableList<Long>>()
private const val MAX_ATTEMPTS = 5
private const val WINDOW_MS = 5 * 60 * 1000L

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Email and password are required")
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
                call.respond(HttpStatusCode.TooManyRequests, "Too many login attempts. Please try again in 5 minutes.")
                return@post
            }

            authService.login(request).fold(
                onSuccess = { response ->
                    loginAttempts.remove(clientIp)
                    call.respond(response.toDto())
                },
                onFailure = { error ->
                    synchronized(attempts) { attempts.add(now) }
                    val status = when (error.message) {
                        "Invalid credentials" -> HttpStatusCode.Unauthorized
                        "Account disabled" -> HttpStatusCode.Forbidden
                        else -> HttpStatusCode.BadRequest
                    }
                    call.respond(status, error.message ?: "Login failed")
                }
            )
        }
    }
}
