package com.nxzef.wc.di

import com.nxzef.wc.presentation.screens.auth.ForgotPasswordViewModel
import com.nxzef.wc.presentation.screens.auth.JoinTeamViewModel
import com.nxzef.wc.presentation.screens.auth.LoginViewModel
import com.nxzef.wc.presentation.screens.auth.RegisterViewModel
import com.nxzef.wc.presentation.screens.bookings.BookingViewModel
import com.nxzef.wc.presentation.screens.dashboard.DashboardViewModel
import com.nxzef.wc.presentation.screens.editor.EditorViewModel
import com.nxzef.wc.presentation.screens.invoices.InvoiceViewModel
import com.nxzef.wc.presentation.screens.leads.AddLeadViewModel
import com.nxzef.wc.presentation.screens.leads.LeadPipelineViewModel
import com.nxzef.wc.presentation.screens.marketing.MarketingViewModel
import com.nxzef.wc.presentation.screens.notifications.NotificationViewModel
import com.nxzef.wc.presentation.screens.photographer.PhotographerViewModel
import com.nxzef.wc.presentation.screens.quotes.QuoteViewModel
import com.nxzef.wc.presentation.screens.settings.SettingsViewModel
import com.nxzef.wc.presentation.screens.expenses.ProjectExpensesViewModel
import com.nxzef.wc.presentation.screens.analytics.AnalyticsViewModel
import com.nxzef.wc.presentation.screens.project.ProjectViewModel
import com.nxzef.wc.presentation.screens.tasks.TasksViewModel
import com.nxzef.wc.presentation.screens.team.TeamViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::JoinTeamViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::LeadPipelineViewModel)
    viewModelOf(::AddLeadViewModel)
    viewModelOf(::TeamViewModel)
    viewModelOf(::BookingViewModel)
    viewModelOf(::InvoiceViewModel)
    viewModelOf(::PhotographerViewModel)
    viewModelOf(::EditorViewModel)
    viewModelOf(::MarketingViewModel)
    viewModelOf(::NotificationViewModel)
    viewModelOf(::QuoteViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProjectExpensesViewModel)
    viewModelOf(::ProjectViewModel)
    viewModelOf(::AnalyticsViewModel)
}
