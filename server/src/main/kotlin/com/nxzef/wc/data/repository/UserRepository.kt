package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.UsersTable
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class UserRepository {

    fun getAllUsers(): List<User> {
        return transaction {
            UsersTable
                .selectAll()
                .map { row ->
                    User(
                        id = row[UsersTable.id].toString(),
                        name = row[UsersTable.name],
                        email = row[UsersTable.email],
                        role = UserRole.valueOf(row[UsersTable.role]),
                        isActive = row[UsersTable.isActive]
                    )
                }
        }
    }

    fun createUser(
        name: String,
        email: String,
        passwordHash: String,
        role: String
    ): User {
        return transaction {
            val id = UsersTable.insert {
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.role] = role
                it[UsersTable.isActive] = true
                it[UsersTable.createdAt] = Instant.now()
            } get UsersTable.id

            User(
                id = id.toString(),
                name = name,
                email = email,
                role = UserRole.valueOf(role),
                isActive = true
            )
        }
    }

    fun deleteUser(id: String): Boolean {
        return transaction {
            val uuid = try {
                java.util.UUID.fromString(id)
            } catch (_: Exception) {
                return@transaction false
            }
            // Check if user exists
            val exists = UsersTable
                .selectAll()
                .where { UsersTable.id eq uuid }
                .count() > 0

            if (!exists) return@transaction false

            // Correct way to delete in Exposed
            UsersTable.deleteWhere { UsersTable.id eq uuid }
            true
        }
    }

    fun findByEmail(email: String): Pair<User, String>? {
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?.let { row ->
                    val user = User(
                        id = row[UsersTable.id].toString(),
                        name = row[UsersTable.name],
                        email = row[UsersTable.email],
                        role = UserRole.valueOf(row[UsersTable.role]),
                        isActive = row[UsersTable.isActive]
                    )
                    Pair(user, row[UsersTable.passwordHash])
                }
        }
    }

    fun seedOwner() {
        transaction {
            val exists = UsersTable.selectAll().count() > 0
            if (!exists) {
                val ownerEmail = System.getenv("OWNER_EMAIL")
                val ownerPassword = System.getenv("OWNER_PASSWORD")
                val ownerName = System.getenv("OWNER_NAME") ?: "Owner"

                if (ownerEmail.isNullOrBlank() || ownerPassword.isNullOrBlank()) {
                    println("⚠️  WARNING: OWNER_EMAIL or OWNER_PASSWORD not set — owner account NOT seeded.")
                    println("⚠️  Set OWNER_EMAIL and OWNER_PASSWORD in .env before first run.")
                    return@transaction
                }

                UsersTable.insert {
                    it[name] = ownerName
                    it[email] = ownerEmail
                    it[passwordHash] = BCrypt.hashpw(ownerPassword, BCrypt.gensalt())
                    it[role] = UserRole.OWNER.name
                    it[isActive] = true
                    it[createdAt] = Instant.now()
                }
                println("✅ Owner account seeded: $ownerEmail")
            }
        }
    }

    fun updatePassword(userId: String, newPasswordHash: String): Boolean {
        return transaction {
            val uuid = try {
                java.util.UUID.fromString(userId)
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
                java.util.UUID.fromString(userId)
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