package com.nxzef.wc.di

import com.nxzef.wc.data.repository.AuthRepositoryImpl
import com.nxzef.wc.data.repository.BookingRepositoryImpl
import com.nxzef.wc.data.repository.DashboardRepositoryImpl
import com.nxzef.wc.data.repository.InvoiceRepositoryImpl
import com.nxzef.wc.data.repository.LeadRepositoryImpl
import com.nxzef.wc.data.repository.NotificationRepositoryImpl
import com.nxzef.wc.data.repository.TaskRepositoryImpl
import com.nxzef.wc.data.repository.UserRepositoryImpl
import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.domain.repository.NotificationRepository
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.repository.UserRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<LeadRepository> { LeadRepositoryImpl(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
    single<BookingRepository> { BookingRepositoryImpl(get()) }
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<InvoiceRepository> { InvoiceRepositoryImpl(get()) }
}
