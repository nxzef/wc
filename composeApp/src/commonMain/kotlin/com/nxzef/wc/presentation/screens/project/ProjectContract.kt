package com.nxzef.wc.presentation.screens.project

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.ProjectExpense
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.User

data class ProjectState(
    val booking: Booking? = null,
    val lead: Lead? = null,
    val tasks: List<Task> = emptyList(),
    val team: List<User> = emptyList(),
    val invoice: Invoice? = null,
    val expenses: List<ProjectExpense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Status change
    val showStatusDialog: Boolean = false,
    val pendingStatus: BookingStatus? = null,
    // Assign team
    val showAssignTeamDialog: Boolean = false,
    val draftPhotographerId: String? = null,
    val draftEditorId: String? = null,
    // Add expense
    val showAddExpenseDialog: Boolean = false,
    val newExpCategory: String = "Photographer Charges",
    val newExpDescription: String = "",
    val newExpEstimated: String = "",
    val newExpActual: String = "",
    val newExpDate: String = "",
    val newExpPaymentMethod: String = "",
    val newExpNotes: String = "",
    val isSavingExpense: Boolean = false
)

sealed interface ProjectAction {
    data class Load(val bookingId: String) : ProjectAction
    data object Retry : ProjectAction
    // Status
    data class RequestStatusChange(val status: BookingStatus) : ProjectAction
    data object ConfirmStatusChange : ProjectAction
    data object DismissStatusDialog : ProjectAction
    // Assign team
    data object ShowAssignTeamDialog : ProjectAction
    data object HideAssignTeamDialog : ProjectAction
    data class SelectPhotographer(val userId: String?) : ProjectAction
    data class SelectEditor(val userId: String?) : ProjectAction
    data object ConfirmAssignTeam : ProjectAction
    // Tasks
    data class OnTaskToggle(val taskId: String, val isDone: Boolean) : ProjectAction
    // Expenses
    data object ShowAddExpenseDialog : ProjectAction
    data object HideAddExpenseDialog : ProjectAction
    data class OnExpCategoryChange(val value: String) : ProjectAction
    data class OnExpDescriptionChange(val value: String) : ProjectAction
    data class OnExpEstimatedChange(val value: String) : ProjectAction
    data class OnExpActualChange(val value: String) : ProjectAction
    data class OnExpDateChange(val value: String) : ProjectAction
    data class OnExpPaymentMethodChange(val value: String) : ProjectAction
    data class OnExpNotesChange(val value: String) : ProjectAction
    data object AddExpense : ProjectAction
}

sealed interface ProjectUiEvent {
    data class ShowSnackbar(val message: String) : ProjectUiEvent
}
