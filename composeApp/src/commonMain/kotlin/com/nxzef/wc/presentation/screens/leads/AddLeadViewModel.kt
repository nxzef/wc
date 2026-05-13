package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.domain.usecase.leads.CreateLeadUseCase
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.Lead
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
    private val userRepository: UserRepository,
    private val leadRepository: LeadRepository
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
                    _state.update { s ->
                        s.copy(
                            teamMembers = team,
                            // Don't overwrite assignedTo if already populated from a loaded lead
                            assignedTo = if (s.assignedTo.isBlank()) team.firstOrNull()?.id ?: "" else s.assignedTo,
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
                updateFormField { copy(fullName = action.value) }

            is AddLeadAction.OnPhoneChange ->
                updateFormField { copy(phone = action.value) }

            is AddLeadAction.OnEmailChange ->
                updateFormField { copy(email = action.value) }

            is AddLeadAction.OnSourceChange ->
                updateFormField { copy(source = action.value) }

            is AddLeadAction.OnEventTypeChange ->
                updateFormField { copy(eventType = action.value) }

            is AddLeadAction.OnEventDateChange ->
                updateFormField { copy(eventDate = action.value) }

            is AddLeadAction.OnMultiDayToggle ->
                updateFormField {
                    copy(isMultiDay = action.value, eventEndDate = if (!action.value) "" else eventEndDate)
                }

            is AddLeadAction.OnEventEndDateChange ->
                updateFormField { copy(eventEndDate = action.value) }

            is AddLeadAction.OnLocationChange ->
                updateFormField { copy(location = action.value) }

            is AddLeadAction.OnPriorityChange ->
                updateFormField { copy(priority = action.value) }

            is AddLeadAction.OnBudgetChange ->
                updateFormField { copy(budget = action.value) }

            is AddLeadAction.OnNotesChange ->
                updateFormField { copy(notes = action.value) }

            is AddLeadAction.OnAssignedToChange ->
                updateFormField { copy(assignedTo = action.value) }

            is AddLeadAction.OnSubmit -> submit()

            is AddLeadAction.LoadLead -> loadLead(action.leadId)
        }
    }

    private fun updateFormField(transform: AddLeadState.() -> AddLeadState) {
        _state.update { state ->
            val updated = state.transform()
            updated.copy(hasChanges = computeHasChanges(updated))
        }
    }

    private fun computeHasChanges(state: AddLeadState): Boolean {
        val orig = state.originalLead ?: return false
        return state.fullName.trim() != orig.fullName ||
               state.phone.trim() != orig.phone ||
               state.email.trim() != (orig.email ?: "") ||
               state.source != orig.source ||
               state.eventType != orig.eventType ||
               state.eventDate.trim() != (orig.eventDate ?: "") ||
               state.eventEndDate.trim() != (orig.eventEndDate ?: "") ||
               state.location.trim() != (orig.location ?: "") ||
               state.priority != orig.priority ||
               (state.budget.trim().toDoubleOrNull() ?: 0.0) != orig.budget ||
               state.notes.trim() != (orig.notes ?: "") ||
               state.assignedTo != orig.assignedTo
    }

    private fun loadLead(leadId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            leadRepository.getById(leadId)
                .onSuccess { lead ->
                    _state.update { s ->
                        s.copy(
                            isEditMode = true,
                            originalLead = lead,
                            hasChanges = false,
                            fullName = lead.fullName,
                            phone = lead.phone,
                            email = lead.email ?: "",
                            source = lead.source,
                            eventType = lead.eventType,
                            eventDate = lead.eventDate ?: "",
                            isMultiDay = lead.eventEndDate != null,
                            eventEndDate = lead.eventEndDate ?: "",
                            location = lead.location ?: "",
                            priority = lead.priority,
                            budget = if (lead.budget == 0.0) "" else lead.budget.toString(),
                            notes = lead.notes ?: "",
                            assignedTo = lead.assignedTo,
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.send(AddLeadUiEvent.ShowSnackbar("Failed to load lead"))
                }
        }
    }

    private fun submit() {
        val s = _state.value
        if (s.fullName.isBlank() || s.phone.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(AddLeadUiEvent.ShowSnackbar("Name and phone are required"))
            }
            return
        }

        val request = CreateLeadRequest(
            fullName = s.fullName.trim(),
            phone = s.phone.trim(),
            email = s.email.trim().ifBlank { null },
            source = s.source,
            eventType = s.eventType,
            eventDate = s.eventDate.trim().ifBlank { null },
            eventEndDate = if (s.isMultiDay) s.eventEndDate.trim().ifBlank { null } else null,
            location = s.location.trim().ifBlank { null },
            priority = s.priority,
            budget = s.budget.trim().toDoubleOrNull() ?: 0.0,
            notes = s.notes.trim().ifBlank { null },
            assignedTo = s.assignedTo.ifBlank { SessionManager.getUser()?.id ?: "" }
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (s.isEditMode && s.originalLead != null) {
                leadRepository.updateLead(s.originalLead.id, request)
                    .onSuccess {
                        RefreshManager.triggerRefresh()
                        _uiEvent.send(AddLeadUiEvent.LeadSaved)
                    }
                    .onFailure {
                        _uiEvent.send(AddLeadUiEvent.ShowSnackbar("Failed to update lead. Please try again."))
                    }
            } else {
                createLeadUseCase(request)
                    .onSuccess {
                        RefreshManager.triggerRefresh()
                        _uiEvent.send(AddLeadUiEvent.LeadSaved)
                    }
                    .onFailure {
                        _uiEvent.send(AddLeadUiEvent.ShowSnackbar("Failed to create lead. Please try again."))
                    }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
