package com.nxzef.wc.data.db

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.data.db.tables.NotificationsTable
import com.nxzef.wc.data.db.tables.QuoteItemsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.data.db.tables.TasksTable
import com.nxzef.wc.data.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private lateinit var currentDbUrl: String

    fun init(jdbcUrl: String? = null) {
        currentDbUrl = jdbcUrl ?: System.getenv("DATABASE_URL")
            ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

        val isH2 = currentDbUrl.startsWith("jdbc:h2:")

        val config = HikariConfig().apply {
            this.jdbcUrl = currentDbUrl
            driverClassName = if (isH2) "org.h2.Driver" else "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTimeout = 60000
            validate()
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.create(
                UsersTable,
                LeadsTable,
                QuotesTable,
                QuoteItemsTable,
                BookingsTable,
                InvoicesTable,
                TasksTable,
                NotificationsTable
            )
        }

        println("\uD83D\uDFE2 Database connected and tables created")
    }
}