package com.nxzef.wc.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.shared.util.AppResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getAllLeadsUseCase: GetAllLeadsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<AnalyticsUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        onAction(AnalyticsAction.Load)
    }

    fun onAction(action: AnalyticsAction) {
        when (action) {
            AnalyticsAction.Load, AnalyticsAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val statsResult = getDashboardStatsUseCase()
            val leadsResult = getAllLeadsUseCase()

            val stats = (statsResult as? AppResult.Success)?.data
            val leads = (leadsResult as? AppResult.Success)?.data ?: emptyList()

            if (stats == null) {
                _state.update { it.copy(isLoading = false, error = "Failed to load analytics data") }
            } else {
                _state.update { it.copy(isLoading = false, stats = stats, leads = leads) }
            }
        }
    }
}
