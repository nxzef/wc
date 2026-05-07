package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.LeadStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class LeadStatusRepository {

    fun rowToLeadStatus(row: ResultRow): LeadStatus = LeadStatus(
        id = row[LeadStatusesTable.id].toString(),
        name = row[LeadStatusesTable.name],
        color = row[LeadStatusesTable.color],
        isDefault = row[LeadStatusesTable.isDefault]
    )

    fun getAll(teamId: String): List<LeadStatus> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.teamId eq tUuid }
                .orderBy(LeadStatusesTable.position, SortOrder.ASC)
                .map { rowToLeadStatus(it) }
        }
    }

    fun getById(id: String, teamId: String): LeadStatus? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where {
                    (LeadStatusesTable.id eq UUID.fromString(id)) and
                            (LeadStatusesTable.teamId eq tUuid)
                }
                .singleOrNull()
                ?.let { rowToLeadStatus(it) }
        }
    }

    fun findByName(name: String, teamId: String): LeadStatus? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where {
                    (LeadStatusesTable.name.lowerCase() eq name.trim().lowercase()) and
                            (LeadStatusesTable.teamId eq tUuid)
                }
                .firstOrNull()
                ?.let { rowToLeadStatus(it) }
        }
    }

    fun getDefault(teamId: String): LeadStatus? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where {
                    (LeadStatusesTable.isDefault eq true) and
                            (LeadStatusesTable.teamId eq tUuid)
                }
                .firstOrNull()
                ?.let { rowToLeadStatus(it) }
        }
    }

    fun create(name: String, color: String, teamId: String): LeadStatus {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val nextPosition = LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.teamId eq tUuid }
                .count().toInt()
            val newId = LeadStatusesTable.insert {
                it[LeadStatusesTable.name] = name
                it[LeadStatusesTable.color] = color
                it[LeadStatusesTable.position] = nextPosition
                it[LeadStatusesTable.isDefault] = false
                it[LeadStatusesTable.teamId] = tUuid
                it[LeadStatusesTable.createdAt] = Instant.now()
            }[LeadStatusesTable.id]
            getById(newId.toString(), teamId)!!
        }
    }

    /**
     * Returns one of:
     *   "ok"          — deleted (and any leads were reassigned to the default status)
     *   "is_default"  — refused: cannot delete the default status
     *   "only_one"    — refused: only one status remains
     *   "not_found"   — id didn't match any row
     */
    fun delete(id: String, teamId: String): String {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val uuid = try { UUID.fromString(id) } catch (_: Exception) {
                return@transaction "not_found"
            }
            val row = LeadStatusesTable.selectAll()
                .where { (LeadStatusesTable.id eq uuid) and (LeadStatusesTable.teamId eq tUuid) }
                .singleOrNull() ?: return@transaction "not_found"

            val totalCount = LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.teamId eq tUuid }
                .count()
            if (totalCount <= 1L) return@transaction "only_one"

            if (row[LeadStatusesTable.isDefault]) return@transaction "is_default"

            val defaultId = LeadStatusesTable
                .selectAll()
                .where {
                    (LeadStatusesTable.isDefault eq true) and
                            (LeadStatusesTable.teamId eq tUuid)
                }
                .firstOrNull()
                ?.get(LeadStatusesTable.id)
                ?: return@transaction "not_found"

            LeadsTable.update({ LeadsTable.statusId eq uuid }) {
                it[statusId] = defaultId
            }
            LeadsTable.update({ LeadsTable.customStatusId eq uuid }) {
                it[customStatusId] = defaultId
            }

            LeadStatusesTable.deleteWhere { LeadStatusesTable.id eq uuid }
            "ok"
        }
    }

    fun seedDefaultForTeam(teamId: String) {
        val tUuid = UUID.fromString(teamId)
        transaction {
            val count = LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.teamId eq tUuid }
                .count()
            if (count == 0L) {
                LeadStatusesTable.insert {
                    it[name] = "New"
                    it[color] = "#2196F3"
                    it[position] = 0
                    it[isDefault] = true
                    it[LeadStatusesTable.teamId] = tUuid
                    it[createdAt] = Instant.now()
                }
            }
        }
    }
}
