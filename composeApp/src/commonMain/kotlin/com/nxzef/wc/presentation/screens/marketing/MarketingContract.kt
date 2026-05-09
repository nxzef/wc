package com.nxzef.wc.presentation.screens.marketing

import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource

data class MarketingState(
    val leads: List<Lead> = emptyList(),
    val sourceStats: Map<LeadSource, Int> = emptyMap(),
    val userName: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val sourceFilter: LeadSource? = null
)

sealed interface MarketingAction {
    data object Load : MarketingAction
    data class FilterBySource(val source: LeadSource?) : MarketingAction
}

sealed interface MarketingUiEvent {
    data class ShowSnackbar(val message: String) : MarketingUiEvent
}