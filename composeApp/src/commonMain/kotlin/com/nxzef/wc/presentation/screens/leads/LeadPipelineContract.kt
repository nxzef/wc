package com.nxzef.wc.presentation.screens.leads

import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus

data class LeadPipelineState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val error: String? = null,
    val selectedLead: Lead? = null
)

sealed interface LeadPipelineAction {
    data object LoadLeads : LeadPipelineAction
    data class SelectLead(val lead: Lead) : LeadPipelineAction
    data class UpdateStatus(
        val leadId: String,
        val status: LeadStatus,
        val notes: String? = null
    ) : LeadPipelineAction

    data object DismissDetail : LeadPipelineAction
}

sealed interface LeadPipelineUiEvent {
    data class ShowError(val message: String) : LeadPipelineUiEvent
}