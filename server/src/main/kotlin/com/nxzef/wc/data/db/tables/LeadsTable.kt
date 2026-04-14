package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object LeadsTable : Table("leads") {
    val id = uuid("id").autoGenerate()
    val fullName = varchar("full_name", 100)
    val phone = varchar("phone", 20)
    val email = varchar("email", 100).nullable()
    val leadSource = varchar("source", 50)
    val eventType = varchar("event_type", 50)
    val eventDate = date("event_date").nullable()
    val location = varchar("location", 200).nullable()
    val status = varchar("status", 50).default("NEW")
    val lostReason = varchar("lost_reason", 500).nullable()
    val notes = text("notes").nullable()
    val addedBy = uuid("added_by").references(UsersTable.id)
    val assignedTo = uuid("assigned_to").references(UsersTable.id)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}