package com.nxzef.wc.domain.service

import com.nxzef.wc.config.ServerConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class EmailService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class ResendRequest(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String,
        val attachments: List<ResendAttachment>? = null
    )

    @Serializable
    data class ResendAttachment(
        val filename: String,
        val content: String  // base64 encoded
    )

    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlBody: String,
        attachmentBase64: String? = null,
        attachmentFileName: String? = null
    ): Boolean {
        val apiKey = ServerConfig.resendApiKey
        val keyDesc = if (apiKey.isBlank()) "BLANK" else "${apiKey.length} chars"
        println("📧 sendEmail → to=$to subject=\"$subject\" apiKey=$keyDesc from=${ServerConfig.fromEmail}")

        if (apiKey.isBlank()) {
            println("⚠️  RESEND_API_KEY not set — email skipped: \"$subject\" to $to")
            return false
        }

        return try {
            val attachments = if (attachmentBase64 != null && attachmentFileName != null) {
                listOf(ResendAttachment(attachmentFileName, attachmentBase64))
            } else null

            val response = client.post("https://api.resend.com/emails") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(ResendRequest(
                    from = "The Wedding Clouds <${ServerConfig.fromEmail}>",
                    to = listOf(to),
                    subject = subject,
                    html = htmlBody,
                    attachments = attachments
                ))
            }
            val ok = response.status.isSuccess()
            println("📧 sendEmail ← status=${response.status.value} ok=$ok to=$to subject=\"$subject\"")
            if (!ok) {
                val body = try { response.bodyAsText() } catch (_: Exception) { "<unreadable>" }
                println("📧 Resend response body: $body")
            }
            ok
        } catch (e: Exception) {
            println("❌ Email send failed to=$to subject=\"$subject\" error=${e.message}")
            false
        }
    }

    suspend fun sendQuoteEmail(
        to: String,
        clientName: String,
        pdfBase64: String,
        fileName: String,
        notes: String? = null
    ): Boolean {
        val notesHtml = if (!notes.isNullOrBlank())
            "<p style=\"color:#555;font-style:italic;border-left:3px solid #E91E63;padding-left:12px;margin:16px 0\">$notes</p>"
        else ""
        val html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <h2 style="color:#E91E63">The Wedding Clouds</h2>
              <p>Dear $clientName,</p>
              <p>Please find your customized photography quote attached to this email.</p>
              <p>We would love to capture your special day. Please review the quote and let us know if you have any questions.</p>
              $notesHtml
              <br/>
              <p>Warm regards,<br/><strong>The Wedding Clouds Team</strong></p>
            </div>
        """.trimIndent()
        return sendEmail(to, "Your Photography Quote — The Wedding Clouds", html, pdfBase64, fileName)
    }

    suspend fun sendPaymentReceiptEmail(
        to: String,
        clientName: String,
        amount: Double,
        paymentType: String,
        eventType: String,
        eventDate: String,
        receiptId: String
    ): Boolean {
        val formattedAmount = "₹${String.format("%,.0f", amount)}"
        val typeLabel = if (paymentType == "ADVANCE") "Advance Payment" else "Final Payment"
        val html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <h2 style="color:#E91E63">The Wedding Clouds</h2>
              <h3>Payment Receipt</h3>
              <p>Dear $clientName,</p>
              <p>We have received your $typeLabel of <strong>$formattedAmount</strong>.</p>
              <table style="width:100%;border-collapse:collapse;margin:20px 0">
                <tr><td style="padding:8px;border-bottom:1px solid #eee"><strong>Receipt ID</strong></td><td style="padding:8px;border-bottom:1px solid #eee">$receiptId</td></tr>
                <tr><td style="padding:8px;border-bottom:1px solid #eee"><strong>Event</strong></td><td style="padding:8px;border-bottom:1px solid #eee">$eventType</td></tr>
                <tr><td style="padding:8px;border-bottom:1px solid #eee"><strong>Event Date</strong></td><td style="padding:8px;border-bottom:1px solid #eee">$eventDate</td></tr>
                <tr><td style="padding:8px;border-bottom:1px solid #eee"><strong>Payment Type</strong></td><td style="padding:8px;border-bottom:1px solid #eee">$typeLabel</td></tr>
                <tr><td style="padding:8px"><strong>Amount Paid</strong></td><td style="padding:8px;color:#4CAF50;font-weight:bold">$formattedAmount</td></tr>
              </table>
              <p>Thank you for choosing The Wedding Clouds for your special day!</p>
              <br/>
              <p>Warm regards,<br/><strong>The Wedding Clouds Team</strong></p>
            </div>
        """.trimIndent()
        return sendEmail(to, "Payment Receipt — The Wedding Clouds", html)
    }

    suspend fun sendTeamInvitationEmail(
        to: String,
        inviteeName: String,
        teamName: String,
        inviteCode: String
    ): Boolean {
        val html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2 style="color:#E91E63;margin-bottom:8px">The Wedding Clouds</h2>
              <p>Hi $inviteeName,</p>
              <p>You have been invited to join <strong>$teamName</strong> on The Wedding Clouds CRM.</p>

              <div style="background:#f5f5f5;padding:24px;border-radius:8px;margin:24px 0;text-align:center">
                <p style="margin:0;font-size:13px;color:#666;text-transform:uppercase;letter-spacing:1px">Your Team Invite Code</p>
                <p style="margin:12px 0 0;font-size:36px;font-weight:bold;letter-spacing:8px;color:#E91E63">$inviteCode</p>
              </div>

              <h3 style="color:#333;margin-top:24px;margin-bottom:8px">How to join</h3>
              <ol style="padding-left:20px;line-height:1.8;margin-top:0">
                <li>Open The Wedding Clouds app on your device.</li>
                <li>On the Welcome screen, tap <strong>Join Existing Team</strong>.</li>
                <li>Enter the invite code above and your email address.</li>
                <li>Choose and confirm your own password — you set this yourself during join.</li>
              </ol>

              <p style="color:#888;font-size:13px;margin-top:24px">
                Note: there is no temporary password. The password you choose during the join step becomes your account password.
              </p>

              <br/>
              <p>Warm regards,<br/><strong>The Wedding Clouds Team</strong></p>
            </div>
        """.trimIndent()
        return sendEmail(to, "You're invited to join $teamName — The Wedding Clouds", html)
    }
}
