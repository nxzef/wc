package com.nxzef.wc.presentation.screens.dashboard

import com.nxzef.wc.shared.model.DashboardStats

data class DashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val stats: DashboardStats? = null,
    val error: String? = null,
    val showGoalDialog: Boolean = false,
    val goalRevenue: String = "",
    val goalProfit: String = "",
    val isSavingGoal: Boolean = false
)

sealed interface DashboardAction {
    data object LoadStats : DashboardAction
    data object ShowGoalDialog : DashboardAction
    data object HideGoalDialog : DashboardAction
    data class OnGoalRevenueChange(val value: String) : DashboardAction
    data class OnGoalProfitChange(val value: String) : DashboardAction
    data object SetMonthlyGoal : DashboardAction
}

sealed interface DashboardUiEvent {
    data class ShowError(val message: String) : DashboardUiEvent
    data class ShowSnackbar(val message: String) : DashboardUiEvent
}
