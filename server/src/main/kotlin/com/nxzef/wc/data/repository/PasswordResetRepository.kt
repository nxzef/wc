package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.PasswordResetTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class PasswordResetRepository {

    fun createToken(userId: String, token: String, expiresAt: Instant) {
        transaction {
            PasswordResetTable.insert {
                it[PasswordResetTable.userId] = UUID.fromString(userId)
                it[PasswordResetTable.token] = token
                it[PasswordResetTable.expiresAt] = expiresAt
                it[PasswordResetTable.createdAt] = Instant.now()
            }
        }
    }

    fun findValidToken(userId: String, token: String): UUID? {
        val uUuid = try { UUID.fromString(userId) } catch (_: Exception) { return null }
        return transaction {
            PasswordResetTable
                .selectAll()
                .where {
                    (PasswordResetTable.userId eq uUuid) and
                    (PasswordResetTable.token eq token) and
                    (PasswordResetTable.used eq false) and
                    (PasswordResetTable.expiresAt greater Instant.now())
                }
                .singleOrNull()
                ?.get(PasswordResetTable.id)
        }
    }

    fun markAsUsed(id: UUID) {
        transaction {
            PasswordResetTable.update({ PasswordResetTable.id eq id }) {
                it[used] = true
            }
        }
    }
}
