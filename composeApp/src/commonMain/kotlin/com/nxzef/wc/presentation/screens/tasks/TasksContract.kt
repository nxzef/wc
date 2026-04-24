package com.nxzef.wc.presentation.screens.tasks

import com.nxzef.wc.shared.model.Task

data class TasksState(
    val pendingTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface TasksAction {
    data object Load : TasksAction
    data class MarkDone(val taskId: String, val done: Boolean) : TasksAction
}

sealed interface TasksUiEvent {
    data class ShowError(val message: String) : TasksUiEvent
}