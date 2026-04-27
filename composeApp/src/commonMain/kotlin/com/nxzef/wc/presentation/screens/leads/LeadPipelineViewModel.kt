package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import com.nxzef.wc.domain.usecase.tasks.CreateTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.DeleteTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.GetTasksByLeadUseCase
import com.nxzef.wc.domain.usecase.tasks.MarkTaskDoneUseCase
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeadPipelineViewModel(
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val updateLeadStatusUseCase: UpdateLeadStatusUseCase,
    private val getTasksByLeadUseCase: GetTasksByLeadUseCase,
    private val markTaskDoneUseCase: MarkTaskDoneUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
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

            is LeadPipelineAction.SelectLead -> {
                _state.update { it.copy(selectedLead = action.lead) }
                loadTasks(action.lead.id)
            }

            is LeadPipelineAction.DismissDetail ->
                _state.update { it.copy(selectedLead = null, tasks = emptyList()) }

            is LeadPipelineAction.UpdateStatus ->
                updateStatus(action.leadId, action.status, action.notes)

            is LeadPipelineAction.MarkTaskDone ->
                markTaskDone(action.taskId, action.isDone)

            LeadPipelineAction.ShowAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = true) }
            LeadPipelineAction.HideAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
            is LeadPipelineAction.OnNewTaskTitleChange -> _state.update { it.copy(newTaskTitle = action.title) }
            LeadPipelineAction.OnAddTask -> addTask()
            is LeadPipelineAction.OnDeleteTask -> deleteTask(action.taskId)
            is LeadPipelineAction.SetFilter -> _state.update { it.copy(currentFilter = action.filter) }
        }
    }

    private fun addTask() {
        val s = _state.value
        val leadId = s.selectedLead?.id ?: return
        if (s.newTaskTitle.isBlank()) return

        viewModelScope.launch {
            createTaskUseCase(
                CreateTaskRequest(
                    leadId = leadId,
                    title = s.newTaskTitle,
                    assignedTo = ""
                )
            ).onSuccess {
                _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
                loadTasks(leadId)
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId).onSuccess {
                val leadId = _state.value.selectedLead?.id ?: return@onSuccess
                loadTasks(leadId)
            }
        }
    }

    private fun markTaskDone(taskId: String, isDone: Boolean) {
        viewModelScope.launch {
            markTaskDoneUseCase(taskId, isDone)
                .onSuccess { updatedTask ->
                    _state.update { s ->
                        s.copy(tasks = s.tasks.map { if (it.id == taskId) updatedTask else it })
                    }
                }
        }
    }

    private fun loadTasks(leadId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTasksLoading = true) }
            getTasksByLeadUseCase(leadId)
                .onSuccess { tasks ->
                    _state.update { it.copy(tasks = tasks, isTasksLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isTasksLoading = false) }
                }
        }
    }

    private fun loadLeads() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllLeadsUseCase()
                .onSuccess { leads ->
                    _state.update { it.copy(leads = leads, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to load leads",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun updateStatus(
        leadId: String,
        status: LeadStatus,
        notes: String?
    ) {
        viewModelScope.launch {
            updateLeadStatusUseCase(leadId, status, notes)
                .onSuccess {
                    loadLeads() // refresh
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to update")
                    }
                }
        }
    }
}