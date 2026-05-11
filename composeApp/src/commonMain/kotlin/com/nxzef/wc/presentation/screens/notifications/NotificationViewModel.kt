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
                delay(10_000)
                load(silent = true)
            }
        }
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                _state.update { it.copy(isRefreshing = true) }
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
            }.onFailure {
                if (!silent) {
                    _state.update { it.copy(error = "Failed to load notifications.") }
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar("Failed to load notifications."))
                }
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
            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private fun markRead(id: String) {
        val prev = _state.value
        _state.update { s ->
            s.copy(
                notifications = s.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                },
                unreadCount = (s.unreadCount - 1).coerceAtLeast(0)
            )
        }
        previousUnreadCount = _state.value.unreadCount
        viewModelScope.launch {
            repository.markAsRead(id)
                .onSuccess { load(silent = true) }
                .onFailure {
                    _state.update { s ->
                        s.copy(
                            notifications = prev.notifications,
                            unreadCount = prev.unreadCount
                        )
                    }
                    previousUnreadCount = prev.unreadCount
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar("Failed to mark as read."))
                }
        }
    }

    private fun markAllRead() {
        val prev = _state.value
        _state.update { s ->
            s.copy(
                notifications = s.notifications.map { it.copy(isRead = true) },
                unreadCount = 0
            )
        }
        previousUnreadCount = 0
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess { load(silent = true) }
                .onFailure {
                    _state.update { s ->
                        s.copy(
                            notifications = prev.notifications,
                            unreadCount = prev.unreadCount
                        )
                    }
                    previousUnreadCount = prev.unreadCount
                    _uiEvent.send(NotificationUiEvent.ShowSnackbar("Failed to mark all as read."))
                }
        }
    }
}
