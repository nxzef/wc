package com.nxzef.wc.data.db

import com.nxzef.wc.data.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: error("DATABASE_URL environment variable not set")

        val config = HikariConfig().apply {
            jdbcUrl         = dbUrl
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit    = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTimeout = 60000
            validate()
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.create(UsersTable)
        }

        println("✅ Database connected and tables created")
    }
}