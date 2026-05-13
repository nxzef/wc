package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: String,
    val leadId: String,
    val quoteId: String? = null,
    val photographerId: String? = null,
    val editorId: String? = null,
    val eventDate: String,
    val eventEndDate: String? = null,
    val eventType: String,
    val location: String,
    val status: String,
    val notes: String? = null,
    val createdAt: String
)

fun BookingDto.toDomain(): Booking {
    return Booking(
        id = id,
        leadId = leadId,
        quoteId = quoteId,
        photographerId = photographerId,
        editorId = editorId,
        eventDate = eventDate,
        eventEndDate = eventEndDate,
        eventType = eventType,
        location = location,
        status = try { BookingStatus.valueOf(status) } catch (_: Exception) { BookingStatus.BOOKED },
        notes = notes,
        createdAt = createdAt
    )
}

fun Booking.toDto(): BookingDto {
    return BookingDto(
        id = id,
        leadId = leadId,
        quoteId = quoteId,
        photographerId = photographerId,
        editorId = editorId,
        eventDate = eventDate,
        eventEndDate = eventEndDate,
        eventType = eventType,
        location = location,
        status = status.name,
        notes = notes,
        createdAt = createdAt
    )
}
