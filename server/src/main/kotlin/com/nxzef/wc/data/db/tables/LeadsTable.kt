package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal

object LeadsTable : Table("leads") {
    val id = uuid("id").autoGenerate()
    val fullName = varchar("full_name", 100)
    val phone = varchar("phone", 20)
    val email = varchar("email", 100).nullable()
    val leadSource = varchar("source", 50)
    val eventType = varchar("event_type", 50)
    val eventDate = date("event_date").nullable()
    val eventEndDate = date("event_end_date").nullable()
    val location = varchar("location", 200).nullable()
    val status = varchar("status", 50).default("NEW")
    val statusId = uuid("status_id").references(LeadStatusesTable.id)
    val customStatusId = uuid("custom_status_id").references(LeadStatusesTable.id).nullable()
    val priority = integer("priority").default(0)
    val lostReason = varchar("lost_reason", 500).nullable()
    val notes = text("notes").nullable()
    val addedBy = uuid("added_by").references(UsersTable.id)
    val assignedTo = uuid("assigned_to").references(UsersTable.id)
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()
    val budget = decimal("budget", precision = 12, scale = 2).default(BigDecimal.ZERO)
    val isWon = bool("is_won").default(false)
    val isLost = bool("is_lost").default(false)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}