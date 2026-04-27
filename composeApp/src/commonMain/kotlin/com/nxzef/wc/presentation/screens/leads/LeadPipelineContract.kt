package com.nxzef.wc.presentation.screens.leads

import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task

enum class PipelineFilter {
    ACTIVE, WON, LOST
}

data class LeadPipelineState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val error: String? = null,
    val selectedLead: Lead? = null,
    val tasks: List<Task> = emptyList(),
    val isTasksLoading: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val newTaskTitle: String = "",
    val currentFilter: PipelineFilter = PipelineFilter.ACTIVE
)

sealed interface LeadPipelineAction {
    data object LoadLeads : LeadPipelineAction
    data class SelectLead(val lead: Lead) : LeadPipelineAction
    data class UpdateStatus(
        val leadId: String,
        val status: LeadStatus,
        val notes: String? = null
    ) : LeadPipelineAction

    data class MarkTaskDone(val taskId: String, val isDone: Boolean) : LeadPipelineAction

    data object ShowAddTaskDialog : LeadPipelineAction
    data object HideAddTaskDialog : LeadPipelineAction
    data class OnNewTaskTitleChange(val title: String) : LeadPipelineAction
    data object OnAddTask : LeadPipelineAction
    data class OnDeleteTask(val taskId: String) : LeadPipelineAction

    data object DismissDetail : LeadPipelineAction
    data class SetFilter(val filter: PipelineFilter) : LeadPipelineAction
}

sealed interface LeadPipelineUiEvent {
    data class ShowError(val message: String) : LeadPipelineUiEvent
}