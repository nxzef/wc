package com.nxzef.wc.di

import com.nxzef.wc.presentation.screens.auth.LoginViewModel
import com.nxzef.wc.presentation.screens.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::DashboardViewModel)
}