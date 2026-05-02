package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate

class BookingRepository {

    private fun rowToBooking(row: ResultRow): Booking {
        return Booking(
            id = row[BookingsTable.id].toString(),
            leadId = row[BookingsTable.leadId].toString(),
            quoteId = row[BookingsTable.quoteId]?.toString(),
            photographerId = row[BookingsTable.photographerId]?.toString(),
            editorId = row[BookingsTable.editorId]?.toString(),
            eventDate = row[BookingsTable.eventDate].toString(),
            eventType = row[BookingsTable.eventType],
            location = row[BookingsTable.location],
            status = BookingStatus.valueOf(row[BookingsTable.status]),
            notes = row[BookingsTable.notes],
            createdAt = row[BookingsTable.createdAt].toString()
        )
    }

    fun getAll(): List<Booking> {
        return transaction {
            BookingsTable
                .selectAll()
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun getById(id: String): Booking? {
        return transaction {
            BookingsTable
                .selectAll()
                .where {
                    BookingsTable.id eq
                            java.util.UUID.fromString(id)
                }
                .singleOrNull()
                ?.let { rowToBooking(it) }
        }
    }

    fun getByPhotographer(photographerId: String): List<Booking> {
        return transaction {
            BookingsTable
                .selectAll()
                .where {
                    BookingsTable.photographerId eq
                            java.util.UUID.fromString(photographerId)
                }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun getByEditor(editorId: String): List<Booking> {
        return transaction {
            BookingsTable
                .selectAll()
                .where {
                    BookingsTable.editorId eq
                            java.util.UUID.fromString(editorId)
                }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun create(request: CreateBookingRequest): Booking {
        return transaction {
            val newId = java.util.UUID.randomUUID()
            BookingsTable.insert { statement ->
                statement[id] = newId
                statement[leadId] = java.util.UUID.fromString(request.leadId)
                // Only set quoteId if not null or blank
                statement[quoteId] = request.quoteId
                    ?.takeIf { it.isNotBlank() && it != "null" }
                    ?.let { java.util.UUID.fromString(it) }
                statement[eventDate] = LocalDate.parse(request.eventDate)
                statement[eventType] = request.eventType
                statement[location] = request.location
                statement[status] = BookingStatus.BOOKED.name
                statement[notes] = request.notes
                statement[createdAt] = Instant.now()
            }

            getById(newId.toString())!!
        }
    }

    fun update(id: String, request: UpdateBookingRequest): Booking? {
        return transaction {
            BookingsTable.update(
                { BookingsTable.id eq java.util.UUID.fromString(id) }
            ) {
                if (request.status != null) {
                    it[status] = request.status!!.name
                }
                if (request.photographerId != null) {
                    it[photographerId] = java.util.UUID.fromString(
                        request.photographerId
                    )
                }
                if (request.editorId != null) {
                    it[editorId] = java.util.UUID.fromString(
                        request.editorId
                    )
                }
                if (request.notes != null) {
                    it[notes] = request.notes
                }
            }
            getById(id)
        }
    }
}