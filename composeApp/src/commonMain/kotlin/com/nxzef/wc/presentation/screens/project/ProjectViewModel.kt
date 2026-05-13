package com.nxzef.wc.presentation.screens.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.domain.repository.InvoiceRepository
import com.nxzef.wc.domain.repository.ProjectExpenseRepository
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.tasks.GetTasksByBookingUseCase
import com.nxzef.wc.domain.usecase.tasks.MarkTaskDoneUseCase
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val bookingRepository: BookingRepository,
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val getTasksByBookingUseCase: GetTasksByBookingUseCase,
    private val invoiceRepository: InvoiceRepository,
    private val expenseRepository: ProjectExpenseRepository,
    private val userRepository: UserRepository,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val markTaskDoneUseCase: MarkTaskDoneUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectState())
    val state: StateFlow<ProjectState> = _state.asStateFlow()

    private val _uiEvent = Channel<ProjectUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var bookingId: String = ""

    fun onAction(action: ProjectAction) {
        when (action) {
            is ProjectAction.Load -> { bookingId = action.bookingId; load() }
            ProjectAction.Retry -> load()
            is ProjectAction.RequestStatusChange ->
                _state.update { it.copy(showStatusDialog = true, pendingStatus = action.status) }
            ProjectAction.ConfirmStatusChange -> confirmStatusChange()
            ProjectAction.DismissStatusDialog ->
                _state.update { it.copy(showStatusDialog = false, pendingStatus = null) }
            ProjectAction.ShowAssignTeamDialog -> {
                val booking = _state.value.booking
                _state.update {
                    it.copy(
                        showAssignTeamDialog = true,
                        draftPhotographerId = booking?.photographerId,
                        draftEditorId = booking?.editorId
                    )
                }
            }
            ProjectAction.HideAssignTeamDialog ->
                _state.update { it.copy(showAssignTeamDialog = false) }
            is ProjectAction.SelectPhotographer ->
                _state.update { it.copy(draftPhotographerId = action.userId) }
            is ProjectAction.SelectEditor ->
                _state.update { it.copy(draftEditorId = action.userId) }
            ProjectAction.ConfirmAssignTeam -> confirmAssignTeam()
            is ProjectAction.OnTaskToggle -> toggleTask(action.taskId, action.isDone)
            ProjectAction.ShowAddExpenseDialog ->
                _state.update { it.copy(showAddExpenseDialog = true) }
            ProjectAction.HideAddExpenseDialog ->
                _state.update { it.copy(showAddExpenseDialog = false) }
            is ProjectAction.OnExpCategoryChange -> _state.update { it.copy(newExpCategory = action.value) }
            is ProjectAction.OnExpDescriptionChange -> _state.update { it.copy(newExpDescription = action.value) }
            is ProjectAction.OnExpEstimatedChange -> _state.update { it.copy(newExpEstimated = action.value) }
            is ProjectAction.OnExpActualChange -> _state.update { it.copy(newExpActual = action.value) }
            is ProjectAction.OnExpDateChange -> _state.update { it.copy(newExpDate = action.value) }
            is ProjectAction.OnExpPaymentMethodChange -> _state.update { it.copy(newExpPaymentMethod = action.value) }
            is ProjectAction.OnExpNotesChange -> _state.update { it.copy(newExpNotes = action.value) }
            ProjectAction.AddExpense -> addExpense()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            var failed = false

            bookingRepository.getById(bookingId)
                .onSuccess { booking -> _state.update { it.copy(booking = booking) } }
                .onFailure { failed = true; _state.update { it.copy(error = "Failed to load project", isLoading = false) } }

            if (failed) return@launch

            getAllLeadsUseCase().onSuccess { leads ->
                val booking = _state.value.booking ?: return@onSuccess
                _state.update { it.copy(lead = leads.find { l -> l.id == booking.leadId }) }
            }
            getTasksByBookingUseCase(bookingId).onSuccess { tasks ->
                _state.update { it.copy(tasks = tasks) }
            }
            userRepository.getTeam().onSuccess { team ->
                _state.update { it.copy(team = team) }
            }
            invoiceRepository.getByBookingId(bookingId).onSuccess { invoice ->
                _state.update { it.copy(invoice = invoice) }
            }
            expenseRepository.getByBookingId(bookingId).onSuccess { expenses ->
                _state.update { it.copy(expenses = expenses) }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun confirmStatusChange() {
        val status = _state.value.pendingStatus ?: return
        _state.update { it.copy(showStatusDialog = false, pendingStatus = null) }
        viewModelScope.launch {
            updateBookingUseCase(bookingId, UpdateBookingRequest(status = status))
                .onSuccess { booking -> _state.update { it.copy(booking = booking) } }
                .onFailure { _uiEvent.send(ProjectUiEvent.ShowSnackbar("Failed to update status")) }
        }
    }

    private fun confirmAssignTeam() {
        val s = _state.value
        _state.update { it.copy(showAssignTeamDialog = false) }
        viewModelScope.launch {
            updateBookingUseCase(
                bookingId,
                UpdateBookingRequest(
                    photographerId = s.draftPhotographerId,
                    editorId = s.draftEditorId
                )
            ).onSuccess { booking -> _state.update { it.copy(booking = booking) } }
             .onFailure { _uiEvent.send(ProjectUiEvent.ShowSnackbar("Failed to update team")) }
        }
    }

    private fun toggleTask(taskId: String, isDone: Boolean) {
        viewModelScope.launch {
            markTaskDoneUseCase(taskId, isDone).onSuccess {
                _state.update { s ->
                    s.copy(tasks = s.tasks.map { t -> if (t.id == taskId) t.copy(isDone = isDone) else t })
                }
            }
        }
    }

    private fun addExpense() {
        val s = _state.value
        val actual = s.newExpActual.toDoubleOrNull() ?: run {
            viewModelScope.launch { _uiEvent.send(ProjectUiEvent.ShowSnackbar("Enter a valid amount")) }
            return
        }
        if (s.newExpDate.isBlank()) {
            viewModelScope.launch { _uiEvent.send(ProjectUiEvent.ShowSnackbar("Enter expense date")) }
            return
        }
        _state.update { it.copy(isSavingExpense = true) }
        viewModelScope.launch {
            expenseRepository.create(
                CreateProjectExpenseRequest(
                    bookingId = bookingId,
                    category = s.newExpCategory,
                    description = s.newExpDescription.ifBlank { null },
                    estimatedAmount = s.newExpEstimated.toDoubleOrNull() ?: 0.0,
                    actualAmount = actual,
                    expenseDate = s.newExpDate,
                    paymentMethod = s.newExpPaymentMethod.ifBlank { null },
                    notes = s.newExpNotes.ifBlank { null }
                )
            ).onSuccess { expense ->
                _state.update {
                    it.copy(
                        expenses = it.expenses + expense,
                        showAddExpenseDialog = false,
                        isSavingExpense = false,
                        newExpCategory = "Photographer Charges",
                        newExpDescription = "", newExpEstimated = "", newExpActual = "",
                        newExpDate = "", newExpPaymentMethod = "", newExpNotes = ""
                    )
                }
            }.onFailure {
                _state.update { it.copy(isSavingExpense = false) }
                _uiEvent.send(ProjectUiEvent.ShowSnackbar("Failed to add expense"))
            }
        }
    }
}
