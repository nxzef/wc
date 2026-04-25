package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.application.call
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

            // Allow OWNER, LEAD_MANAGER, and MARKETING to see the team list (e.g. for assigning leads)
            if (role != UserRole.OWNER.name && 
                role != UserRole.LEAD_MANAGER.name &&
                role != UserRole.MARKETING.name) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "Insufficient permissions to view team"
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

        // DELETE team member (owner only)
        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload
                ?.getClaim("role")?.asString()

            if (role != UserRole.OWNER.name) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "Only owner can remove users"
                )
                return@delete
            }

            val id = call.parameters["id"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing user id"
                )

            val success = userRepository.deleteUser(id)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    "User not found"
                )
            }
        }
    }
}