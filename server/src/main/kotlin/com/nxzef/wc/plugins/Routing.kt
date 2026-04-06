package com.nxzef.wc.plugins

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    userRepository.seedOwner()

    routing {
        // Public
        authRoutes(userRepository)

        // Protected test route
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val role  = principal?.payload?.getClaim("role")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                val name  = principal?.payload?.getClaim("name")?.asString()
                call.respond(
                    mapOf(
                        "email" to email,
                        "role"  to role,
                        "name"  to name
                    )
                )
            }
        }
    }
}