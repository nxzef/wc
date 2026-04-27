package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.DatabaseFactory
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.data.db.tables.UsersTable
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.model.UserRole
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BookingRepositoryTest {

    private lateinit var repository: BookingRepository
    private val dbName = "test_${UUID.randomUUID()}"

    @Before
    fun setup() {
        DatabaseFactory.init("jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;MODE=PostgreSQL") // Unique DB per test
        repository = BookingRepository()

        // Seed test data
        transaction {
            // Create test user
            val userId = UUID.randomUUID()
            UsersTable.insert {
                it[id] = userId
                it[name] = "Test User"
                it[email] = "test@example.com"
                it[passwordHash] = "hash"
                it[role] = UserRole.LEAD_MANAGER.name
                it[isActive] = true
                it[createdAt] = Instant.now()
            }

            // Create test lead
            val leadId = UUID.randomUUID()
            LeadsTable.insert {
                it[id] = leadId
                it[fullName] = "Test Lead"
                it[phone] = "1234567890"
                it[leadSource] = LeadSource.INSTAGRAM.name
                it[eventType] = EventType.WEDDING.name
                it[eventDate] = LocalDate.parse("2024-12-25")
                it[location] = "Test Location"
                it[status] = LeadStatus.NEW.name
                it[addedBy] = userId
                it[assignedTo] = userId
                it[createdAt] = Instant.now()
            }
        }
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun `create booking should create booking and update lead status`() {
        val request = CreateBookingRequest(
            leadId = transaction { LeadsTable.selectAll().single()[LeadsTable.id].toString() },
            quoteId = null,
            eventDate = "2024-12-25",
            eventType = "Wedding",
            location = "Test Venue",
            notes = "Test booking"
        )

        val booking = repository.create(request)

        assertNotNull(booking)
        assertEquals(BookingStatus.BOOKED, booking.status)
        assertEquals(request.eventDate, booking.eventDate)
        assertEquals(request.eventType, booking.eventType)
        assertEquals(request.location, booking.location)
        assertEquals(request.notes, booking.notes)

        // Verify lead status updated to WON
        val leadStatus = transaction {
            LeadsTable.selectAll().single()[LeadsTable.status]
        }
        assertEquals(LeadStatus.WON.name, leadStatus)
    }

    @Test
    fun `getById should return booking if exists`() {
        val request = CreateBookingRequest(
            leadId = transaction { LeadsTable.selectAll().single()[LeadsTable.id].toString() },
            quoteId = null,
            eventDate = "2024-12-25",
            eventType = "Wedding",
            location = "Test Venue",
            notes = "Test booking"
        )

        val created = repository.create(request)
        val retrieved = repository.getById(created.id)

        assertNotNull(retrieved)
        assertEquals(created.id, retrieved.id)
        assertEquals(created.status, retrieved.status)
    }

    @Test
    fun `getById should return null if not exists`() {
        val booking = repository.getById(UUID.randomUUID().toString())
        assertNull(booking)
    }

    @Test
    fun `update booking should update fields correctly`() {
        val createRequest = CreateBookingRequest(
            leadId = transaction { LeadsTable.selectAll().single()[LeadsTable.id].toString() },
            quoteId = null,
            eventDate = "2024-12-25",
            eventType = "Wedding",
            location = "Test Venue",
            notes = "Test booking"
        )

        val booking = repository.create(createRequest)

        val updateRequest = UpdateBookingRequest(
            status = BookingStatus.SHOOT_DONE,
            photographerId = transaction { UsersTable.selectAll().single()[UsersTable.id].toString() },
            editorId = null,
            notes = "Updated notes"
        )

        val updated = repository.update(booking.id, updateRequest)

        assertNotNull(updated)
        assertEquals(BookingStatus.SHOOT_DONE, updated.status)
        assertEquals(updateRequest.photographerId, updated.photographerId)
        assertEquals(updateRequest.notes, updated.notes)
    }

    @Test
    fun `getAll should return all bookings ordered by event date`() {
        // Create multiple bookings with different dates
        val leadId = transaction { LeadsTable.selectAll().single()[LeadsTable.id].toString() }

        val booking1 = repository.create(CreateBookingRequest(
            leadId = leadId,
            eventDate = "2024-12-20",
            eventType = "Wedding",
            location = "Venue 1",
            notes = "First"
        ))

        // Create another lead for second booking
        val leadId2 = UUID.randomUUID().toString()
        transaction {
            LeadsTable.insert {
                it[id] = UUID.fromString(leadId2)
                it[fullName] = "Test Lead 2"
                it[phone] = "0987654321"
                it[leadSource] = LeadSource.FACEBOOK.name
                it[eventType] = EventType.PORTRAIT.name
                it[status] = LeadStatus.NEW.name
                it[addedBy] = UUID.fromString(UsersTable.selectAll().single()[UsersTable.id].toString())
                it[assignedTo] = UUID.fromString(UsersTable.selectAll().single()[UsersTable.id].toString())
                it[createdAt] = Instant.now()
            }
        }

        val booking2 = repository.create(CreateBookingRequest(
            leadId = leadId2,
            eventDate = "2024-12-15",
            eventType = "Portrait",
            location = "Venue 2",
            notes = "Second"
        ))

        val allBookings = repository.getAll()

        assertEquals(2, allBookings.size)
        // Should be ordered by event date ASC
        assertEquals("2024-12-15", allBookings[0].eventDate)
        assertEquals("2024-12-20", allBookings[1].eventDate)
    }
}