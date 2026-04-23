package com.nxzef.wc.presentation.screens.notifications

import com.nxzef.wc.shared.model.Notification

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isVisible: Boolean = false,
    val error: String? = null
)

sealed interface NotificationAction {
    data object Load : NotificationAction
    data object Show : NotificationAction
    data object Hide : NotificationAction
    data class MarkRead(val id: String) : NotificationAction
    data object MarkAllRead : NotificationAction
}

sealed interface NotificationUiEvent {
    data class ShowSnackbar(val message: String) : NotificationUiEvent
}