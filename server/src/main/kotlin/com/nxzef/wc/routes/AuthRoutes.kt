package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.domain.model.LoginRequest
import com.nxzef.wc.domain.model.LoginResponse
import com.nxzef.wc.plugins.generateToken
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(userRepository: UserRepository) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()

            val result = userRepository.findByEmail(request.email)

            if (result == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val (user, hash) = result

            if (!BCrypt.checkpw(request.password, hash)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            if (!user.isActive) {
                call.respond(HttpStatusCode.Forbidden, "Account disabled")
                return@post
            }

            val token = generateToken(
                userId = user.id,
                email  = user.email,
                role   = user.role.name
            )

            call.respond(LoginResponse(token = token, user = user))
        }
    }
}