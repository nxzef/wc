package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.domain.usecase.leads.CreateLeadUseCase
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddLeadViewModel(
    private val createLeadUseCase: CreateLeadUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddLeadState())
    val state: StateFlow<AddLeadState> = _state.asStateFlow()

    private val _uiEvent = Channel<AddLeadUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTeam = true) }
            userRepository.getTeam()
                .onSuccess { team ->
                    _state.update {
                        it.copy(
                            teamMembers = team,
                            assignedTo = team.firstOrNull()?.id ?: "",
                            isLoadingTeam = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingTeam = false) }
                }
        }
    }

    fun onAction(action: AddLeadAction) {
        when (action) {
            is AddLeadAction.OnFullNameChange ->
                _state.update { it.copy(fullName = action.value) }

            is AddLeadAction.OnPhoneChange ->
                _state.update { it.copy(phone = action.value) }

            is AddLeadAction.OnEmailChange ->
                _state.update { it.copy(email = action.value) }

            is AddLeadAction.OnSourceChange ->
                _state.update { it.copy(source = action.value) }

            is AddLeadAction.OnEventTypeChange ->
                _state.update { it.copy(eventType = action.value) }

            is AddLeadAction.OnEventDateChange ->
                _state.update { it.copy(eventDate = action.value) }

            is AddLeadAction.OnLocationChange ->
                _state.update { it.copy(location = action.value) }

            is AddLeadAction.OnPriorityChange ->
                _state.update { it.copy(priority = action.value) }

            is AddLeadAction.OnNotesChange ->
                _state.update { it.copy(notes = action.value) }

            is AddLeadAction.OnAssignedToChange ->
                _state.update { it.copy(assignedTo = action.value) }

            is AddLeadAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        val s = _state.value
        if (s.fullName.isBlank() || s.phone.isBlank() || s.email.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(
                    AddLeadUiEvent.ShowSnackbar(
                        "Name, phone and email are required"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val assignedTo = s.assignedTo.ifBlank {
                SessionManager.getUser()?.id ?: ""
            }
            createLeadUseCase(
                CreateLeadRequest(
                    fullName = s.fullName.trim(),
                    phone = s.phone.trim(),
                    email = s.email.trim().ifBlank { null },
                    source = s.source,
                    eventType = s.eventType,
                    eventDate = s.eventDate.trim().ifBlank { null },
                    location = s.location.trim().ifBlank { null },
                    priority = s.priority,
                    notes = s.notes.trim().ifBlank { null },
                    assignedTo = assignedTo
                )
            ).onSuccess {
                RefreshManager.triggerRefresh()
                _uiEvent.send(AddLeadUiEvent.LeadCreated)
            }.onFailure {
                _uiEvent.send(
                    AddLeadUiEvent.ShowSnackbar("Failed to create lead. Please try again.")
                )
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}