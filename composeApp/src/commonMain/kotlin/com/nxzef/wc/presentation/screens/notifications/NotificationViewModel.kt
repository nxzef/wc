package com.nxzef.wc.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.Channel
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

    init {
        load()
    }

    fun onAction(action: NotificationAction) {
        when (action) {
            NotificationAction.Load -> load()
            NotificationAction.Show ->
                _state.update { it.copy(isVisible = true) }

            NotificationAction.Hide ->
                _state.update { it.copy(isVisible = false) }

            is NotificationAction.MarkRead ->
                markRead(action.id)

            NotificationAction.MarkAllRead ->
                markAllRead()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val notifResult = repository.getMyNotifications()
            val countResult = repository.getUnreadCount()

            notifResult.onSuccess { notifications ->
                _state.update {
                    it.copy(notifications = notifications)
                }
            }
            countResult.onSuccess { count ->
                _state.update { it.copy(unreadCount = count) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun markRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
                .onSuccess { load() }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess { load() }
        }
    }
}