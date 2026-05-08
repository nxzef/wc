package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object PasswordResetTable : Table("password_reset_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", 6)
    val expiresAt = timestamp("expires_at")
    val used = bool("used").default(false)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
