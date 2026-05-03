package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table

object MonthlyGoalsTable : Table("monthly_goals") {
    val id            = uuid("id").autoGenerate()
    val year          = integer("year")
    val month         = integer("month")
    val targetRevenue = decimal("target_revenue", precision = 12, scale = 2).default(0.toBigDecimal())
    val targetProfit  = decimal("target_profit", precision = 12, scale = 2).default(0.toBigDecimal())

    override val primaryKey = PrimaryKey(id)
}
