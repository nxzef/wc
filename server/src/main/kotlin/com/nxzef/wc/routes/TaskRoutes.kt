package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.shared.dto.toDto
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.UpdateTaskRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoutes(taskRepository: TaskRepository) {
    route("/tasks") {

        get("/count/lead/{leadId}") {
            val teamId = call.requireTeamId() ?: return@get
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing leadId")
            val count = taskRepository.getActiveCountByLeadId(leadId, teamId)
            call.respond(mapOf("count" to count))
        }

        get("/my/lead/{leadId}") {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.requireUserId() ?: return@get
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing leadId")
            call.respond(taskRepository.getByMyLeadId(userId, leadId, teamId).map { it.toDto() })
        }

        get("/lead/{leadId}") {
            val teamId = call.requireTeamId() ?: return@get
            val leadId = call.parameters["leadId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing leadId")
            call.respond(taskRepository.getByLeadId(leadId, teamId).map { it.toDto() })
        }

        get("/my/booking/{bookingId}") {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.requireUserId() ?: return@get
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            call.respond(taskRepository.getByMyBookingId(userId, bookingId, teamId).map { it.toDto() })
        }

        get("/booking/{bookingId}") {
            val teamId = call.requireTeamId() ?: return@get
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            call.respond(taskRepository.getByBookingId(bookingId, teamId).map { it.toDto() })
        }

        get("/my/pending") {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.requireUserId() ?: return@get
            call.respond(taskRepository.getPendingByUser(userId, teamId).map { it.toDto() })
        }

        get("/assigned/{userId}") {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.parameters["userId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
            call.respond(taskRepository.getByAssignedUser(userId, teamId).map { it.toDto() })
        }

        post {
            val teamId = call.requireTeamId() ?: return@post
            val createdBy = call.requireUserId() ?: return@post
            val request = call.receive<CreateTaskRequest>()
            val task = taskRepository.create(request, createdBy, teamId)
            call.respond(HttpStatusCode.Created, task.toDto())
        }

        delete("/{id}") {
            val teamId = call.requireTeamId() ?: return@delete
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
            taskRepository.delete(id, teamId)
            call.respond(HttpStatusCode.NoContent)
        }

        put("/{id}/done") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val request = call.receive<UpdateTaskRequest>()
            val task = taskRepository.markDone(id, request.isDone, teamId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Task not found")
            call.respond(task.toDto())
        }
    }
}
