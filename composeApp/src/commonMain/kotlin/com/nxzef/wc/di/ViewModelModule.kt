package com.nxzef.wc.di

import com.nxzef.wc.presentation.screens.auth.LoginViewModel
import com.nxzef.wc.presentation.screens.dashboard.DashboardViewModel
import com.nxzef.wc.presentation.screens.leads.LeadPipelineViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { LeadPipelineViewModel(get(), get()) }
}
