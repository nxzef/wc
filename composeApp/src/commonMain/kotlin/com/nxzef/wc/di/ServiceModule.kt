package com.nxzef.wc.di

import com.nxzef.wc.data.remote.ApiClient
import com.nxzef.wc.data.remote.AuthService
import com.nxzef.wc.data.remote.BookingService
import com.nxzef.wc.data.remote.DashboardService
import com.nxzef.wc.data.remote.InvoiceService
import com.nxzef.wc.data.remote.LeadService
import com.nxzef.wc.data.remote.NotificationService
import com.nxzef.wc.data.remote.TaskService
import com.nxzef.wc.data.remote.UserService
import com.nxzef.wc.data.session.SessionManager
import org.koin.dsl.module

val serviceModule = module {
    // HTTP Client — single instance
    single { ApiClient.client }

    // Session — single instance
    single { SessionManager }

    // Services — all inject HttpClient via get()
    single { AuthService(get()) }
    single { DashboardService(get(), get()) }
    single { LeadService(get(), get()) }
    single { BookingService(get()) }
    single { TaskService(get()) }
    single { NotificationService(get()) }
    single { UserService(get()) }
    single { InvoiceService(get()) }
}