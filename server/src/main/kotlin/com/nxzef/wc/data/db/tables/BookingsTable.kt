package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object BookingsTable : Table("bookings") {
    val id = uuid("id").autoGenerate()
    val leadId = uuid("lead_id").references(LeadsTable.id)
    val quoteId = uuid("quote_id")
        .references(QuotesTable.id)
        .nullable()
    val photographerId = uuid("photographer_id").references(UsersTable.id).nullable()
    val editorId = uuid("editor_id").references(UsersTable.id).nullable()
    val eventDate = date("event_date")
    val eventEndDate = date("event_end_date").nullable()
    val eventType = varchar("event_type", 50)
    val location = varchar("location", 200)
    val status = varchar("status", 50).default("BOOKED")
    val notes = text("notes").nullable()
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}