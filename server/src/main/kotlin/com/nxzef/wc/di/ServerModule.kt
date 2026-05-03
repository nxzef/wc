package com.nxzef.wc.di

import com.nxzef.wc.data.repository.*
import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.domain.service.NotificationService
import com.nxzef.wc.domain.service.SmtpConfig
import org.koin.dsl.module

val serverModule = module {
    single { UserRepository() }
    single { LeadStatusRepository() }
    single { LeadRepository() }
    single { QuoteRepository() }
    single { BookingRepository() }
    single { InvoiceRepository() }
    single { ReceiptRepository() }
    single { ProjectExpenseRepository() }
    single { MonthlyGoalRepository() }
    single { DashboardRepository() }
    single { TaskRepository() }
    single { NotificationRepository() }

    single {
        SmtpConfig(
            host = System.getenv("SMTP_HOST") ?: "",
            port = System.getenv("SMTP_PORT") ?: "587",
            username = System.getenv("SMTP_USERNAME") ?: "",
            password = System.getenv("SMTP_PASSWORD") ?: "",
            from = System.getenv("SMTP_FROM") ?: System.getenv("SMTP_USERNAME") ?: ""
        )
    }

    single { AuthService(get()) }
    single { NotificationService(get(), get()) }
    single { EmailService(get()) }
}