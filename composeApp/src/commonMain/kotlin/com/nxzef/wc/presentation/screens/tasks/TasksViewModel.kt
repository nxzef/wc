package com.nxzef.wc.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.usecase.tasks.GetMyPendingTasksUseCase
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val getMyPendingTasksUseCase: GetMyPendingTasksUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    private val _uiEvent = Channel<TasksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: TasksAction) {
        when (action) {
            TasksAction.Load -> load()
            is TasksAction.MarkDone -> markTaskDone(action.taskId, action.done)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMyPendingTasksUseCase()
                .onSuccess { tasks ->
                    _state.update {
                        it.copy(pendingTasks = tasks, isLoading = false)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = e.message ?: "Failed to load tasks",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun markTaskDone(taskId: String, done: Boolean) {
        viewModelScope.launch {
            taskRepository.markDone(taskId, done)
                .onSuccess {
                    load()
                }
                .onFailure { e ->
                    _uiEvent.send(
                        TasksUiEvent.ShowError(
                            e.message ?: "Failed to update task"
                        )
                    )
                }
        }
    }
}