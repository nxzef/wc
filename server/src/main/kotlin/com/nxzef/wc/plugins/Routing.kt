package com.nxzef.wc.plugins

import com.nxzef.wc.data.repository.*
import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.routes.*
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val leadRepository by inject<LeadRepository>()
    val quoteRepository by inject<QuoteRepository>()
    val bookingRepository by inject<BookingRepository>()
    val invoiceRepository by inject<InvoiceRepository>()
    val dashboardRepository by inject<DashboardRepository>()
    val taskRepository by inject<TaskRepository>()
    val notificationRepository by inject<NotificationRepository>()

    val authService by inject<AuthService>()

    userRepository.seedOwner()

    routing {
        // Public
        authRoutes(authService)

        // Protected
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                call.respond(mapOf("email" to email, "role" to role))
            }

            // Lead
            leadRoutes(leadRepository, taskRepository)
            // Quote
            quoteRoutes(quoteRepository)
            // Booking
            bookingRoutes(bookingRepository, taskRepository)
            // Invoice
            invoiceRoutes(invoiceRepository)
            // inside authenticate block:
            dashboardRoutes(dashboardRepository)
            // Task
            taskRoutes(taskRepository)
            // Notification
            notificationRoutes(notificationRepository)
        }
    }
}