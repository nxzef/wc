package com.nxzef.wc.shared.domain

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.UpdateBookingRequest

class BookingDomainService {

    /**
     * Validates booking creation request
     */
    fun validateCreateBooking(request: CreateBookingRequest) {
        require(request.leadId.isNotBlank()) { "Lead ID cannot be blank" }
        require(request.eventDate.isNotBlank()) { "Event date cannot be blank" }
        require(request.eventType.isNotBlank()) { "Event type cannot be blank" }
        require(request.location.isNotBlank()) { "Location cannot be blank" }

        // Validate event date format
        kotlin.runCatching {
            kotlinx.datetime.LocalDate.parse(request.eventDate)
        }.getOrElse {
            throw IllegalArgumentException("Invalid event date format")
        }
    }

    /**
     * Determines the initial status for a new booking
     */
    fun getInitialBookingStatus(): BookingStatus = BookingStatus.BOOKED

    /**
     * Determines the lead status after booking creation
     */
    fun getLeadStatusAfterBooking(): LeadStatus = LeadStatus.WON

    /**
     * Validates booking update request
     */
    fun validateUpdateBooking(currentBooking: Booking, request: UpdateBookingRequest) {
        // Cannot update completed bookings
        if (currentBooking.status == BookingStatus.DELIVERED) {
            throw IllegalStateException("Cannot update delivered bookings")
        }

        // Validate status transitions
        if (request.status != null) {
            validateStatusTransition(currentBooking.status, request.status)
        }
    }

    /**
     * Validates status transition rules
     */
    private fun validateStatusTransition(from: BookingStatus, to: BookingStatus) {
        val validTransitions = mapOf(
            BookingStatus.BOOKED to setOf(BookingStatus.SHOOT_DONE, BookingStatus.EDITING),
            BookingStatus.SHOOT_DONE to setOf(BookingStatus.EDITING),
            BookingStatus.EDITING to setOf(BookingStatus.DELIVERED),
            BookingStatus.DELIVERED to setOf(BookingStatus.CLOSED)
        )

        val allowed = validTransitions[from]?.contains(to) ?: false
        if (!allowed) {
            throw IllegalArgumentException("Invalid status transition from $from to $to")
        }
    }

    /**
     * Checks if a booking can be assigned to a photographer
     */
    fun canAssignPhotographer(booking: Booking): Boolean {
        return booking.status in setOf(BookingStatus.BOOKED, BookingStatus.SHOOT_DONE)
    }

    /**
     * Checks if a booking can be assigned to an editor
     */
    fun canAssignEditor(booking: Booking): Boolean {
        return booking.status in setOf(BookingStatus.SHOOT_DONE, BookingStatus.EDITING)
    }
}