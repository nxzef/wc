package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.DatabaseFactory
import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.data.db.tables.TeamsTable
import com.nxzef.wc.data.db.tables.UsersTable
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.CreateBookingRequest
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.UpdateBookingRequest
import com.nxzef.wc.shared.model.UserRole
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
    private val testTeamId = UUID.randomUUID().toString()
    private var testUserId: UUID? = null
    private var testStatusId: UUID? = null

    @Before
    fun setup() {
        DatabaseFactory.init("jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;MODE=PostgreSQL") // Unique DB per test
        repository = BookingRepository()

        // Seed test data
        transaction {
            // Ensure tables are created for this connection
            SchemaUtils.create(
                UsersTable, TeamsTable, LeadStatusesTable, LeadsTable, BookingsTable, QuotesTable
            )

            val tUuid = UUID.fromString(testTeamId)
            val userId = UUID.randomUUID()
            testUserId = userId

            // Create test user first (to satisfy TeamsTable.ownerId FK)
            UsersTable.insert {
                it[id] = userId
                it[name] = "Test User"
                it[email] = "test@example.com"
                it[passwordHash] = "hash"
                it[role] = UserRole.LEAD_MANAGER.name
                it[teamId] = null // Temporarily null until team exists
                it[isActive] = true
                it[createdAt] = Instant.now()
            }

            // Create test team
            TeamsTable.insert {
                it[id] = tUuid
                it[name] = "Test Team"
                it[ownerId] = userId
                it[inviteCode] = "TEST1234"
                it[createdAt] = Instant.now()
            }

            // Update user with teamId
            UsersTable.update({ UsersTable.id eq userId }) {
                it[teamId] = tUuid
            }

            // Create test lead status
            val statusId = UUID.randomUUID()
            testStatusId = statusId
            LeadStatusesTable.insert {
                it[id] = statusId
                it[name] = "New"
                it[isDefault] = true
                it[teamId] = tUuid
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
                it[status] = "NEW"
                it[LeadsTable.statusId] = statusId
                it[addedBy] = userId
                it[assignedTo] = userId
                it[teamId] = tUuid
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

        val booking = repository.create(request, testTeamId)

        assertNotNull(booking)
        assertEquals(BookingStatus.BOOKED, booking.status)
        assertEquals(request.eventDate, booking.eventDate)
        assertEquals(request.eventType, booking.eventType)
        assertEquals(request.location, booking.location)
        assertEquals(request.notes, booking.notes)

        // Lead status is managed separately via custom statuses, not auto-updated on booking
        val leadStatus = transaction {
            LeadsTable.selectAll().single()[LeadsTable.status]
        }
        assertEquals("NEW", leadStatus)
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

        val created = repository.create(request, testTeamId)
        val retrieved = repository.getById(created.id, testTeamId)

        assertNotNull(retrieved)
        assertEquals(created.id, retrieved.id)
        assertEquals(created.status, retrieved.status)
    }

    @Test
    fun `getById should return null if not exists`() {
        val booking = repository.getById(UUID.randomUUID().toString(), testTeamId)
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

        val booking = repository.create(createRequest, testTeamId)

        val updateRequest = UpdateBookingRequest(
            status = BookingStatus.SHOOT_DONE,
            photographerId = transaction { UsersTable.selectAll().single()[UsersTable.id].toString() },
            editorId = null,
            notes = "Updated notes"
        )

        val updated = repository.update(booking.id, updateRequest, testTeamId)

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
        ), testTeamId)

        // Create another lead for second booking
        val leadId2 = UUID.randomUUID().toString()
        transaction {
            LeadsTable.insert {
                it[id] = UUID.fromString(leadId2)
                it[fullName] = "Test Lead 2"
                it[phone] = "0987654321"
                it[leadSource] = LeadSource.FACEBOOK.name
                it[eventType] = EventType.PORTRAIT.name
                it[status] = "NEW"
                it[statusId] = testStatusId!!
                it[addedBy] = testUserId!!
                it[assignedTo] = testUserId!!
                it[teamId] = UUID.fromString(testTeamId)
                it[createdAt] = Instant.now()
            }
        }

        val booking2 = repository.create(CreateBookingRequest(
            leadId = leadId2,
            eventDate = "2024-12-15",
            eventType = "Portrait",
            location = "Venue 2",
            notes = "Second"
        ), testTeamId)

        val allBookings = repository.getAll(testTeamId)

        assertEquals(2, allBookings.size)
        // Should be ordered by event date ASC
        assertEquals("2024-12-15", allBookings[0].eventDate)
        assertEquals("2024-12-20", allBookings[1].eventDate)
    }
}