package com.nxzef.wc.di

import com.nxzef.wc.domain.usecase.auth.LoginUseCase
import com.nxzef.wc.domain.usecase.bookings.CreateBookingUseCase
import com.nxzef.wc.domain.usecase.bookings.GetAllBookingsUseCase
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.nxzef.wc.domain.usecase.invoices.CreateInvoiceUseCase
import com.nxzef.wc.domain.usecase.invoices.GetAllInvoicesUseCase
import com.nxzef.wc.domain.usecase.invoices.GetInvoiceByBookingUseCase
import com.nxzef.wc.domain.usecase.invoices.UpdatePaymentUseCase
import com.nxzef.wc.domain.usecase.leads.CreateLeadUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import com.nxzef.wc.domain.usecase.team.CreateTeamMemberUseCase
import com.nxzef.wc.domain.usecase.team.GetTeamUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { GetDashboardStatsUseCase(get()) }
    factory { GetAllLeadsUseCase(get()) }
    factory { UpdateLeadStatusUseCase(get()) }
    factory { CreateLeadUseCase(get()) }
    factory { GetTeamUseCase(get()) }
    factory { CreateTeamMemberUseCase(get()) }
    factory { GetAllBookingsUseCase(get()) }
    factory { CreateBookingUseCase(get()) }
    factory { UpdateBookingUseCase(get()) }
    factory { GetAllInvoicesUseCase(get()) }
    factory { GetInvoiceByBookingUseCase(get()) }
    factory { CreateInvoiceUseCase(get()) }
    factory { UpdatePaymentUseCase(get()) }
}
