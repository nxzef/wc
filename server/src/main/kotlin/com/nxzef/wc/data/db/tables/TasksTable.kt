package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object TasksTable : Table("tasks") {
    val id = uuid("id").autoGenerate()
    val leadId = uuid("lead_id")
        .references(LeadsTable.id)
        .nullable()      // ← nullable now
    val bookingId = uuid("booking_id")
        .references(BookingsTable.id)
        .nullable()      // ← nullable now
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val assignedTo = uuid("assigned_to")
        .references(UsersTable.id)
    val dueDate = date("due_date").nullable()
    val isDone = bool("is_done").default(false)
    val doneAt = timestamp("done_at").nullable()
    val createdBy = uuid("created_by")
        .references(UsersTable.id)
    val createdAt = timestamp("created_at")
    val stageName = varchar("stage_name", 100).nullable()
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()

    override val primaryKey = PrimaryKey(id)
}