package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.TeamsTable
import com.nxzef.wc.shared.model.Team
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

class TeamRepository {

    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    private fun rowToTeam(row: ResultRow): Team = Team(
        id = row[TeamsTable.id].toString(),
        name = row[TeamsTable.name],
        ownerId = row[TeamsTable.ownerId].toString(),
        inviteCode = row[TeamsTable.inviteCode],
        isActive = row[TeamsTable.isActive],
        createdAt = row[TeamsTable.createdAt].toString()
    )

    private fun generateInviteCode(): String =
        (1..6).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")

    fun createTeam(ownerUserId: String, teamName: String): Team {
        return transaction {
            var code: String
            do {
                code = generateInviteCode()
            } while (
                TeamsTable.selectAll()
                    .where { TeamsTable.inviteCode eq code }
                    .any()
            )

            val newId = TeamsTable.insert {
                it[name] = teamName
                it[ownerId] = UUID.fromString(ownerUserId)
                it[inviteCode] = code
                it[isActive] = true
                it[createdAt] = Instant.now()
            } get TeamsTable.id

            TeamsTable.selectAll()
                .where { TeamsTable.id eq newId }
                .single()
                .let { rowToTeam(it) }
        }
    }

    fun getByInviteCode(code: String): Team? = transaction {
        TeamsTable.selectAll()
            .where { TeamsTable.inviteCode eq code.trim().uppercase() }
            .singleOrNull()
            ?.let { rowToTeam(it) }
    }

    fun getById(id: String): Team? = transaction {
        val uuid = try { UUID.fromString(id) } catch (_: Exception) { return@transaction null }
        TeamsTable.selectAll()
            .where { TeamsTable.id eq uuid }
            .singleOrNull()
            ?.let { rowToTeam(it) }
    }
}
