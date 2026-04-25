package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.UpdateTaskRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoutes(taskRepository: TaskRepository) {
    route("/tasks") {

        // GET tasks by lead
        get("/lead/{leadId}") {
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing leadId"
                )
            call.respond(taskRepository.getByLeadId(leadId).map { it.toDto() })
        }

        // GET tasks by booking
        get("/booking/{bookingId}") {
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing bookingId"
                )
            call.respond(taskRepository.getByBookingId(bookingId).map { it.toDto() })
        }

        // GET my pending tasks
        get("/my/pending") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            call.respond(taskRepository.getPendingByUser(userId).map { it.toDto() })
        }

        // GET tasks assigned to user
        get("/assigned/{userId}") {
            val userId = call.parameters["userId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing userId"
                )
            call.respond(taskRepository.getByAssignedUser(userId).map { it.toDto() })
        }

        // POST create task
        post {
            val principal = call.principal<JWTPrincipal>()
            val createdBy = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            val request = call.receive<CreateTaskRequest>()
            val task = taskRepository.create(request, createdBy)
            call.respond(HttpStatusCode.Created, task.toDto())
        }

        // PUT mark done/undone
        put("/{id}/done") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest, "Missing id"
                )
            val request = call.receive<UpdateTaskRequest>()
            val task = taskRepository.markDone(id, request.isDone)
                ?: return@put call.respond(
                    HttpStatusCode.NotFound, "Task not found"
                )
            call.respond(task.toDto())
        }
    }
}