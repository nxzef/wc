package com.nxzef.wc.presentation.screens.leads

import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task

enum class PipelineViewLayout { BOARD, LIST }

data class LeadPipelineState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
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
    val statusToDelete: LeadStatus? = null,
    val searchQuery: String = "",
    val filterPriority: Int? = null,
    val filterSource: LeadSource? = null,
    val filterEventType: EventType? = null,
    val filterDateMonth: Int? = null,
    val filterDateYear: Int? = null,
    val filterStatusIds: Set<String> = emptySet(),
    val viewLayout: PipelineViewLayout = PipelineViewLayout.BOARD,
    val columnWidths: Map<String, Float> = emptyMap(),
    val syncingLeadIds: Set<String> = emptySet(),
    val isReordering: Boolean = false
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
    data class RenameStatus(val statusId: String, val newName: String) : LeadPipelineAction
    data class ChangeStatusColor(val statusId: String, val newColor: String) : LeadPipelineAction

    data class OnSearchQueryChange(val query: String) : LeadPipelineAction
    data class OnFilterPriorityChange(val priority: Int?) : LeadPipelineAction
    data class OnFilterSourceChange(val source: LeadSource?) : LeadPipelineAction
    data class OnFilterEventTypeChange(val eventType: EventType?) : LeadPipelineAction
    data class OnFilterMonthChange(val month: Int?) : LeadPipelineAction
    data class OnFilterYearChange(val year: Int?) : LeadPipelineAction
    data class OnFilterStatusesChange(val statusIds: Set<String>) : LeadPipelineAction
    data object ClearFilters : LeadPipelineAction

    data object ToggleViewLayout : LeadPipelineAction
    data class OnColumnWidthChange(val statusId: String, val widthDp: Float) : LeadPipelineAction
    data class ReorderStatuses(val newOrder: List<LeadStatus>) : LeadPipelineAction
}

sealed interface LeadPipelineUiEvent {
    data class ShowError(val message: String) : LeadPipelineUiEvent
    data class ShowSnackbar(val message: String) : LeadPipelineUiEvent
}
