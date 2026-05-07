package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.UsersTable
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class UserRepository {

    private fun rowToUser(row: org.jetbrains.exposed.sql.ResultRow): User = User(
        id = row[UsersTable.id].toString(),
        name = row[UsersTable.name],
        email = row[UsersTable.email],
        role = UserRole.valueOf(row[UsersTable.role]),
        isActive = row[UsersTable.isActive],
        teamId = row[UsersTable.teamId]?.toString()
    )

    fun getTeamMembers(teamId: String): List<User> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.teamId eq tUuid }
                .map { rowToUser(it) }
        }
    }

    fun createUser(
        name: String,
        email: String,
        passwordHash: String?,
        role: String,
        teamId: String?
    ): User {
        return transaction {
            val id = UsersTable.insert {
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.role] = role
                it[UsersTable.isActive] = true
                it[UsersTable.teamId] = teamId?.let { tid -> UUID.fromString(tid) }
                it[UsersTable.createdAt] = Instant.now()
            } get UsersTable.id

            User(
                id = id.toString(),
                name = name,
                email = email,
                role = UserRole.valueOf(role),
                isActive = true,
                teamId = teamId
            )
        }
    }

    fun deleteUser(id: String, teamId: String? = null): Boolean {
        return transaction {
            val uuid = try {
                UUID.fromString(id)
            } catch (_: Exception) {
                return@transaction false
            }
            val condition = if (teamId != null) {
                val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return@transaction false }
                (UsersTable.id eq uuid) and (UsersTable.teamId eq tUuid)
            } else {
                UsersTable.id eq uuid
            }

            val exists = UsersTable
                .selectAll()
                .where { condition }
                .count() > 0

            if (!exists) return@transaction false

            UsersTable.deleteWhere { condition }
            true
        }
    }

    fun findById(id: String): User? = transaction {
        val uuid = try { UUID.fromString(id) } catch (_: Exception) { return@transaction null }
        UsersTable
            .selectAll()
            .where { UsersTable.id eq uuid }
            .singleOrNull()
            ?.let { rowToUser(it) }
    }

    /** Login lookup — match by email regardless of team. Returned hash may be null when the user has not joined yet. */
    fun findByEmail(email: String): Pair<User, String?>? {
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?.let { row ->
                    Pair(rowToUser(row), row[UsersTable.passwordHash])
                }
        }
    }

    /** Join-team lookup — match by email AND a specific team. Hash may be null when the user hasn't joined yet. */
    fun findByEmailInTeam(email: String, teamId: String): Pair<User, String?>? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            UsersTable
                .selectAll()
                .where { (UsersTable.email eq email) and (UsersTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { row ->
                    Pair(rowToUser(row), row[UsersTable.passwordHash])
                }
        }
    }

    fun emailExists(email: String): Boolean = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }.any()
    }

    fun assignTeam(userId: String, teamId: String) {
        transaction {
            UsersTable.update({ UsersTable.id eq UUID.fromString(userId) }) {
                it[UsersTable.teamId] = UUID.fromString(teamId)
            }
        }
    }

    fun updatePassword(userId: String, newPasswordHash: String): Boolean {
        return transaction {
            val uuid = try {
                UUID.fromString(userId)
            } catch (_: Exception) {
                return@transaction false
            }
            UsersTable.update({ UsersTable.id eq uuid }) {
                it[passwordHash] = newPasswordHash
            } > 0
        }
    }

    fun getPasswordHash(userId: String): String? {
        return transaction {
            val uuid = try {
                UUID.fromString(userId)
            } catch (_: Exception) {
                return@transaction null
            }
            UsersTable
                .selectAll()
                .where { UsersTable.id eq uuid }
                .singleOrNull()
                ?.get(UsersTable.passwordHash)
        }
    }
}
