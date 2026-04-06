package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.UsersTable
import com.nxzef.wc.domain.model.User
import com.nxzef.wc.domain.model.UserRole
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class UserRepository {

    fun findByEmail(email: String): Pair<User, String>? {
        return transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?.let { row ->
                    val user = User(
                        id       = row[UsersTable.id].toString(),
                        name     = row[UsersTable.name],
                        email    = row[UsersTable.email],
                        role     = UserRole.valueOf(row[UsersTable.role]),
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
                UsersTable.insert {
                    it[name]         = "Niyas"
                    it[email]        = "niyas@weddingclouds.com"
                    it[passwordHash] = BCrypt.hashpw("changeme123", BCrypt.gensalt())
                    it[role]         = UserRole.OWNER.name
                    it[isActive]     = true
                    it[createdAt]    = Instant.now()
                }
                println("✅ Owner account seeded")
            }
        }
    }
}