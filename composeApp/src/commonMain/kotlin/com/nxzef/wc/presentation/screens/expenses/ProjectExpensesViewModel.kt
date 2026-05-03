package com.nxzef.wc.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.ProjectExpenseRepository
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectExpensesViewModel(
    private val expenseRepository: ProjectExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectExpensesState())
    val state: StateFlow<ProjectExpensesState> = _state.asStateFlow()

    private val _uiEvent = Channel<ProjectExpensesUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: ProjectExpensesAction) {
        when (action) {
            is ProjectExpensesAction.Load -> load(action.bookingId)
            ProjectExpensesAction.ShowAddDialog -> _state.update { it.copy(showAddDialog = true) }
            ProjectExpensesAction.HideAddDialog -> _state.update { it.copy(showAddDialog = false) }
            is ProjectExpensesAction.OnCategoryChange -> _state.update { it.copy(newCategory = action.value) }
            is ProjectExpensesAction.OnDescriptionChange -> _state.update { it.copy(newDescription = action.value) }
            is ProjectExpensesAction.OnEstimatedChange -> _state.update { it.copy(newEstimated = action.value) }
            is ProjectExpensesAction.OnActualChange -> _state.update { it.copy(newActual = action.value) }
            is ProjectExpensesAction.OnDateChange -> _state.update { it.copy(newDate = action.value) }
            is ProjectExpensesAction.OnPaymentMethodChange -> _state.update { it.copy(newPaymentMethod = action.value) }
            is ProjectExpensesAction.OnNotesChange -> _state.update { it.copy(newNotes = action.value) }
            ProjectExpensesAction.AddExpense -> addExpense()
            is ProjectExpensesAction.DeleteExpense -> deleteExpense(action.id)
        }
    }

    private fun load(bookingId: String) {
        _state.update { it.copy(bookingId = bookingId, isLoading = true, error = null) }
        viewModelScope.launch {
            expenseRepository.getByBookingId(bookingId)
                .onSuccess { expenses ->
                    _state.update { it.copy(expenses = expenses, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message ?: "Failed to load", isLoading = false) }
                }
        }
    }

    private fun addExpense() {
        val s = _state.value
        val actual = s.newActual.toDoubleOrNull() ?: run {
            viewModelScope.launch { _uiEvent.send(ProjectExpensesUiEvent.ShowSnackbar("Enter a valid amount")) }
            return
        }
        val date = s.newDate.ifBlank {
            viewModelScope.launch { _uiEvent.send(ProjectExpensesUiEvent.ShowSnackbar("Enter expense date")) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val request = CreateProjectExpenseRequest(
                bookingId = s.bookingId,
                category = s.newCategory,
                description = s.newDescription.ifBlank { null },
                estimatedAmount = s.newEstimated.toDoubleOrNull() ?: 0.0,
                actualAmount = actual,
                expenseDate = date,
                paymentMethod = s.newPaymentMethod.ifBlank { null },
                notes = s.newNotes.ifBlank { null }
            )
            expenseRepository.create(request)
                .onSuccess { expense ->
                    _state.update { it.copy(
                        expenses = it.expenses + expense,
                        isSaving = false,
                        showAddDialog = false,
                        newCategory = "Photographer Charges",
                        newDescription = "",
                        newEstimated = "",
                        newActual = "",
                        newDate = "",
                        newPaymentMethod = "",
                        newNotes = ""
                    ) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _uiEvent.send(ProjectExpensesUiEvent.ShowSnackbar(error.message ?: "Failed to add expense"))
                }
        }
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            expenseRepository.delete(id)
                .onSuccess {
                    _state.update { it.copy(expenses = it.expenses.filter { e -> e.id != id }) }
                }
                .onFailure { error ->
                    _uiEvent.send(ProjectExpensesUiEvent.ShowSnackbar(error.message ?: "Failed to delete"))
                }
        }
    }
}
