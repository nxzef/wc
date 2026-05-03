package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.shared.model.LeadStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class LeadStatusRepository {

    fun rowToLeadStatus(row: ResultRow): LeadStatus = LeadStatus(
        id = row[LeadStatusesTable.id].toString(),
        name = row[LeadStatusesTable.name],
        color = row[LeadStatusesTable.color],
        isDefault = row[LeadStatusesTable.isDefault]
    )

    fun getAll(): List<LeadStatus> {
        return transaction {
            LeadStatusesTable
                .selectAll()
                .orderBy(LeadStatusesTable.position, SortOrder.ASC)
                .map { rowToLeadStatus(it) }
        }
    }

    fun getById(id: String): LeadStatus? {
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.id eq java.util.UUID.fromString(id) }
                .singleOrNull()
                ?.let { rowToLeadStatus(it) }
        }
    }

    fun getDefault(): LeadStatus? {
        return transaction {
            LeadStatusesTable
                .selectAll()
                .where { LeadStatusesTable.isDefault eq true }
                .firstOrNull()
                ?.let { rowToLeadStatus(it) }
        }
    }

    fun create(name: String, color: String): LeadStatus {
        return transaction {
            val nextPosition = LeadStatusesTable.selectAll().count().toInt()
            val newId = LeadStatusesTable.insert {
                it[LeadStatusesTable.name] = name
                it[LeadStatusesTable.color] = color
                it[LeadStatusesTable.position] = nextPosition
                it[LeadStatusesTable.isDefault] = false
                it[LeadStatusesTable.createdAt] = Instant.now()
            }[LeadStatusesTable.id]
            getById(newId.toString())!!
        }
    }

    fun delete(id: String): Boolean {
        return transaction {
            val deleted = LeadStatusesTable.deleteWhere {
                LeadStatusesTable.id eq java.util.UUID.fromString(id) and
                        (LeadStatusesTable.isDefault eq false)
            }
            deleted > 0
        }
    }

    fun seedDefault() {
        transaction {
            val count = LeadStatusesTable.selectAll().count()
            if (count == 0L) {
                LeadStatusesTable.insert {
                    it[name] = "New"
                    it[color] = "#2196F3"
                    it[position] = 0
                    it[isDefault] = true
                    it[createdAt] = Instant.now()
                }
            }
        }
    }
}
