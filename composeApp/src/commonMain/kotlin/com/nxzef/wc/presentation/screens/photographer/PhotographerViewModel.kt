package com.nxzef.wc.presentation.screens.photographer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.TaskRepository
import com.nxzef.wc.domain.usecase.bookings.GetMyShootsUseCase
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

class PhotographerViewModel(
    private val getMyShootsUseCase: GetMyShootsUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhotographerState())
    val state: StateFlow<PhotographerState> = _state.asStateFlow()

    private val _uiEvent = Channel<PhotographerUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        SessionManager.currentUser
            .onEach { user ->
                _state.update { it.copy(userName = user?.name ?: "") }
            }
            .launchIn(viewModelScope)
        load()
    }

    fun onAction(action: PhotographerAction) {
        when (action) {
            PhotographerAction.Load ->
                load()

            is PhotographerAction.SelectShoot -> {
                _state.update {
                    it.copy(selectedShoot = action.booking)
                }
                loadTasksForShoot(action.booking.id)
            }

            PhotographerAction.DismissDetail ->
                _state.update { it.copy(selectedShoot = null, tasks = emptyList()) }

            is PhotographerAction.MarkShootDone ->
                markDone(action.bookingId)

            is PhotographerAction.MarkTaskDone ->
                markTaskDone(action.taskId, action.done)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMyShootsUseCase()
                .onSuccess { shoots ->
                    _state.update {
                        it.copy(shoots = shoots, isLoading = false)
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

    private fun loadTasksForShoot(bookingId: String) {
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
                    status = BookingStatus.SHOOT_DONE
                )
            ).onSuccess {
                _uiEvent.send(PhotographerUiEvent.StatusUpdated)
                _state.update { it.copy(selectedShoot = null) }
                load()
            }.onFailure { e ->
                _uiEvent.send(
                    PhotographerUiEvent.ShowSnackbar(
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
                    _state.value.selectedShoot?.let {
                        loadTasksForShoot(it.id)
                    }
                }
        }
    }
}