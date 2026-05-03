package com.nxzef.wc.presentation.screens.marketing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketingViewModel(
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val sessionManager: com.nxzef.wc.data.session.SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MarketingState())
    val state: StateFlow<MarketingState> = _state.asStateFlow()

    private val _uiEvent = Channel<MarketingUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val filteredLeads: StateFlow<List<com.nxzef.wc.shared.model.Lead>> = _state.map { s ->
        if (s.sourceFilter == null) s.leads
        else s.leads.filter { it.source == s.sourceFilter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var hasLoadedOnce = false
    private var previousLeadCount = -1

    init {
        val currentUser = sessionManager.getUser()
        _state.update { it.copy(userName = currentUser?.name ?: "User") }
        load()
        startAutoRefresh()
        collectRefreshTrigger()
    }

    fun onAction(action: MarketingAction) {
        when (action) {
            MarketingAction.Load -> load(silent = false)
            is MarketingAction.FilterBySource -> _state.update { it.copy(sourceFilter = action.source) }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                load(silent = true)
            }
        }
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                load(silent = true)
            }
        }
    }

    private fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(isLoading = true, error = null) }
            val oldCount = previousLeadCount
            getAllLeadsUseCase()
                .onSuccess { leads ->
                    val newCount = leads.size
                    if (hasLoadedOnce && silent && newCount != oldCount) {
                        _uiEvent.send(MarketingUiEvent.ShowSnackbar("Updated"))
                    }
                    previousLeadCount = newCount
                    hasLoadedOnce = true
                    val stats = leads.groupingBy { it.source }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .toMap()
                    _state.update { it.copy(leads = leads, sourceStats = stats, isLoading = false) }
                }
                .onFailure { e ->
                    if (!silent) {
                        _state.update { it.copy(error = e.message ?: "Failed", isLoading = false) }
                    }
                }
        }
    }
}
