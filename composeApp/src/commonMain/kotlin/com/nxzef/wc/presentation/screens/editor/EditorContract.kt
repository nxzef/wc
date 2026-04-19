package com.nxzef.wc.presentation.screens.editor

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.Task

data class EditorState(
    val queue: List<Booking> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val selectedJob: Booking? = null,
    val error: String? = null
)

sealed interface EditorAction {
    data object Load : EditorAction
    data class SelectJob(val booking: Booking) : EditorAction
    data object DismissDetail : EditorAction
    data class MarkEditingDone(val bookingId: String) : EditorAction
    data class MarkTaskDone(
        val taskId: String,
        val done: Boolean
    ) : EditorAction
}

sealed interface EditorUiEvent {
    data class ShowSnackbar(val message: String) : EditorUiEvent
    data object StatusUpdated : EditorUiEvent
}