package com.nxzef.wc.routes

import com.nxzef.wc.data.repository.NotificationRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.notificationRoutes(
    notificationRepository: NotificationRepository
) {
    route("/notifications") {

        // GET my notifications
        get {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            call.respond(
                notificationRepository.getByUserId(userId)
            )
        }

        // GET unread count
        get("/unread/count") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            val count = notificationRepository.getUnreadCount(userId)
            call.respond(mapOf("count" to count))
        }

        // PUT mark one as read
        put("/{id}/read") {
            val id = call.parameters["id"]
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest, "Missing id"
                )
            val success = notificationRepository.markAsRead(id)
            if (success) {
                call.respond(mapOf("success" to true))
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    "Notification not found"
                )
            }
        }

        // PUT mark all as read
        put("/read/all") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload
                ?.getClaim("userId")?.asString()
                ?: return@put call.respond(
                    HttpStatusCode.Unauthorized, "Unauthorized"
                )
            val count = notificationRepository.markAllAsRead(userId)
            call.respond(mapOf("marked" to count))
        }
    }
}