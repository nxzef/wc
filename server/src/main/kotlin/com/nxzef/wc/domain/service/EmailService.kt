package com.nxzef.wc.domain.service

import jakarta.activation.DataHandler
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource
import java.util.Properties

data class SmtpConfig(
    val host: String,
    val port: String,
    val username: String,
    val password: String,
    val from: String
)

class EmailService(private val config: SmtpConfig) {

    private val isConfigured get() = config.host.isNotBlank() && config.username.isNotBlank() && config.password.isNotBlank()

    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port)
        }
        return Session.getInstance(props, object : jakarta.mail.Authenticator() {
            override fun getPasswordAuthentication() =
                jakarta.mail.PasswordAuthentication(config.username, config.password)
        })
    }

    fun sendQuoteEmail(to: String, fileName: String, pdfBytes: ByteArray) {
        if (!isConfigured) {
            println("EmailService: SMTP not configured, skipping email to $to")
            return
        }

        val senderAddress = config.from.ifBlank { config.username }
        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(senderAddress))
            setRecipients(Message.RecipientType.TO, to)
            subject = "Your Wedding Quote"
        }

        val textPart = MimeBodyPart().apply {
            setText("Please find your wedding quote attached. Feel free to reach out with any questions.")
        }

        val pdfPart = MimeBodyPart().apply {
            dataHandler = DataHandler(ByteArrayDataSource(pdfBytes, "application/pdf"))
            setFileName(fileName)
        }

        message.setContent(MimeMultipart().apply {
            addBodyPart(textPart)
            addBodyPart(pdfPart)
        })

        Transport.send(message)
        println("EmailService: quote sent to $to")
    }

    fun sendTestEmail(to: String) {
        if (!isConfigured) {
            throw IllegalStateException("SMTP not configured. Set SMTP_HOST, SMTP_USERNAME, SMTP_PASSWORD in environment.")
        }

        val senderAddress = config.from.ifBlank { config.username }
        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(senderAddress))
            setRecipients(Message.RecipientType.TO, to)
            subject = "Wedding Clouds — SMTP Test"
        }

        message.setContent(MimeMultipart().apply {
            addBodyPart(MimeBodyPart().apply {
                setText("SMTP is configured correctly. Wedding Clouds email delivery is working.")
            })
        })

        Transport.send(message)
        println("EmailService: test email sent to $to")
    }
}
