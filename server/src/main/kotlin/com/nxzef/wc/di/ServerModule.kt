package com.nxzef.wc.di

import com.nxzef.wc.data.repository.*
import com.nxzef.wc.domain.service.AuthService
import org.koin.dsl.module

val serverModule = module {
    single { UserRepository() }
    single { LeadRepository() }
    single { QuoteRepository() }
    single { BookingRepository() }
    single { InvoiceRepository() }
    single { DashboardRepository() }
    single { TaskRepository() }
    single { NotificationRepository() }

    single { AuthService(get()) }
}