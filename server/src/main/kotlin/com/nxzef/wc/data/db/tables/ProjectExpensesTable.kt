package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object ProjectExpensesTable : Table("project_expenses") {
    val id            = uuid("id").autoGenerate()
    val bookingId     = uuid("booking_id").references(BookingsTable.id)
    val category      = varchar("category", 100)
    val description   = varchar("description", 255).nullable()
    val estimatedAmount = decimal("estimated_amount", precision = 12, scale = 2).default(0.toBigDecimal())
    val actualAmount  = decimal("actual_amount", precision = 12, scale = 2)
    val expenseDate   = date("expense_date")
    val addedBy       = uuid("added_by").references(UsersTable.id).nullable()
    val paymentMethod = varchar("payment_method", 50).nullable()
    val notes         = text("notes").nullable()
    val createdAt     = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
