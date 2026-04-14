package com.nxzef.wc.di

import com.nxzef.wc.data.remote.ApiClient
import com.nxzef.wc.data.remote.ApiService
import com.nxzef.wc.data.remote.AuthService
import com.nxzef.wc.data.remote.DashboardService
import com.nxzef.wc.data.remote.LeadService
import com.nxzef.wc.data.session.SessionManager
import org.koin.dsl.module

val serviceModule = module {
    single { ApiClient.client }
    single { SessionManager() }
    single { ApiService(get()) }
    single { AuthService(get()) }
    single { LeadService(get(), get()) }
    single { DashboardService(get(), get()) }
}
