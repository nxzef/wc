package com.nxzef.wc.presentation.screens.analytics

import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.Lead

data class AnalyticsState(
    val stats: DashboardStats? = null,
    val leads: List<Lead> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface AnalyticsAction {
    data object Load : AnalyticsAction
    data object Retry : AnalyticsAction
}

sealed interface AnalyticsUiEvent {
    data class ShowSnackbar(val message: String) : AnalyticsUiEvent
}
