package com.nxzef.wc.presentation.screens.tasks

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.Task

data class TasksState(
    val pendingTasks: List<Task> = emptyList(),
    val leads: List<Lead> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

sealed interface TasksAction {
    data object Load : TasksAction
    data class MarkDone(val taskId: String, val done: Boolean) : TasksAction
    data class OnSearchQueryChange(val query: String) : TasksAction
}

sealed interface TasksUiEvent {
    data class ShowError(val message: String) : TasksUiEvent
    data class ShowSnackbar(val message: String) : TasksUiEvent
}