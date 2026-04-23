package com.nxzef.wc.presentation.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.usecase.bookings.GetMyEditingQueueUseCase
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(
    private val getMyEditingQueueUseCase: GetMyEditingQueueUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _uiEvent = Channel<EditorUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        SessionManager.currentUser
            .onEach { user ->
                _state.update { it.copy(userName = user?.name ?: "") }
            }
            .launchIn(viewModelScope)
        load()
    }

    fun onAction(action: EditorAction) {
        when (action) {
            EditorAction.Load -> load()
            is EditorAction.SelectJob -> {
                _state.update { it.copy(selectedJob = action.booking) }
                loadTasks(action.booking.id)
            }

            EditorAction.DismissDetail ->
                _state.update {
                    it.copy(selectedJob = null, tasks = emptyList())
                }

            is EditorAction.MarkEditingDone ->
                markDone(action.bookingId)

            is EditorAction.MarkTaskDone ->
                markTaskDone(action.taskId, action.done)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMyEditingQueueUseCase()
                .onSuccess { queue ->
                    _state.update {
                        it.copy(queue = queue, isLoading = false)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = e.message ?: "Failed to load",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun loadTasks(bookingId: String) {
        viewModelScope.launch {
            taskRepository.getByBookingId(bookingId)
                .onSuccess { tasks ->
                    _state.update { it.copy(tasks = tasks) }
                }
        }
    }

    private fun markDone(bookingId: String) {
        viewModelScope.launch {
            updateBookingUseCase(
                id = bookingId,
                request = UpdateBookingRequest(
                    status = BookingStatus.DELIVERED
                )
            ).onSuccess {
                _uiEvent.send(EditorUiEvent.StatusUpdated)
                _state.update { it.copy(selectedJob = null) }
                load()
            }.onFailure { e ->
                _uiEvent.send(
                    EditorUiEvent.ShowSnackbar(
                        e.message ?: "Failed to update"
                    )
                )
            }
        }
    }

    private fun markTaskDone(taskId: String, done: Boolean) {
        viewModelScope.launch {
            taskRepository.markDone(taskId, done)
                .onSuccess {
                    _state.value.selectedJob?.let {
                        loadTasks(it.id)
                    }
                }
        }
    }
}