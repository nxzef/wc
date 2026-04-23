package com.nxzef.wc.presentation.screens.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.domain.repository.UserRepository
import com.nxzef.wc.domain.usecase.bookings.CreateBookingUseCase
import com.nxzef.wc.domain.usecase.bookings.GetAllBookingsUseCase
import com.nxzef.wc.domain.usecase.bookings.UpdateBookingUseCase
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingViewModel(
    private val getAllBookingsUseCase: GetAllBookingsUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val leadRepository: LeadRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    private val _uiEvent = Channel<BookingUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Load bookings, won leads and team in parallel
            val bookingsResult = getAllBookingsUseCase()
            val leadsResult = leadRepository.getAll()
            val teamResult = userRepository.getTeam()

            bookingsResult.onSuccess { bookings ->
                _state.update { it.copy(bookings = bookings) }
            }
            leadsResult.onSuccess { leads ->
                _state.update {
                    it.copy(
                        wonLeads = leads.filter { l ->
                            l.status == LeadStatus.WON
                        }
                    )
                }
            }
            teamResult.onSuccess { team ->
                _state.update { it.copy(team = team) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(action: BookingAction) {
        when (action) {
            BookingAction.LoadBookings -> load()
            BookingAction.ShowCreateDialog ->
                _state.update { it.copy(showCreateDialog = true) }

            BookingAction.HideCreateDialog ->
                _state.update { it.copy(showCreateDialog = false) }

            is BookingAction.SelectBooking ->
                _state.update {
                    it.copy(selectedBooking = action.booking)
                }

            BookingAction.DismissDetail ->
                _state.update { it.copy(selectedBooking = null) }

            is BookingAction.OnLeadSelected ->
                _state.update { it.copy(selectedLeadId = action.leadId) }

            is BookingAction.OnEventDateChange ->
                _state.update { it.copy(eventDate = action.value) }

            is BookingAction.OnEventTypeChange ->
                _state.update { it.copy(eventType = action.value) }

            is BookingAction.OnLocationChange ->
                _state.update { it.copy(location = action.value) }

            is BookingAction.OnNotesChange ->
                _state.update { it.copy(notes = action.value) }

            BookingAction.OnCreateBooking -> createBooking()
            is BookingAction.OnUpdateStatus ->
                updateStatus(action.bookingId, action.status)

            is BookingAction.OnFilterStatus ->
                _state.update { it.copy(filterStatus = action.status) }
        }
    }

    private fun createBooking() {
        val s = _state.value
        if (s.selectedLeadId.isBlank() ||
            s.eventDate.isBlank() ||
            s.location.isBlank()
        ) {
            viewModelScope.launch {
                _uiEvent.send(
                    BookingUiEvent.ShowSnackbar(
                        "Lead, date and location are required"
                    )
                )
            }
            return
        }

        // Get quote from selected lead
        val lead = s.wonLeads.find { it.id == s.selectedLeadId }
        val quoteId = s.selectedQuoteId.ifBlank {
            "" // will be handled by server
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }
            createBookingUseCase(
                CreateBookingRequest(
                    leadId = s.selectedLeadId,
                    quoteId = quoteId,
                    eventDate = s.eventDate,
                    eventType = s.eventType.ifBlank {
                        lead?.eventType?.name ?: "WEDDING"
                    },
                    location = s.location,
                    notes = s.notes.ifBlank { null }
                )
            ).onSuccess {
                _state.update {
                    it.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        selectedLeadId = "",
                        eventDate = "",
                        location = "",
                        notes = ""
                    )
                }
                _uiEvent.send(BookingUiEvent.BookingCreated)
                load()
            }.onFailure { e ->
                _state.update { it.copy(isCreating = false) }
                _uiEvent.send(
                    BookingUiEvent.ShowSnackbar(
                        e.message ?: "Failed to create booking"
                    )
                )
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
            }.onFailure { e ->
                _uiEvent.send(
                    BookingUiEvent.ShowSnackbar(
                        e.message ?: "Failed to update"
                    )
                )
            }
        }
    }

    val filteredBookings
        get() = _state.value.let { s ->
            if (s.filterStatus == null) s.bookings
            else s.bookings.filter { it.status == s.filterStatus }
        }
}