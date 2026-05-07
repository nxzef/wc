package com.nxzef.wc.routes

import com.nxzef.wc.config.ServerConfig
import com.nxzef.wc.domain.service.EmailService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class EmailTestResponse(
    val sent: Boolean,
    val apiKeySet: Boolean,
    val fromEmail: String,
    val error: String? = null
)

fun Route.testRoutes(emailService: EmailService) {
    get("/debug/email-test") {
        val apiKeySet = ServerConfig.resendApiKey.isNotBlank()
        val fromEmail = ServerConfig.fromEmail

        if (!apiKeySet) {
            call.respond(
                EmailTestResponse(
                    sent = false,
                    apiKeySet = false,
                    fromEmail = fromEmail,
                    error = "RESEND_API_KEY not set"
                )
            )
            return@get
        }

        val response = try {
            val sent = emailService.sendEmail(
                to = fromEmail,
                subject = "WC server email test",
                htmlBody = "<p>If you can read this, Resend delivery from the WC server is configured correctly.</p>"
            )
            EmailTestResponse(
                sent = sent,
                apiKeySet = true,
                fromEmail = fromEmail,
                error = if (sent) null else "Email send failed — check server logs"
            )
        } catch (e: Exception) {
            EmailTestResponse(
                sent = false,
                apiKeySet = true,
                fromEmail = fromEmail,
                error = e.message ?: "Unknown error"
            )
        }
        call.respond(response)
    }
}
