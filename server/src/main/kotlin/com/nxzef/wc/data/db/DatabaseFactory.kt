package com.nxzef.wc.data.db

import com.nxzef.wc.config.ServerConfig
import com.nxzef.wc.data.repository.LeadStatusRepository
import com.nxzef.wc.routes.AppConfigTable
import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.data.db.tables.MonthlyGoalsTable
import com.nxzef.wc.data.db.tables.NotificationsTable
import com.nxzef.wc.data.db.tables.ProjectExpensesTable
import com.nxzef.wc.data.db.tables.QuoteItemsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.data.db.tables.ReceiptsTable
import com.nxzef.wc.data.db.tables.RefreshTokensTable
import com.nxzef.wc.data.db.tables.TasksTable
import com.nxzef.wc.data.db.tables.TeamsTable
import com.nxzef.wc.data.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private var dataSource: HikariDataSource? = null

    fun init(jdbcUrl: String? = null) {
        val rawUrl = jdbcUrl ?: ServerConfig.databaseUrl
        
        // Close existing data source if we're re-initializing (common in tests)
        dataSource?.close()

        // PgBouncer (Supabase port 6543) runs in transaction mode which doesn't support
        // server-side prepared statements — disable them to avoid "already exists" errors.
        val currentDbUrl = if (rawUrl.startsWith("jdbc:postgresql") && !rawUrl.contains("prepareThreshold")) {
            rawUrl + (if (rawUrl.contains("?")) "&" else "?") + "prepareThreshold=0"
        } else rawUrl

        val isH2 = currentDbUrl.startsWith("jdbc:h2:")

        // For H2, we need to ensure it uses PostgreSQL compatibility mode properly
        val finalUrl = if (isH2 && !currentDbUrl.contains("MODE=PostgreSQL")) {
            if (currentDbUrl.contains(";")) "$currentDbUrl;MODE=PostgreSQL" else "$currentDbUrl;MODE=PostgreSQL"
        } else currentDbUrl

        val config = HikariConfig().apply {
            this.jdbcUrl = finalUrl
            driverClassName = if (isH2) "org.h2.Driver" else "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED" // More compatible for H2
            connectionTimeout = 30000
            validate()
        }

        val newDataSource = HikariDataSource(config)
        dataSource = newDataSource
        Database.connect(newDataSource)

        transaction {
            // Use createMissingTablesAndColumns to be idempotent.
            // Order: Create tables with non-nullable circular dependencies carefully.
            // Exposed handles circular foreign keys by creating tables first, then constraints.
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                TeamsTable,
                RefreshTokensTable,
                LeadStatusesTable,
                LeadsTable,
                QuotesTable,
                QuoteItemsTable,
                BookingsTable,
                InvoicesTable,
                ReceiptsTable,
                ProjectExpensesTable,
                MonthlyGoalsTable,
                TasksTable,
                NotificationsTable,
                AppConfigTable
            )
        }

        LeadStatusRepository().ensureTerminalStatuses()

        logger.info("🟢 Database connected to {} and tables verified", if (isH2) "H2 (in-memory)" else "PostgreSQL")
    }
}
