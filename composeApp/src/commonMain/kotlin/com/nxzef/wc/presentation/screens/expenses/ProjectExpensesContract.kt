package com.nxzef.wc.presentation.screens.expenses

import com.nxzef.wc.shared.model.ProjectExpense

data class ProjectExpensesState(
    val bookingId: String = "",
    val expenses: List<ProjectExpense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    // add dialog fields
    val newCategory: String = "Photographer Charges",
    val newDescription: String = "",
    val newEstimated: String = "",
    val newActual: String = "",
    val newDate: String = "",
    val newPaymentMethod: String = "",
    val newNotes: String = "",
    val isSaving: Boolean = false
)

sealed interface ProjectExpensesAction {
    data class Load(val bookingId: String) : ProjectExpensesAction
    data object ShowAddDialog : ProjectExpensesAction
    data object HideAddDialog : ProjectExpensesAction
    data class OnCategoryChange(val value: String) : ProjectExpensesAction
    data class OnDescriptionChange(val value: String) : ProjectExpensesAction
    data class OnEstimatedChange(val value: String) : ProjectExpensesAction
    data class OnActualChange(val value: String) : ProjectExpensesAction
    data class OnDateChange(val value: String) : ProjectExpensesAction
    data class OnPaymentMethodChange(val value: String) : ProjectExpensesAction
    data class OnNotesChange(val value: String) : ProjectExpensesAction
    data object AddExpense : ProjectExpensesAction
    data class DeleteExpense(val id: String) : ProjectExpensesAction
}

sealed interface ProjectExpensesUiEvent {
    data class ShowSnackbar(val message: String) : ProjectExpensesUiEvent
}
