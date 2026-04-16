package com.nxzef.wc.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _uiEvent = Channel<DashboardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        onAction(DashboardAction.LoadStats)
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.LoadStats -> loadStats()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getDashboardStatsUseCase().fold(
                onSuccess = { stats ->
                    _state.update { it.copy(stats = stats, isLoading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to load",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }
}