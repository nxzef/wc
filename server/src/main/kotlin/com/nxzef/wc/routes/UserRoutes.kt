package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.TeamRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val role: String
)

@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

fun Route.userRoutes(
    userRepository: UserRepository,
    teamRepository: TeamRepository,
    emailService: EmailService
) {
    route("/users") {

        get("/team") {
            val teamId = call.requireTeamId() ?: return@get
            val role = call.role()

            if (role != UserRole.OWNER.name &&
                role != UserRole.LEAD_MANAGER.name &&
                role != UserRole.MARKETING.name) {
                call.respond(HttpStatusCode.Forbidden, "Insufficient permissions to view team")
                return@get
            }
            val team = userRepository.getTeamMembers(teamId)
            call.respond(team.map { it.toDto() })
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            if (call.role() != UserRole.OWNER.name) {
                call.respond(HttpStatusCode.Forbidden, "Only owner can create users")
                return@post
            }

            val request = call.receive<CreateUserRequest>()

            if (userRepository.emailExists(request.email)) {
                call.respond(HttpStatusCode.Conflict, "Email already registered")
                return@post
            }

            val user = userRepository.createUser(
                name = request.name,
                email = request.email,
                passwordHash = null,
                role = request.role,
                teamId = teamId
            )

            val team = teamRepository.getById(teamId)
            if (team != null) {
                emailService.sendTeamInvitationEmail(
                    to = user.email,
                    inviteeName = user.name,
                    teamName = team.name,
                    inviteCode = team.inviteCode
                )
            }

            call.respond(HttpStatusCode.Created, user.toDto())
        }

        delete("/{id}") {
            val teamId = call.requireTeamId() ?: return@delete
            if (call.role() != UserRole.OWNER.name) {
                call.respond(HttpStatusCode.Forbidden, "Only owner can remove users")
                return@delete
            }

            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing user id")

            val success = userRepository.deleteUser(id, teamId)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }

        put("/me/password") {
            val userId = call.requireUserId() ?: return@put

            val request = call.receive<UpdatePasswordRequest>()
            val currentHash = userRepository.getPasswordHash(userId)

            if (currentHash == null || !BCrypt.checkpw(request.currentPassword, currentHash)) {
                call.respond(HttpStatusCode.BadRequest, "Invalid current password")
                return@put
            }

            val success = userRepository.updatePassword(
                userId = userId,
                newPasswordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
            )

            if (success) {
                call.respond(HttpStatusCode.OK, "Password updated successfully")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update password")
            }
        }
    }
}
