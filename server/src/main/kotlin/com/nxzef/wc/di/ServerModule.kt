package com.nxzef.wc.di

import com.nxzef.wc.data.repository.*
import com.nxzef.wc.data.repository.LeadStatusRepository
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
    single { DashboardRepository() }
    single { TaskRepository() }
    single { NotificationRepository() }

    single { AuthService(get()) }
    single { NotificationService(get(), get()) }
    single { EmailService() }
}