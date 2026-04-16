package com.nxzef.wc.presentation.screens.dashboard

import com.nxzef.wc.shared.model.DashboardStats

data class DashboardState(
    val isLoading: Boolean = false,
    val stats: DashboardStats? = null,
    val error: String? = null
)

sealed interface DashboardAction {
    data object LoadStats : DashboardAction
}

sealed interface DashboardUiEvent {
    data class ShowError(val message: String) : DashboardUiEvent
}