package com.nxzef.wc.presentation.screens.bookings

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.User

data class BookingState(
    val bookings: List<Booking> = emptyList(),
    val leads: List<Lead> = emptyList(),
    val team: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedBooking: Booking? = null,
    // Filter
    val filterStatus: BookingStatus? = null,
    // Task management
    val tasks: List<Task> = emptyList(),
    val isTasksLoading: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val newTaskTitle: String = ""
)

sealed interface BookingAction {
    data object LoadBookings : BookingAction
    data class SelectBooking(val booking: Booking) : BookingAction
    data object DismissDetail : BookingAction
    data class OnUpdateStatus(
        val bookingId: String,
        val status: BookingStatus
    ) : BookingAction

    data class OnFilterStatus(
        val status: BookingStatus?
    ) : BookingAction

    data class AssignPhotographer(val bookingId: String, val userId: String?) : BookingAction
    data class AssignEditor(val bookingId: String, val userId: String?) : BookingAction

    // Task Actions
    data class OnTaskToggle(val taskId: String, val isDone: Boolean) : BookingAction
    data object ShowAddTaskDialog : BookingAction
    data object HideAddTaskDialog : BookingAction
    data class OnNewTaskTitleChange(val title: String) : BookingAction
    data object OnAddTask : BookingAction
    data class OnDeleteTask(val taskId: String) : BookingAction
}

sealed interface BookingUiEvent {
    data class ShowSnackbar(val message: String) : BookingUiEvent
    data object BookingCreated : BookingUiEvent
}