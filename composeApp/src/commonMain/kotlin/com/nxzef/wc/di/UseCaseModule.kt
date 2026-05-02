package com.nxzef.wc.di

import com.nxzef.wc.domain.usecase.auth.LoginUseCase
import com.nxzef.wc.domain.usecase.bookings.CreateBookingUseCase
import com.nxzef.wc.domain.usecase.bookings.GetAllBookingsUseCase
import com.nxzef.wc.domain.usecase.bookings.GetMyEditingQueueUseCase
import com.nxzef.wc.domain.usecase.bookings.GetMyShootsUseCase
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.nxzef.wc.domain.usecase.invoices.GetAllInvoicesUseCase
import com.nxzef.wc.domain.usecase.invoices.GetInvoiceByBookingUseCase
import com.nxzef.wc.domain.usecase.invoices.UpdatePaymentUseCase
import com.nxzef.wc.domain.usecase.leads.CreateLeadUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import com.nxzef.wc.domain.usecase.quotes.SendQuoteUseCase
import com.nxzef.wc.domain.usecase.quotes.GetQuotesByLeadIdUseCase
import com.nxzef.wc.domain.usecase.quotes.UpdateQuoteStatusUseCase
import com.nxzef.wc.domain.usecase.tasks.CreateTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.DeleteTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.GetMyPendingTasksUseCase
import com.nxzef.wc.domain.usecase.tasks.GetTasksByBookingUseCase
import com.nxzef.wc.domain.usecase.tasks.GetTasksByLeadUseCase
import com.nxzef.wc.domain.usecase.tasks.MarkTaskDoneUseCase
import com.nxzef.wc.domain.usecase.team.CreateTeamMemberUseCase
import com.nxzef.wc.domain.usecase.team.DeleteTeamMemberUseCase
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
    factory { UpdatePaymentUseCase(get()) }
    factory { GetMyShootsUseCase(get()) }
    factory { GetMyEditingQueueUseCase(get()) }
    factory { GetQuotesByLeadIdUseCase(get()) }
    factory { SendQuoteUseCase(get()) }
    factory { UpdateQuoteStatusUseCase(get()) }
    factory { GetMyPendingTasksUseCase(get()) }
    factory { GetTasksByLeadUseCase(get()) }
    factory { GetTasksByBookingUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { MarkTaskDoneUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { DeleteTeamMemberUseCase(get()) }
}