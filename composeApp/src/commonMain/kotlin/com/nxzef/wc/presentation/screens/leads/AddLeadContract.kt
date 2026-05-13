package com.nxzef.wc.presentation.screens.leads

import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.User

data class AddLeadState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val source: LeadSource = LeadSource.INSTAGRAM,
    val eventType: EventType = EventType.WEDDING,
    val eventDate: String = "",
    val location: String = "",
    val priority: Int = 0,
    val budget: String = "",
    val notes: String = "",
    val assignedTo: String = "",
    val teamMembers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingTeam: Boolean = false
)

sealed interface AddLeadAction {
    data class OnFullNameChange(val value: String) : AddLeadAction
    data class OnPhoneChange(val value: String) : AddLeadAction
    data class OnEmailChange(val value: String) : AddLeadAction
    data class OnSourceChange(val value: LeadSource) : AddLeadAction
    data class OnEventTypeChange(val value: EventType) : AddLeadAction
    data class OnEventDateChange(val value: String) : AddLeadAction
    data class OnLocationChange(val value: String) : AddLeadAction
    data class OnPriorityChange(val value: Int) : AddLeadAction
    data class OnBudgetChange(val value: String) : AddLeadAction
    data class OnNotesChange(val value: String) : AddLeadAction
    data class OnAssignedToChange(val value: String) : AddLeadAction
    data object OnSubmit : AddLeadAction
}

sealed interface AddLeadUiEvent {
    data object LeadCreated : AddLeadUiEvent
    data class ShowSnackbar(val message: String) : AddLeadUiEvent
}