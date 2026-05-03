package com.nxzef.wc.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.NotificationRepository
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

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val _uiEvent = Channel<NotificationUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var hasLoadedOnce = false
    private var previousUnreadCount = -1

    init {
        load()
        startAutoRefresh()
        collectRefreshTrigger()
    }

    fun onAction(action: NotificationAction) {
        when (action) {
            NotificationAction.Load -> load(silent = false)
            NotificationAction.Show -> _state.update { it.copy(isVisible = true) }
            NotificationAction.Hide -> _state.update { it.copy(isVisible = false) }
            is NotificationAction.MarkRead -> markRead(action.id)
            NotificationAction.MarkAllRead -> markAllRead()
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
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
            val notifResult = repository.getMyNotifications()
            val countResult = repository.getUnreadCount()

            notifResult.onSuccess { notifications ->
                _state.update { it.copy(notifications = notifications) }
            }.onFailure { e ->
                if (!silent) _state.update { it.copy(error = e.message) }
            }
            countResult.onSuccess { count ->
                val oldCount = previousUnreadCount
                if (hasLoadedOnce && silent && count != oldCount) {
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar("New notification"))
                }
                previousUnreadCount = count
                hasLoadedOnce = true
                _state.update { it.copy(unreadCount = count) }
            }
            if (!silent) _state.update { it.copy(isLoading = false) }
        }
    }

    private fun markRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
                .onSuccess { load() }
                .onFailure { e ->
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar(e.message ?: "Failed to mark as read"))
                }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess { load() }
                .onFailure { e ->
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar(e.message ?: "Failed to mark all as read"))
                }
        }
    }
}
