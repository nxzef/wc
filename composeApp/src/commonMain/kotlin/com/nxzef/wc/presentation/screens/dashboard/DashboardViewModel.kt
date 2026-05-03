package com.nxzef.wc.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.MonthlyGoalRepository
import com.nxzef.wc.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.nxzef.wc.shared.model.UpsertMonthlyGoalRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardViewModel(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val monthlyGoalRepository: MonthlyGoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _uiEvent = Channel<DashboardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var hasLoadedOnce = false
    private var previousLeadCount = -1

    init {
        loadStats()
        startAutoRefresh()
        collectRefreshTrigger()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.LoadStats -> loadStats(silent = false)
            DashboardAction.ShowGoalDialog -> _state.update { it.copy(showGoalDialog = true) }
            DashboardAction.HideGoalDialog -> _state.update { it.copy(showGoalDialog = false) }
            is DashboardAction.OnGoalRevenueChange -> _state.update { it.copy(goalRevenue = action.value) }
            is DashboardAction.OnGoalProfitChange -> _state.update { it.copy(goalProfit = action.value) }
            DashboardAction.SetMonthlyGoal -> setGoal()
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                loadStats(silent = true)
            }
        }
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                loadStats(silent = true)
            }
        }
    }

    private fun loadStats(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(isLoading = true, error = null) }
            val oldCount = previousLeadCount
            getDashboardStatsUseCase()
                .onSuccess { stats ->
                    val newCount = stats.recentLeads.size + stats.totalBookingsThisMonth
                    if (hasLoadedOnce && silent && newCount != oldCount) {
                        _uiEvent.send(DashboardUiEvent.ShowSnackbar("Updated"))
                    }
                    previousLeadCount = newCount
                    hasLoadedOnce = true
                    _state.update { it.copy(
                        stats = stats,
                        isLoading = false,
                        goalRevenue = stats.currentMonthGoal?.targetRevenue?.toInt()?.toString() ?: it.goalRevenue,
                        goalProfit = stats.currentMonthGoal?.targetProfit?.toInt()?.toString() ?: it.goalProfit
                    ) }
                }
                .onFailure { error ->
                    if (!silent) {
                        _state.update { it.copy(error = error.message ?: "Failed to load", isLoading = false) }
                    }
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun setGoal() {
        val s = _state.value
        val revenue = s.goalRevenue.toDoubleOrNull() ?: run {
            viewModelScope.launch { _uiEvent.send(DashboardUiEvent.ShowError("Enter a valid revenue target")) }
            return
        }
        val profit = s.goalProfit.toDoubleOrNull() ?: run {
            viewModelScope.launch { _uiEvent.send(DashboardUiEvent.ShowError("Enter a valid profit target")) }
            return
        }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        _state.update { it.copy(isSavingGoal = true) }
        viewModelScope.launch {
            monthlyGoalRepository.upsert(
                UpsertMonthlyGoalRequest(
                    year = now.year,
                    month = now.month.ordinal + 1,
                    targetRevenue = revenue,
                    targetProfit = profit
                )
            )
                .onSuccess {
                    _state.update { it.copy(isSavingGoal = false, showGoalDialog = false) }
                    loadStats(silent = false)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSavingGoal = false) }
                    _uiEvent.send(DashboardUiEvent.ShowError(error.message ?: "Failed to save goal"))
                }
        }
    }
}
