package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.UpdateLeadStatusRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate

class LeadRepository {

    fun rowToLead(row: ResultRow): Lead {
        return Lead(
            id = row[LeadsTable.id].toString(),
            fullName = row[LeadsTable.fullName],
            phone = row[LeadsTable.phone],
            email = row[LeadsTable.email],
            source = LeadSource.valueOf(row[LeadsTable.leadSource]),
            eventType = EventType.valueOf(row[LeadsTable.eventType]),
            eventDate = row[LeadsTable.eventDate]?.toString(),
            location = row[LeadsTable.location],
            status = LeadStatus.valueOf(row[LeadsTable.status]),
            lostReason = row[LeadsTable.lostReason],
            notes = row[LeadsTable.notes],
            addedBy = row[LeadsTable.addedBy].toString(),
            assignedTo = row[LeadsTable.assignedTo].toString(),
            createdAt = row[LeadsTable.createdAt].toString()
        )
    }

    fun getAll(): List<Lead> {
        return transaction {
            LeadsTable
                .selectAll()
                .orderBy(LeadsTable.createdAt, SortOrder.DESC)
                .map { rowToLead(it) }
        }
    }

    fun getById(id: String): Lead? {
        return transaction {
            LeadsTable
                .selectAll()
                .where { LeadsTable.id eq java.util.UUID.fromString(id) }
                .singleOrNull()
                ?.let { rowToLead(it) }
        }
    }

    fun getByStatus(status: LeadStatus): List<Lead> {
        return transaction {
            LeadsTable
                .selectAll()
                .where { LeadsTable.status eq status.name }
                .orderBy(LeadsTable.createdAt, SortOrder.DESC)
                .map { rowToLead(it) }
        }
    }

    fun create(request: CreateLeadRequest, addedByUserId: String): Lead {
        return transaction {
            val id = LeadsTable.insert {
                it[fullName] = request.fullName
                it[phone] = request.phone
                it[email] = request.email
                it[leadSource] = request.source.name
                it[eventType] = request.eventType.name
                it[eventDate] = request.eventDate?.let { d ->
                    LocalDate.parse(d)
                }
                it[location] = request.location
                it[status] = LeadStatus.NEW.name
                it[notes] = request.notes
                it[addedBy] = java.util.UUID.fromString(addedByUserId)
                it[assignedTo] = java.util.UUID.fromString(request.assignedTo)
                it[createdAt] = Instant.now()
            } get LeadsTable.id

            getById(id.toString())!!
        }
    }

    fun updateStatus(
        id: String,
        request: UpdateLeadStatusRequest
    ): Lead? {
        return transaction {
            LeadsTable.update(
                { LeadsTable.id eq java.util.UUID.fromString(id) }
            ) {
                it[status] = request.status.name
                it[lostReason] = request.lostReason
                if (request.notes != null) {
                    it[notes] = request.notes
                }
            }
            getById(id)
        }
    }
}