package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.ProjectExpensesTable
import com.nxzef.wc.shared.model.CreateProjectExpenseRequest
import com.nxzef.wc.shared.model.ProjectExpense
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ProjectExpenseRepository {

    private fun rowToExpense(row: ResultRow): ProjectExpense = ProjectExpense(
        id              = row[ProjectExpensesTable.id].toString(),
        bookingId       = row[ProjectExpensesTable.bookingId].toString(),
        category        = row[ProjectExpensesTable.category],
        description     = row[ProjectExpensesTable.description],
        estimatedAmount = row[ProjectExpensesTable.estimatedAmount].toDouble(),
        actualAmount    = row[ProjectExpensesTable.actualAmount].toDouble(),
        expenseDate     = row[ProjectExpensesTable.expenseDate].toString(),
        addedBy         = row[ProjectExpensesTable.addedBy]?.toString(),
        paymentMethod   = row[ProjectExpensesTable.paymentMethod],
        notes           = row[ProjectExpensesTable.notes],
        createdAt       = row[ProjectExpensesTable.createdAt].toString()
    )

    fun getByBookingId(bookingId: String): List<ProjectExpense> {
        return transaction {
            ProjectExpensesTable.selectAll()
                .where { ProjectExpensesTable.bookingId eq UUID.fromString(bookingId) }
                .orderBy(ProjectExpensesTable.expenseDate, SortOrder.DESC)
                .map { rowToExpense(it) }
        }
    }

    fun getTotalByBookingId(bookingId: String): Double {
        return transaction {
            ProjectExpensesTable
                .select(ProjectExpensesTable.actualAmount.sum())
                .where { ProjectExpensesTable.bookingId eq UUID.fromString(bookingId) }
                .singleOrNull()
                ?.get(ProjectExpensesTable.actualAmount.sum())
                ?.toDouble() ?: 0.0
        }
    }

    fun create(request: CreateProjectExpenseRequest, addedByUserId: String): ProjectExpense {
        return transaction {
            val newId = ProjectExpensesTable.insert {
                it[bookingId]       = UUID.fromString(request.bookingId)
                it[category]        = request.category
                it[description]     = request.description
                it[estimatedAmount] = request.estimatedAmount.toBigDecimal()
                it[actualAmount]    = request.actualAmount.toBigDecimal()
                it[expenseDate]     = LocalDate.parse(request.expenseDate)
                it[addedBy]         = UUID.fromString(addedByUserId)
                it[paymentMethod]   = request.paymentMethod
                it[notes]           = request.notes
                it[createdAt]       = Instant.now()
            } get ProjectExpensesTable.id

            ProjectExpensesTable.selectAll()
                .where { ProjectExpensesTable.id eq newId }
                .single()
                .let { rowToExpense(it) }
        }
    }

    fun delete(id: String) {
        transaction {
            ProjectExpensesTable.deleteWhere { ProjectExpensesTable.id eq UUID.fromString(id) }
        }
    }
}
