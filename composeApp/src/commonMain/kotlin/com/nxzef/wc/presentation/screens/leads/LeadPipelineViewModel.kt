package com.nxzef.wc.presentation.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.LeadStatusRepository
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.leads.UpdateLeadStatusUseCase
import com.nxzef.wc.domain.usecase.tasks.CreateTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.DeleteTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.GetMyTasksByLeadUseCase
import com.nxzef.wc.domain.usecase.tasks.MarkTaskDoneUseCase
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.ErrorMessages
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeadPipelineViewModel(
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val updateLeadStatusUseCase: UpdateLeadStatusUseCase,
    private val getMyTasksByLeadUseCase: GetMyTasksByLeadUseCase,
    private val markTaskDoneUseCase: MarkTaskDoneUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val leadStatusRepository: LeadStatusRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeadPipelineState())
    val state: StateFlow<LeadPipelineState> = _state.asStateFlow()

    private val _uiEvent = Channel<LeadPipelineUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadStatuses()
        loadLeads()
        collectRefreshTrigger()
    }

    fun onAction(action: LeadPipelineAction) {
        when (action) {
            is LeadPipelineAction.LoadLeads -> loadLeads(silent = false)
            is LeadPipelineAction.SelectLead -> {
                _state.update { it.copy(selectedLead = action.lead) }
                loadTasks(action.lead.id)
            }
            is LeadPipelineAction.DismissDetail ->
                _state.update { it.copy(selectedLead = null, tasks = emptyList()) }
            is LeadPipelineAction.UpdateStatus ->
                updateStatus(action.leadId, action.customStatusId, action.notes)
            is LeadPipelineAction.MarkTaskDone ->
                markTaskDone(action.taskId, action.isDone)
            LeadPipelineAction.ShowAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = true) }
            LeadPipelineAction.HideAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
            is LeadPipelineAction.OnNewTaskTitleChange -> _state.update { it.copy(newTaskTitle = action.title) }
            LeadPipelineAction.OnAddTask -> addTask()
            is LeadPipelineAction.OnDeleteTask -> deleteTask(action.taskId)
            LeadPipelineAction.ShowCreateStatusDialog -> _state.update { it.copy(showCreateStatusDialog = true) }
            LeadPipelineAction.HideCreateStatusDialog -> _state.update { it.copy(showCreateStatusDialog = false) }
            is LeadPipelineAction.CreateStatus -> createStatus(action.name, action.color)
            is LeadPipelineAction.RequestDeleteStatus -> _state.update { it.copy(statusToDelete = action.status) }
            LeadPipelineAction.DismissDeleteStatusDialog -> _state.update { it.copy(statusToDelete = null) }
            LeadPipelineAction.ConfirmDeleteStatus -> deleteStatus()
            is LeadPipelineAction.OnSearchQueryChange -> _state.update { it.copy(searchQuery = action.query) }
            is LeadPipelineAction.OnFilterPriorityChange -> _state.update { it.copy(filterPriority = action.priority) }
            is LeadPipelineAction.OnFilterSourceChange -> _state.update { it.copy(filterSource = action.source) }
            is LeadPipelineAction.OnFilterMonthChange -> _state.update { it.copy(filterDateMonth = action.month) }
            LeadPipelineAction.ClearFilters -> _state.update {
                it.copy(
                    searchQuery = "",
                    filterPriority = null,
                    filterSource = null,
                    filterDateMonth = null
                )
            }
        }
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                _state.update { it.copy(isRefreshing = true) }
                loadLeads(silent = true)
            }
        }
    }

    private fun loadStatuses() {
        viewModelScope.launch {
            leadStatusRepository.getAll().onSuccess { statuses ->
                _state.update { it.copy(statuses = statuses) }
            }
        }
    }

    private fun createStatus(name: String, color: String) {
        viewModelScope.launch {
            leadStatusRepository.create(name, color)
                .onSuccess { newStatus ->
                    _state.update { it.copy(
                        statuses = it.statuses + newStatus,
                        showCreateStatusDialog = false
                    ) }
                    RefreshManager.triggerRefresh()
                }
                .onFailure { error ->
                    val raw = error.message.orEmpty()
                    val msg = if (
                        raw.contains("409") ||
                        raw.contains("Conflict", ignoreCase = true) ||
                        raw.contains("already exists", ignoreCase = true)
                    ) "Status name already exists" else ErrorMessages.forGeneric(error.message)
                    _uiEvent.send(LeadPipelineUiEvent.ShowError(msg))
                }
        }
    }

    private fun deleteStatus() {
        val target = _state.value.statusToDelete ?: return
        _state.update { it.copy(statusToDelete = null) }
        viewModelScope.launch {
            leadStatusRepository.delete(target.id)
                .onSuccess {
                    loadStatuses()
                    loadLeads(silent = true)
                    RefreshManager.triggerRefresh()
                    _uiEvent.send(LeadPipelineUiEvent.ShowSnackbar("Deleted '${target.name}'"))
                }
                .onFailure { error ->
                    _uiEvent.send(LeadPipelineUiEvent.ShowError(ErrorMessages.forGeneric(error.message)))
                }
        }
    }

    private fun addTask() {
        val s = _state.value
        val leadId = s.selectedLead?.id ?: return
        val stageName = s.selectedLead.statusName
        if (s.newTaskTitle.isBlank()) return

        viewModelScope.launch {
            createTaskUseCase(
                CreateTaskRequest(leadId = leadId, title = s.newTaskTitle, stageName = stageName)
            ).onSuccess {
                _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
                RefreshManager.triggerRefresh()
                loadTasks(leadId)
                refreshTaskCount(leadId)
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId).onSuccess {
                val leadId = _state.value.selectedLead?.id ?: return@onSuccess
                RefreshManager.triggerRefresh()
                loadTasks(leadId)
                refreshTaskCount(leadId)
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
                    val leadId = _state.value.selectedLead?.id ?: return@onSuccess
                    RefreshManager.triggerRefresh()
                    refreshTaskCount(leadId)
                }
        }
    }

    private fun loadTasks(leadId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTasksLoading = true) }
            getMyTasksByLeadUseCase(leadId)
                .onSuccess { tasks -> _state.update { it.copy(tasks = tasks, isTasksLoading = false) } }
                .onFailure { _state.update { it.copy(isTasksLoading = false) } }
        }
    }

    private fun loadLeads(silent: Boolean = false) {
        viewModelScope.launch {
            val oldLeads = _state.value.leads
            if (!silent) _state.update { it.copy(isLoading = true, error = null) }
            getAllLeadsUseCase()
                .onSuccess { leads ->
                    if (silent && leads.size != oldLeads.size) {
                        _uiEvent.send(LeadPipelineUiEvent.ShowSnackbar("Updated"))
                    }
                    _state.update { it.copy(leads = leads, isLoading = false, isRefreshing = false) }
                    loadTaskCounts(leads)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, isRefreshing = false) }
                    if (!silent) {
                        _state.update { it.copy(error = ErrorMessages.forGeneric(error.message)) }
                    }
                }
        }
    }

    private fun loadTaskCounts(leads: List<Lead>) {
        viewModelScope.launch {
            val counts = leads.map { lead ->
                async {
                    var count = 0
                    taskRepository.getActiveCountByLeadId(lead.id).onSuccess { count = it }
                    lead.id to count
                }
            }.awaitAll().toMap()
            _state.update { it.copy(taskCounts = counts) }
        }
    }

    private fun refreshTaskCount(leadId: String) {
        viewModelScope.launch {
            taskRepository.getActiveCountByLeadId(leadId).onSuccess { count ->
                _state.update { it.copy(taskCounts = it.taskCounts + (leadId to count)) }
            }
        }
    }

    private fun updateStatus(leadId: String, customStatusId: String, notes: String?) {
        viewModelScope.launch {
            updateLeadStatusUseCase(leadId, customStatusId, notes)
                .onSuccess {
                    RefreshManager.triggerRefresh()
                    loadLeads()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = ErrorMessages.forGeneric(error.message)) }
                }
        }
    }
}
