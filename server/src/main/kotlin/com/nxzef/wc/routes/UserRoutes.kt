package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.UserRole
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

fun Route.userRoutes(userRepository: UserRepository) {
    route("/users") {

        // GET team members (all users)
        get("/team") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload
                ?.getClaim("role")?.asString()

            if (role != UserRole.OWNER.name && role != UserRole.LEAD_MANAGER.name) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "Only owner or lead manager can view team"
                )
                return@get
            }
            val team = userRepository.getAllUsers()
            call.respond(team.map { it.toDto() })
        }

        // POST create team member (owner only)
        post {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload
                ?.getClaim("role")?.asString()

            if (role != UserRole.OWNER.name) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "Only owner can create users"
                )
                return@post
            }

            val request = call.receive<CreateUserRequest>()
            val user = userRepository.createUser(
                name = request.name,
                email = request.email,
                passwordHash = BCrypt.hashpw(
                    request.password,
                    BCrypt.gensalt()
                ),
                role = request.role
            )
            call.respond(HttpStatusCode.Created, user.toDto())
        }
    }
}