package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.MonthlyGoalsTable
import com.nxzef.wc.shared.model.MonthlyGoal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class MonthlyGoalRepository {

    private fun rowToGoal(row: ResultRow): MonthlyGoal = MonthlyGoal(
        id            = row[MonthlyGoalsTable.id].toString(),
        year          = row[MonthlyGoalsTable.year],
        month         = row[MonthlyGoalsTable.month],
        targetRevenue = row[MonthlyGoalsTable.targetRevenue].toDouble(),
        targetProfit  = row[MonthlyGoalsTable.targetProfit].toDouble()
    )

    fun getByMonthYear(month: Int, year: Int): MonthlyGoal? {
        return transaction {
            MonthlyGoalsTable.selectAll()
                .where {
                    (MonthlyGoalsTable.month eq month) and (MonthlyGoalsTable.year eq year)
                }
                .singleOrNull()
                ?.let { rowToGoal(it) }
        }
    }

    fun upsert(month: Int, year: Int, targetRevenue: Double, targetProfit: Double): MonthlyGoal {
        return transaction {
            val existing = MonthlyGoalsTable.selectAll()
                .where { (MonthlyGoalsTable.month eq month) and (MonthlyGoalsTable.year eq year) }
                .singleOrNull()

            if (existing != null) {
                MonthlyGoalsTable.update({
                    (MonthlyGoalsTable.month eq month) and (MonthlyGoalsTable.year eq year)
                }) {
                    it[MonthlyGoalsTable.targetRevenue] = targetRevenue.toBigDecimal()
                    it[MonthlyGoalsTable.targetProfit]  = targetProfit.toBigDecimal()
                }
            } else {
                MonthlyGoalsTable.insert {
                    it[MonthlyGoalsTable.year]          = year
                    it[MonthlyGoalsTable.month]         = month
                    it[MonthlyGoalsTable.targetRevenue] = targetRevenue.toBigDecimal()
                    it[MonthlyGoalsTable.targetProfit]  = targetProfit.toBigDecimal()
                }
            }

            MonthlyGoalsTable.selectAll()
                .where { (MonthlyGoalsTable.month eq month) and (MonthlyGoalsTable.year eq year) }
                .single()
                .let { rowToGoal(it) }
        }
    }
}
