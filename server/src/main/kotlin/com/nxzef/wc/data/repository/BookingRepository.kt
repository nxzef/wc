package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate

class BookingRepository {

    private fun rowToBooking(row: ResultRow): Booking {
        return Booking(
            id = row[BookingsTable.id].toString(),
            leadId = row[BookingsTable.leadId].toString(),
            quoteId = row[BookingsTable.quoteId].toString(),
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
            // Mark lead as WON
            LeadsTable.update(
                { LeadsTable.id eq java.util.UUID.fromString(request.leadId) }
            ) {
                it[LeadsTable.status] = LeadStatus.WON.name
            }

            val id = BookingsTable.insert {
                it[leadId] = java.util.UUID.fromString(request.leadId)
                it[quoteId] = java.util.UUID.fromString(request.quoteId)
                it[eventDate] = LocalDate.parse(request.eventDate)
                it[eventType] = request.eventType
                it[location] = request.location
                it[status] = BookingStatus.BOOKED.name
                it[notes] = request.notes
                it[createdAt] = Instant.now()
            } get BookingsTable.id

            getById(id.toString())!!
        }
    }

    fun update(id: String, request: UpdateBookingRequest): Booking? {
        return transaction {
            BookingsTable.update(
                { BookingsTable.id eq java.util.UUID.fromString(id) }
            ) {
                it[status] = request.status.name
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