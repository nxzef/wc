package com.nxzef.wc.di

import com.nxzef.wc.data.repository.*
import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.data.repository.RefreshTokenRepository
import com.nxzef.wc.domain.service.AuthService
import com.nxzef.wc.domain.service.EmailService
import com.nxzef.wc.domain.service.NotificationService
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

    single { RefreshTokenRepository() }
    single { TeamRepository() }
    single { PasswordResetRepository() }
    single { AuthService(get(), get(), get(), get(), get(), get()) }
    single { NotificationService(get(), get()) }
    single { EmailService() }
}