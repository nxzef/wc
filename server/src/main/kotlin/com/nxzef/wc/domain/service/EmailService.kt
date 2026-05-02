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

class EmailService {

    private val host = System.getenv("SMTP_HOST") ?: ""
    private val port = System.getenv("SMTP_PORT") ?: "587"
    private val username = System.getenv("SMTP_USERNAME") ?: ""
    private val password = System.getenv("SMTP_PASSWORD") ?: ""
    private val from = System.getenv("SMTP_FROM") ?: username

    fun sendQuoteEmail(to: String, fileName: String, pdfBytes: ByteArray) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            println("EmailService: SMTP not configured, skipping email to $to")
            return
        }

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
        }

        val session = Session.getInstance(props, object : jakarta.mail.Authenticator() {
            override fun getPasswordAuthentication() =
                jakarta.mail.PasswordAuthentication(username, password)
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
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
}
