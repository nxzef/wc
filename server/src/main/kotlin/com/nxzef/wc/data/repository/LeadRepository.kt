package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.CreateLeadRequest
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.UpdateLeadStatusRequest
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

class LeadRepository {

    fun rowToLead(row: ResultRow): Lead {
        val customStatusId = row[LeadsTable.customStatusId]
        val customStatus: LeadStatus?
        val statusName: String

        if (customStatusId != null) {
            val statusRow = LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.id eq customStatusId }
                .singleOrNull()
            statusName = statusRow?.get(LeadStatusesTable.name) ?: row[LeadsTable.status]
            customStatus = statusRow?.let {
                LeadStatus(
                    id = it[LeadStatusesTable.id].toString(),
                    name = it[LeadStatusesTable.name],
                    color = it[LeadStatusesTable.color],
                    isDefault = it[LeadStatusesTable.isDefault]
                )
            }
        } else {
            statusName = row[LeadsTable.status]
            customStatus = null
        }

        return Lead(
            id = row[LeadsTable.id].toString(),
            fullName = row[LeadsTable.fullName],
            phone = row[LeadsTable.phone],
            email = row[LeadsTable.email],
            source = LeadSource.valueOf(row[LeadsTable.leadSource]),
            eventType = EventType.valueOf(row[LeadsTable.eventType]),
            eventDate = row[LeadsTable.eventDate]?.toString(),
            location = row[LeadsTable.location],
            statusName = statusName,
            customStatus = customStatus,
            priority = row[LeadsTable.priority],
            lostReason = row[LeadsTable.lostReason],
            notes = row[LeadsTable.notes],
            addedBy = row[LeadsTable.addedBy].toString(),
            assignedTo = row[LeadsTable.assignedTo].toString(),
            createdAt = row[LeadsTable.createdAt].toString()
        )
    }

    fun getAll(teamId: String): List<Lead> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            LeadsTable
                .selectAll()
                .where { LeadsTable.teamId eq tUuid }
                .orderBy(LeadsTable.createdAt, SortOrder.DESC)
                .map { rowToLead(it) }
        }
    }

    fun getById(id: String, teamId: String): Lead? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            LeadsTable
                .selectAll()
                .where { (LeadsTable.id eq UUID.fromString(id)) and (LeadsTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { rowToLead(it) }
        }
    }

    fun create(request: CreateLeadRequest, addedByUserId: String, teamId: String): Lead {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val defaultStatusId = LeadStatusesTable
                .selectAll()
                .where { (LeadStatusesTable.isDefault eq true) and (LeadStatusesTable.teamId eq tUuid) }
                .firstOrNull()
                ?.get(LeadStatusesTable.id)
                ?: error("No default lead status found for team $teamId")

            val id = LeadsTable.insert {
                it[fullName] = request.fullName
                it[phone] = request.phone
                it[email] = request.email
                it[leadSource] = request.source.name
                it[eventType] = request.eventType.name
                it[eventDate] = request.eventDate?.let { d -> LocalDate.parse(d) }
                it[location] = request.location
                it[priority] = request.priority
                it[status] = "New"
                it[statusId] = defaultStatusId
                it[customStatusId] = defaultStatusId
                it[notes] = request.notes
                it[addedBy] = UUID.fromString(addedByUserId)
                it[assignedTo] = UUID.fromString(request.assignedTo)
                it[LeadsTable.teamId] = tUuid
                it[createdAt] = Instant.now()
            } get LeadsTable.id

            getById(id.toString(), teamId)!!
        }
    }

    fun updateStatus(id: String, request: UpdateLeadStatusRequest, teamId: String): Lead? {
        val tUuid = UUID.fromString(teamId)
        val sUuid = try { UUID.fromString(request.customStatusId) } catch (_: Exception) { return null }

        return transaction {
            // Validate that the custom status belongs to the same team
            val statusExists = LeadStatusesTable
                .selectAll()
                .where { (LeadStatusesTable.id eq sUuid) and (LeadStatusesTable.teamId eq tUuid) }
                .any()

            if (!statusExists) return@transaction null

            LeadsTable.update(
                { (LeadsTable.id eq UUID.fromString(id)) and (LeadsTable.teamId eq tUuid) }
            ) {
                it[customStatusId] = sUuid
                it[lostReason] = request.lostReason
                if (request.notes != null) {
                    it[notes] = request.notes
                }
            }
            getById(id, teamId)
        }
    }
}
