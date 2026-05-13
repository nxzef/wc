package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    BOOKED, SHOOT_DONE, EDITING, DELIVERED, CLOSED
}

@Serializable
data class Booking(
    val id: String,
    val leadId: String,
    val quoteId: String? = null,
    val photographerId: String? = null,
    val editorId: String? = null,
    val eventDate: String,
    val eventEndDate: String? = null,
    val eventType: String,
    val location: String,
    val status: BookingStatus,
    val notes: String? = null,
    val createdAt: String
)

@Serializable
data class CreateBookingRequest(
    val leadId: String,
    val quoteId: String? = null,
    val eventDate: String,
    val eventEndDate: String? = null,
    val eventType: String,
    val location: String,
    val notes: String? = null
)

@Serializable
data class UpdateBookingRequest(
    val status: BookingStatus? = null,
    val photographerId: String? = null,
    val editorId: String? = null,
    val notes: String? = null
)
