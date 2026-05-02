package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object LeadStatusesTable : Table("lead_statuses") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val color = varchar("color", 20).default("#2196F3")
    val position = integer("position").default(0)
    val isDefault = bool("is_default").default(false)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
