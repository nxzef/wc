package com.nxzef.wc.presentation.screens.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.domain.usecase.leads.GetAllLeadsUseCase
import com.nxzef.wc.domain.usecase.bookings.GetAllBookingsUseCase
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.domain.usecase.tasks.CreateTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.DeleteTaskUseCase
import com.nxzef.wc.domain.usecase.tasks.GetTasksByBookingUseCase
import com.nxzef.wc.domain.usecase.tasks.MarkTaskDoneUseCase
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.util.applySearch
import com.nxzef.wc.shared.model.CreateTaskRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingViewModel(
    private val getAllBookingsUseCase: GetAllBookingsUseCase,
    private val getAllLeadsUseCase: GetAllLeadsUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val getTasksByBookingUseCase: GetTasksByBookingUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val markTaskDoneUseCase: MarkTaskDoneUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    private val _uiEvent = Channel<BookingUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var hasLoadedOnce = false
    private var previousBookingCount = -1

    init {
        load()
        collectRefreshTrigger()
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
            if (!silent) _state.update { it.copy(isLoading = true) }
            val oldCount = previousBookingCount
            val bookingsResult = getAllBookingsUseCase()
            val leadsResult = getAllLeadsUseCase()
            val teamResult = userRepository.getTeam()

            bookingsResult.onSuccess { bookings ->
                val newCount = bookings.size
                if (hasLoadedOnce && silent && newCount != oldCount) {
                    _uiEvent.send(BookingUiEvent.ShowSnackbar("Updated"))
                }
                previousBookingCount = newCount
                hasLoadedOnce = true
                _state.update { it.copy(bookings = bookings) }
            }.onFailure {
                if (!silent) {
                    _uiEvent.send(BookingUiEvent.ShowSnackbar("Failed to load bookings."))
                }
            }
            leadsResult.onSuccess { leads ->
                _state.update { it.copy(leads = leads) }
            }
            teamResult.onSuccess { team ->
                _state.update { it.copy(team = team) }
            }
            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun onAction(action: BookingAction) {
        when (action) {
            BookingAction.LoadBookings -> load(silent = false)
            is BookingAction.SelectBooking -> {
                _state.update { it.copy(selectedBooking = action.booking) }
                loadTasks(action.booking.id)
            }
            BookingAction.DismissDetail ->
                _state.update { it.copy(selectedBooking = null, tasks = emptyList()) }
            is BookingAction.OnUpdateStatus ->
                updateStatus(action.bookingId, action.status)
            is BookingAction.OnSearchQueryChange ->
                _state.update { it.copy(searchQuery = action.query) }
            is BookingAction.OnFilterStatus ->
                _state.update { it.copy(filterStatus = action.status) }
            is BookingAction.AssignPhotographer -> assignPhotographer(action.bookingId, action.userId)
            is BookingAction.AssignEditor -> assignEditor(action.bookingId, action.userId)
            is BookingAction.OnTaskToggle -> toggleTask(action.taskId, action.isDone)
            BookingAction.ShowAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = true) }
            BookingAction.HideAddTaskDialog -> _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
            is BookingAction.OnNewTaskTitleChange -> _state.update { it.copy(newTaskTitle = action.title) }
            BookingAction.OnAddTask -> addTask()
            is BookingAction.OnDeleteTask -> deleteTask(action.taskId)
        }
    }

    private fun assignPhotographer(bookingId: String, userId: String?) {
        viewModelScope.launch {
            updateBookingUseCase(
                id = bookingId,
                request = UpdateBookingRequest(photographerId = userId)
            ).onSuccess {
                load()
                RefreshManager.triggerRefresh()
                if (_state.value.selectedBooking?.id == bookingId) {
                    _state.update { s -> s.copy(selectedBooking = s.bookings.find { it.id == bookingId }) }
                }
            }.onFailure {
                _uiEvent.send(BookingUiEvent.ShowSnackbar("Failed to update booking."))
            }
        }
    }

    private fun assignEditor(bookingId: String, userId: String?) {
        viewModelScope.launch {
            updateBookingUseCase(
                id = bookingId,
                request = UpdateBookingRequest(editorId = userId)
            ).onSuccess {
                load()
                RefreshManager.triggerRefresh()
                if (_state.value.selectedBooking?.id == bookingId) {
                    _state.update { s -> s.copy(selectedBooking = s.bookings.find { it.id == bookingId }) }
                }
            }.onFailure {
                _uiEvent.send(BookingUiEvent.ShowSnackbar("Failed to update booking."))
            }
        }
    }

    private fun loadTasks(bookingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTasksLoading = true) }
            getTasksByBookingUseCase(bookingId).onSuccess { tasks ->
                _state.update { it.copy(tasks = tasks, isTasksLoading = false) }
            }.onFailure {
                _state.update { it.copy(isTasksLoading = false) }
            }
        }
    }

    private fun toggleTask(taskId: String, isDone: Boolean) {
        viewModelScope.launch {
            markTaskDoneUseCase(taskId, isDone).onSuccess {
                val bookingId = _state.value.selectedBooking?.id ?: return@onSuccess
                RefreshManager.triggerRefresh()
                loadTasks(bookingId)
            }
        }
    }

    private fun addTask() {
        val s = _state.value
        val bookingId = s.selectedBooking?.id ?: return
        if (s.newTaskTitle.isBlank()) return

        viewModelScope.launch {
            createTaskUseCase(
                CreateTaskRequest(bookingId = bookingId, title = s.newTaskTitle)
            ).onSuccess {
                _state.update { it.copy(showAddTaskDialog = false, newTaskTitle = "") }
                RefreshManager.triggerRefresh()
                loadTasks(bookingId)
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId).onSuccess {
                val bookingId = _state.value.selectedBooking?.id ?: return@onSuccess
                RefreshManager.triggerRefresh()
                loadTasks(bookingId)
            }
        }
    }

    private fun updateStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            updateBookingUseCase(
                id = bookingId,
                request = UpdateBookingRequest(status = status)
            ).onSuccess {
                load()
                RefreshManager.triggerRefresh()
                if (_state.value.selectedBooking?.id == bookingId) {
                    _state.update { s -> s.copy(selectedBooking = s.bookings.find { it.id == bookingId }) }
                }
            }.onFailure {
                _uiEvent.send(BookingUiEvent.ShowSnackbar("Failed to update booking."))
            }
        }
    }

    val filteredBookings
        get() = _state.value.let { s ->
            val byStatus = if (s.filterStatus == null) s.bookings
                           else s.bookings.filter { it.status == s.filterStatus }
            byStatus.applySearch(s.searchQuery,
                { b -> s.leads.find { it.id == b.leadId }?.fullName },
                { b -> b.eventType },
                { b -> b.location },
                { b -> b.eventDate }
            )
        }
}
