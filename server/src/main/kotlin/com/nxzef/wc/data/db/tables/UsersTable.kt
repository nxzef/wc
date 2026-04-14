package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 200)
    val role = varchar("role", 50)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}