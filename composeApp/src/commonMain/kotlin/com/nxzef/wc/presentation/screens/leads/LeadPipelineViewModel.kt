package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeadPipelineViewModel(
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val updateLeadStatusUseCase: UpdateLeadStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LeadPipelineState())
    val state: StateFlow<LeadPipelineState> = _state.asStateFlow()

    private val _uiEvent = Channel<LeadPipelineUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        onAction(LeadPipelineAction.LoadLeads)
    }

    fun onAction(action: LeadPipelineAction) {
        when (action) {
            is LeadPipelineAction.LoadLeads ->
                loadLeads()

            is LeadPipelineAction.SelectLead ->
                _state.update { it.copy(selectedLead = action.lead) }

            is LeadPipelineAction.DismissDetail ->
                _state.update { it.copy(selectedLead = null) }

            is LeadPipelineAction.UpdateStatus ->
                updateStatus(action.leadId, action.status, action.notes)
        }
    }

    private fun loadLeads() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllLeadsUseCase().fold(
                onSuccess = { leads ->
                    _state.update { it.copy(leads = leads, isLoading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to load leads",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun updateStatus(
        leadId: String,
        status: String,
        notes: String?
    ) {
        viewModelScope.launch {
            updateLeadStatusUseCase(leadId, status, notes).fold(
                onSuccess = {
                    loadLeads() // refresh
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to update")
                    }
                }
            )
        }
    }
}