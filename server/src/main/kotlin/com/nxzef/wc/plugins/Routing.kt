package com.nxzef.wc.plugins

import com.nxzef.wc.data.repository.BookingRepository
import com.nxzef.wc.data.repository.DashboardRepository
import com.nxzef.wc.data.repository.InvoiceRepository
import com.nxzef.wc.data.repository.LeadRepository
import com.nxzef.wc.data.repository.QuoteRepository
import com.nxzef.wc.data.repository.UserRepository
import com.nxzef.wc.routes.authRoutes
import com.nxzef.wc.routes.bookingRoutes
import com.nxzef.wc.routes.dashboardRoutes
import com.nxzef.wc.routes.invoiceRoutes
import com.nxzef.wc.routes.leadRoutes
import com.nxzef.wc.routes.quoteRoutes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val leadRepository = LeadRepository()
    val quoteRepository = QuoteRepository()
    val bookingRepository = BookingRepository()
    val invoiceRepository = InvoiceRepository()
    val dashboardRepository = DashboardRepository()

    userRepository.seedOwner()

    routing {
        // Public
        authRoutes(userRepository)

        // Protected
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val role  = principal?.payload?.getClaim("role")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                call.respond(mapOf("email" to email, "role" to role))
            }

            // Lead
            leadRoutes(leadRepository)
            // Quote
            quoteRoutes(quoteRepository)
            // Booking
            bookingRoutes(bookingRepository)
            // Invoice
            invoiceRoutes(invoiceRepository)
            // inside authenticate block:
            dashboardRoutes(dashboardRepository)
        }
    }
}