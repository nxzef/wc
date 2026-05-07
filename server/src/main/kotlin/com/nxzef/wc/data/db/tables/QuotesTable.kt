package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object QuotesTable : Table("quotes") {
    val id = uuid("id").autoGenerate()
    val leadId = uuid("lead_id").references(LeadsTable.id)
    val createdBy = uuid("created_by").references(UsersTable.id)
    val validUntil = date("valid_until").nullable()
    val notes = text("notes").nullable()
    val status = varchar("status", 50).default("DRAFT")
    val fileName = varchar("file_name", 500).nullable()
    val totalAmount = decimal("total_amount", precision = 12, scale = 2).default(0.toBigDecimal())
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}