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

    fun init() {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: error("DATABASE_URL environment variable not set")

        val config = HikariConfig().apply {
            jdbcUrl = dbUrl
            driverClassName = "org.postgresql.Driver"
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