package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.model.Lead
import com.nxzef.wc.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeadPipelineState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val error: String? = null,
    val selectedLead: Lead? = null
)

sealed interface LeadPipelineAction {
    object LoadLeads : LeadPipelineAction
    data class SelectLead(val lead: Lead) : LeadPipelineAction
    data class UpdateStatus(
        val leadId: String,
        val status: String,
        val notes: String? = null
    ) : LeadPipelineAction
    object DismissDetail : LeadPipelineAction
}

class LeadPipelineViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(LeadPipelineState())
    val state: StateFlow<LeadPipelineState> = _state.asStateFlow()

    init { loadLeads() }

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
            try {
                val leads = apiService.getAllLeads()
                _state.update { it.copy(leads = leads, isLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to load leads",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateStatus(
        leadId: String,
        status: String,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                apiService.updateLeadStatus(leadId, status, notes)
                loadLeads() // refresh
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to update")
                }
            }
        }
    }

    // Helpers for pipeline columns
    fun leadsByStatus(status: String) =
        _state.value.leads.filter { it.status == status }
}