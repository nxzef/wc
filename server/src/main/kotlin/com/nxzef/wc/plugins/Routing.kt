package com.nxzef.wc.plugins

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.DashboardRepository
import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.data.repository.NotificationRepository
import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.data.repository.MonthlyGoalRepository
import com.nxzef.wc.data.repository.ProjectExpenseRepository
import com.nxzef.wc.data.repository.ReceiptRepository
import com.nxzef.wc.data.repository.TaskRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.routes.authRoutes
import com.nxzef.wc.routes.bookingRoutes
import com.nxzef.wc.routes.dashboardRoutes
import com.nxzef.wc.routes.invoiceRoutes
import com.nxzef.wc.routes.leadRoutes
import com.nxzef.wc.routes.leadStatusRoutes
import com.nxzef.wc.routes.notificationRoutes
import com.nxzef.wc.routes.quoteRoutes
import com.nxzef.wc.routes.monthlyGoalRoutes
import com.nxzef.wc.routes.projectExpenseRoutes
import com.nxzef.wc.routes.receiptRoutes
import com.nxzef.wc.routes.taskRoutes
import com.nxzef.wc.routes.userRoutes
import com.nxzef.wc.shared.model.UserRole
import io.ktor.http.HttpStatusCode
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
    val receiptRepository by inject<ReceiptRepository>()
    val expenseRepository by inject<ProjectExpenseRepository>()
    val goalRepository by inject<MonthlyGoalRepository>()
    val dashboardRepository by inject<DashboardRepository>()
    val taskRepository by inject<TaskRepository>()
    val notificationRepository by inject<NotificationRepository>()

    val authService by inject<AuthService>()
    val notificationService by inject<NotificationService>()
    val emailService by inject<EmailService>()
    val leadStatusRepository by inject<LeadStatusRepository>()

    userRepository.seedOwner()

    routing {
        // Public
        authRoutes(authService)

        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        // Protected
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                call.respond(mapOf("email" to email, "role" to role))
            }

            // Lead
            leadRoutes(leadRepository, notificationService)
            // Lead Statuses
            leadStatusRoutes(leadStatusRepository)
            // Quote
            quoteRoutes(quoteRepository, leadRepository, bookingRepository, invoiceRepository, notificationService, emailService)
            // Booking
            bookingRoutes(bookingRepository, leadRepository, notificationService)
            // Invoice
            invoiceRoutes(invoiceRepository, receiptRepository)
            // Receipt
            receiptRoutes(receiptRepository)
            // Project Expenses
            projectExpenseRoutes(expenseRepository)
            // Monthly Goals
            monthlyGoalRoutes(goalRepository)
            // inside authenticate block:
            dashboardRoutes(dashboardRepository)
            // Task
            taskRoutes(taskRepository)
            // Notification
            notificationRoutes(notificationRepository)
            // User
            userRoutes(userRepository)

            // Email test (OWNER only)
            get("/email/test") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()

                if (role != UserRole.OWNER.name) {
                    call.respond(HttpStatusCode.Forbidden, "Owner only")
                    return@get
                }
                if (email == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    return@get
                }

                try {
                    emailService.sendTestEmail(email)
                    call.respond(mapOf("status" to "Test email sent to $email"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed: ${e.message}")
                }
            }
        }
    }
}