package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object NotificationsTable : Table("notifications") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val title = varchar("title", 200)
    val message = text("message")
    val isRead = bool("is_read").default(false)
    val bookingId = uuid("booking_id").references(BookingsTable.id).nullable()
    val taskId = uuid("task_id").references(TasksTable.id).nullable()
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}