package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.plugins.generateToken
import com.nxzef.wc.shared.model.LoginRequest
import com.nxzef.wc.shared.model.LoginResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
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
                email = user.email,
                role = user.role.name
            )

            call.respond(LoginResponse(token = token, user = user))
        }
    }
}