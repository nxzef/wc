package com.nxzef.wc.di

import com.nxzef.wc.presentation.screens.auth.LoginViewModel
import com.nxzef.wc.presentation.screens.bookings.BookingViewModel
import com.nxzef.wc.presentation.screens.dashboard.DashboardViewModel
import com.nxzef.wc.presentation.screens.editor.EditorViewModel
import com.nxzef.wc.presentation.screens.invoices.InvoiceViewModel
import com.nxzef.wc.presentation.screens.leads.AddLeadViewModel
import com.nxzef.wc.presentation.screens.leads.LeadPipelineViewModel
import com.nxzef.wc.presentation.screens.marketing.MarketingViewModel
import com.nxzef.wc.presentation.screens.photographer.PhotographerViewModel
import com.nxzef.wc.presentation.screens.team.TeamViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::LeadPipelineViewModel)
    viewModelOf(::AddLeadViewModel)
    viewModelOf(::TeamViewModel)
    viewModelOf(::BookingViewModel)
    viewModelOf(::InvoiceViewModel)
    viewModelOf(::PhotographerViewModel)
    viewModelOf(::EditorViewModel)
    viewModelOf(::MarketingViewModel)
}
