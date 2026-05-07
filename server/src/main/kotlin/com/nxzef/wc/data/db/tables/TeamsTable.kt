package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TeamsTable : Table("teams") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 200)
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val inviteCode = varchar("invite_code", 8).uniqueIndex()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
