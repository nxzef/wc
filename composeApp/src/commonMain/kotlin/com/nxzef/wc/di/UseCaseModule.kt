package com.nxzef.wc.di

import com.nxzef.wc.domain.usecase.auth.LoginUseCase
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.nxzef.wc.domain.usecase.leads.CreateLeadUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { GetDashboardStatsUseCase(get()) }
    factory { GetAllLeadsUseCase(get()) }
    factory { UpdateLeadStatusUseCase(get()) }
    factory { CreateLeadUseCase(get()) }
}
