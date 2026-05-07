package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.NotificationRepository
import com.nxzef.wc.shared.dto.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.notificationRoutes(
    notificationRepository: NotificationRepository
) {
    route("/notifications") {

        get {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.requireUserId() ?: return@get
            call.respond(
                notificationRepository.getByUserId(userId, teamId).map { it.toDto() }
            )
        }

        get("/unread/count") {
            val teamId = call.requireTeamId() ?: return@get
            val userId = call.requireUserId() ?: return@get
            val count = notificationRepository.getUnreadCount(userId, teamId)
            call.respond(mapOf("count" to count))
        }

        put("/{id}/read") {
            val teamId = call.requireTeamId() ?: return@put
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
            val success = notificationRepository.markAsRead(id, teamId)
            if (success) {
                call.respond(mapOf("success" to true))
            } else {
                call.respond(HttpStatusCode.NotFound, "Notification not found")
            }
        }

        put("/read/all") {
            val teamId = call.requireTeamId() ?: return@put
            val userId = call.requireUserId() ?: return@put
            val count = notificationRepository.markAllAsRead(userId, teamId)
            call.respond(mapOf("marked" to count))
        }
    }
}
