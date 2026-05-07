package com.nxzef.wc.presentation.screens.leads

import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task

data class LeadPipelineState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val statuses: List<LeadStatus> = emptyList(),
    val taskCounts: Map<String, Int> = emptyMap(),
    val error: String? = null,
    val selectedLead: Lead? = null,
    val tasks: List<Task> = emptyList(),
    val isTasksLoading: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val newTaskTitle: String = "",
    val showCreateStatusDialog: Boolean = false,
    val statusToDelete: LeadStatus? = null
)

sealed interface LeadPipelineAction {
    data object LoadLeads : LeadPipelineAction
    data class SelectLead(val lead: Lead) : LeadPipelineAction
    data class UpdateStatus(
        val leadId: String,
        val customStatusId: String,
        val notes: String? = null
    ) : LeadPipelineAction

    data class MarkTaskDone(val taskId: String, val isDone: Boolean) : LeadPipelineAction

    data object ShowAddTaskDialog : LeadPipelineAction
    data object HideAddTaskDialog : LeadPipelineAction
    data class OnNewTaskTitleChange(val title: String) : LeadPipelineAction
    data object OnAddTask : LeadPipelineAction
    data class OnDeleteTask(val taskId: String) : LeadPipelineAction

    data object DismissDetail : LeadPipelineAction
    data object ShowCreateStatusDialog : LeadPipelineAction
    data object HideCreateStatusDialog : LeadPipelineAction
    data class CreateStatus(val name: String, val color: String) : LeadPipelineAction

    data class RequestDeleteStatus(val status: LeadStatus) : LeadPipelineAction
    data object ConfirmDeleteStatus : LeadPipelineAction
    data object DismissDeleteStatusDialog : LeadPipelineAction
}

sealed interface LeadPipelineUiEvent {
    data class ShowError(val message: String) : LeadPipelineUiEvent
    data class ShowSnackbar(val message: String) : LeadPipelineUiEvent
}
