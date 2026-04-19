package com.nxzef.wc.presentation.screens.photographer

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.Task

data class PhotographerState(
    val shoots: List<Booking> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val selectedShoot: Booking? = null,
    val error: String? = null
)

sealed interface PhotographerAction {
    data object Load : PhotographerAction
    data class SelectShoot(val booking: Booking) : PhotographerAction
    data object DismissDetail : PhotographerAction
    data class MarkShootDone(val bookingId: String) : PhotographerAction
    data class MarkTaskDone(
        val taskId: String,
        val done: Boolean
    ) : PhotographerAction
}

sealed interface PhotographerUiEvent {
    data class ShowSnackbar(val message: String) : PhotographerUiEvent
    data object StatusUpdated : PhotographerUiEvent
}