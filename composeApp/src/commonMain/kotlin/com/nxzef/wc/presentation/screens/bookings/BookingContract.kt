package com.nxzef.wc.presentation.screens.bookings

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.User

data class BookingState(
    val bookings: List<Booking> = emptyList(),
    val wonLeads: List<Lead> = emptyList(),
    val team: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val selectedBooking: Booking? = null,
    val showCreateDialog: Boolean = false,
    // Create booking form
    val selectedLeadId: String = "",
    val selectedQuoteId: String = "",
    val eventDate: String = "",
    val eventType: String = "",
    val location: String = "",
    val notes: String = "",
    val isCreating: Boolean = false,
    // Filter
    val filterStatus: BookingStatus? = null
)

sealed interface BookingAction {
    data object LoadBookings : BookingAction
    data object ShowCreateDialog : BookingAction
    data object HideCreateDialog : BookingAction
    data class SelectBooking(val booking: Booking) : BookingAction
    data object DismissDetail : BookingAction
    data class OnLeadSelected(val leadId: String) : BookingAction
    data class OnEventDateChange(val value: String) : BookingAction
    data class OnEventTypeChange(val value: String) : BookingAction
    data class OnLocationChange(val value: String) : BookingAction
    data class OnNotesChange(val value: String) : BookingAction
    data object OnCreateBooking : BookingAction
    data class OnUpdateStatus(
        val bookingId: String,
        val status: BookingStatus
    ) : BookingAction

    data class OnFilterStatus(
        val status: BookingStatus?
    ) : BookingAction
}

sealed interface BookingUiEvent {
    data class ShowSnackbar(val message: String) : BookingUiEvent
    data object BookingCreated : BookingUiEvent
}