package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.UpdateBookingRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BookingRepository {

    private fun rowToBooking(row: ResultRow): Booking {
        return Booking(
            id = row[BookingsTable.id].toString(),
            leadId = row[BookingsTable.leadId].toString(),
            quoteId = row[BookingsTable.quoteId]?.toString(),
            photographerId = row[BookingsTable.photographerId]?.toString(),
            editorId = row[BookingsTable.editorId]?.toString(),
            eventDate = row[BookingsTable.eventDate].toString(),
            eventEndDate = row[BookingsTable.eventEndDate]?.toString(),
            eventType = row[BookingsTable.eventType],
            location = row[BookingsTable.location],
            status = BookingStatus.valueOf(row[BookingsTable.status]),
            notes = row[BookingsTable.notes],
            createdAt = row[BookingsTable.createdAt].toString()
        )
    }

    fun getAll(teamId: String): List<Booking> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            BookingsTable
                .selectAll()
                .where { BookingsTable.teamId eq tUuid }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun getById(id: String, teamId: String): Booking? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            BookingsTable
                .selectAll()
                .where { (BookingsTable.id eq UUID.fromString(id)) and (BookingsTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { rowToBooking(it) }
        }
    }

    fun getByPhotographer(photographerId: String, teamId: String): List<Booking> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            BookingsTable
                .selectAll()
                .where {
                    (BookingsTable.photographerId eq UUID.fromString(photographerId)) and
                            (BookingsTable.teamId eq tUuid)
                }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun getByEditor(editorId: String, teamId: String): List<Booking> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            BookingsTable
                .selectAll()
                .where {
                    (BookingsTable.editorId eq UUID.fromString(editorId)) and
                            (BookingsTable.teamId eq tUuid)
                }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .map { rowToBooking(it) }
        }
    }

    fun create(request: CreateBookingRequest, teamId: String): Booking {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val newId = UUID.randomUUID()
            BookingsTable.insert { statement ->
                statement[id] = newId
                statement[leadId] = UUID.fromString(request.leadId)
                statement[quoteId] = request.quoteId
                    ?.takeIf { it.isNotBlank() && it != "null" }
                    ?.let { UUID.fromString(it) }
                statement[eventDate] = LocalDate.parse(request.eventDate)
                statement[eventEndDate] = request.eventEndDate?.let { LocalDate.parse(it) }
                statement[eventType] = request.eventType
                statement[location] = request.location
                statement[status] = BookingStatus.BOOKED.name
                statement[notes] = request.notes
                statement[BookingsTable.teamId] = tUuid
                statement[createdAt] = Instant.now()
            }

            getById(newId.toString(), teamId)!!
        }
    }

    fun update(id: String, request: UpdateBookingRequest, teamId: String): Booking? {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            BookingsTable.update(
                { (BookingsTable.id eq UUID.fromString(id)) and (BookingsTable.teamId eq tUuid) }
            ) {
                if (request.status != null) {
                    it[status] = request.status!!.name
                }
                if (request.photographerId != null) {
                    it[photographerId] = UUID.fromString(request.photographerId)
                }
                if (request.editorId != null) {
                    it[editorId] = UUID.fromString(request.editorId)
                }
                if (request.notes != null) {
                    it[notes] = request.notes
                }
            }
            getById(id, teamId)
        }
    }
}
