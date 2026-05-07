package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.RefreshTokensTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class RefreshTokenRepository {

    data class Record(val userId: String, val expiresAt: Instant)

    fun save(userId: String, token: String, expiresAt: Instant) {
        transaction {
            RefreshTokensTable.insert {
                it[RefreshTokensTable.userId]    = UUID.fromString(userId)
                it[RefreshTokensTable.token]     = token
                it[RefreshTokensTable.expiresAt] = expiresAt
                it[RefreshTokensTable.createdAt] = Instant.now()
            }
        }
    }

    fun find(token: String): Record? = transaction {
        RefreshTokensTable
            .selectAll()
            .where { RefreshTokensTable.token eq token }
            .singleOrNull()
            ?.let { Record(it[RefreshTokensTable.userId].toString(), it[RefreshTokensTable.expiresAt]) }
    }

    fun delete(token: String) = transaction {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.token eq token }
    }

    fun deleteAllForUser(userId: String) = transaction {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.userId eq UUID.fromString(userId) }
    }

    fun deleteExpired() = transaction {
        RefreshTokensTable.deleteWhere { expiresAt lessEq Instant.now() }
    }
}
