package com.nxzef.wc.di

import com.nxzef.wc.data.repository.AuthRepositoryImpl
import com.nxzef.wc.data.repository.DashboardRepositoryImpl
import com.nxzef.wc.data.repository.LeadRepositoryImpl
import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.domain.repository.DashboardRepository
import com.nxzef.wc.domain.repository.LeadRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<LeadRepository> { LeadRepositoryImpl(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
}
