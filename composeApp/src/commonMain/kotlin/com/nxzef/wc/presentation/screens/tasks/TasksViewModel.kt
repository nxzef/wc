package com.nxzef.wc.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.usecase.bookings.GetAllBookingsUseCase
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.tasks.GetMyPendingTasksUseCase
import com.nxzef.wc.shared.util.ErrorMessages
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val getMyPendingTasksUseCase: GetMyPendingTasksUseCase,
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val getAllBookingsUseCase: GetAllBookingsUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    private val _uiEvent = Channel<TasksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var hasLoadedOnce = false
    private var previousTaskCount = -1

    init {
        load()
        collectRefreshTrigger()
    }

    fun onAction(action: TasksAction) {
        when (action) {
            TasksAction.Load -> load(silent = false)
            is TasksAction.MarkDone -> markTaskDone(action.taskId, action.done)
            is TasksAction.OnSearchQueryChange -> _state.update { it.copy(searchQuery = action.query) }
        }
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                _state.update { it.copy(isRefreshing = true) }
                load(silent = true)
            }
        }
    }

    private fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(isLoading = true, error = null) }
            val oldCount = previousTaskCount

            val tasksDeferred    = async { getMyPendingTasksUseCase() }
            val leadsDeferred    = async { getAllLeadsUseCase() }
            val bookingsDeferred = async { getAllBookingsUseCase() }

            val tasksResult    = tasksDeferred.await()
            val leadsResult    = leadsDeferred.await()
            val bookingsResult = bookingsDeferred.await()

            tasksResult
                .onSuccess { tasks ->
                    val newCount = tasks.size
                    if (hasLoadedOnce && silent && newCount != oldCount) {
                        _uiEvent.send(TasksUiEvent.ShowSnackbar("Updated"))
                    }
                    previousTaskCount = newCount
                    hasLoadedOnce = true
                    _state.update { it.copy(pendingTasks = tasks) }
                }
                .onFailure { e ->
                    if (!silent) {
                        _state.update { it.copy(error = ErrorMessages.forGeneric(e.message)) }
                    }
                }

            leadsResult.onSuccess { leads -> _state.update { it.copy(leads = leads) } }
            bookingsResult.onSuccess { bookings -> _state.update { it.copy(bookings = bookings) } }

            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private fun markTaskDone(taskId: String, done: Boolean) {
        viewModelScope.launch {
            taskRepository.markDone(taskId, done)
                .onSuccess { load() }
                .onFailure { e ->
                    _uiEvent.send(TasksUiEvent.ShowError(ErrorMessages.forGeneric(e.message)))
                }
        }
    }
}
