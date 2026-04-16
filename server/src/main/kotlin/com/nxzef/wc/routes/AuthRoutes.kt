package com.nxzef.wc.routes

import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.shared.model.LoginRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            authService.login(request).fold(
                onSuccess = { response ->
                    call.respond(response)
                },
                onFailure = { error ->
                    val status = when(error.message) {
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