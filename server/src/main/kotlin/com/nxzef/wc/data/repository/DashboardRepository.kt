package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.data.db.tables.MonthlyGoalsTable
import com.nxzef.wc.data.db.tables.ProjectExpensesTable
import com.nxzef.wc.data.db.tables.ReceiptsTable
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.MonthlyGoal
import com.nxzef.wc.shared.model.ProjectPnL
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class DashboardRepository {

    fun getStats(teamId: String): DashboardStats {
        val tUuid = UUID.fromString(teamId)
        return transaction {

            val now = LocalDate.now()
            val monthStart = now.withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
            val monthEnd = Instant.now()

            val totalLeadsThisMonth = LeadsTable
                .selectAll()
                .where {
                    (LeadsTable.teamId eq tUuid) and
                            (LeadsTable.createdAt greaterEq monthStart) and
                            (LeadsTable.createdAt lessEq monthEnd)
                }
                .count().toInt()

            val openLeads = LeadsTable
                .selectAll()
                .where { LeadsTable.teamId eq tUuid }
                .count().toInt()

            val totalBookingsThisMonth = BookingsTable
                .selectAll()
                .where {
                    (BookingsTable.teamId eq tUuid) and
                            (BookingsTable.createdAt greaterEq monthStart) and
                            (BookingsTable.createdAt lessEq monthEnd)
                }
                .count().toInt()

            val pendingDeliveries = BookingsTable
                .selectAll()
                .where {
                    (BookingsTable.teamId eq tUuid) and
                            (BookingsTable.status inList listOf(
                                BookingStatus.BOOKED.name,
                                BookingStatus.SHOOT_DONE.name,
                                BookingStatus.EDITING.name
                            ))
                }
                .count().toInt()

            val invoicesThisMonth = InvoicesTable
                .selectAll()
                .where { InvoicesTable.teamId eq tUuid }
                .toList()
            val totalRevenueThisMonth = invoicesThisMonth
                .filter { row ->
                    val createdAt = row[InvoicesTable.createdAt]
                    createdAt in monthStart..monthEnd
                }
                .sumOf { it[InvoicesTable.totalAmount].toDouble() }

            val pendingPayments = InvoicesTable
                .selectAll()
                .where { (InvoicesTable.teamId eq tUuid) and (InvoicesTable.finalPaid eq false) }
                .toList()
                .sumOf { row ->
                    val total = row[InvoicesTable.totalAmount].toDouble()
                    val deposit = row[InvoicesTable.depositAmount].toDouble()
                    val depPaid = row[InvoicesTable.depositPaid]
                    if (depPaid) total - deposit else total
                }

            val leadsBySource = LeadsTable
                .selectAll()
                .where { LeadsTable.teamId eq tUuid }
                .groupBy { it[LeadsTable.leadSource] }
                .mapValues { it.value.size }

            val recentLeads = LeadsTable
                .selectAll()
                .where { LeadsTable.teamId eq tUuid }
                .orderBy(LeadsTable.createdAt, SortOrder.DESC)
                .limit(5)
                .map { row ->
                    val customStatusId = row[LeadsTable.customStatusId]
                    val customStatus = customStatusId?.let { sid ->
                        LeadStatusesTable.selectAll()
                            .where { LeadStatusesTable.id eq sid }
                            .singleOrNull()
                            ?.let {
                                LeadStatus(
                                    id = it[LeadStatusesTable.id].toString(),
                                    name = it[LeadStatusesTable.name],
                                    color = it[LeadStatusesTable.color],
                                    isDefault = it[LeadStatusesTable.isDefault]
                                )
                            }
                    }
                    val statusName = customStatus?.name ?: row[LeadsTable.status]

                    Lead(
                        id = row[LeadsTable.id].toString(),
                        fullName = row[LeadsTable.fullName],
                        phone = row[LeadsTable.phone],
                        email = row[LeadsTable.email],
                        source = LeadSource.valueOf(row[LeadsTable.leadSource]),
                        eventType = EventType.valueOf(row[LeadsTable.eventType]),
                        eventDate = row[LeadsTable.eventDate]?.toString(),
                        location = row[LeadsTable.location],
                        statusName = statusName,
                        customStatus = customStatus,
                        lostReason = row[LeadsTable.lostReason],
                        notes = row[LeadsTable.notes],
                        addedBy = row[LeadsTable.addedBy].toString(),
                        assignedTo = row[LeadsTable.assignedTo].toString(),
                        createdAt = row[LeadsTable.createdAt].toString()
                    )
                }

            val upcomingBookings = BookingsTable
                .selectAll()
                .where {
                    (BookingsTable.teamId eq tUuid) and
                            (BookingsTable.eventDate greaterEq now) and
                            (BookingsTable.status neq BookingStatus.CLOSED.name)
                }
                .orderBy(BookingsTable.eventDate, SortOrder.ASC)
                .limit(5)
                .map { row ->
                    Booking(
                        id = row[BookingsTable.id].toString(),
                        leadId = row[BookingsTable.leadId].toString(),
                        quoteId = row[BookingsTable.quoteId].toString(),
                        photographerId = row[BookingsTable.photographerId]?.toString(),
                        editorId = row[BookingsTable.editorId]?.toString(),
                        eventDate = row[BookingsTable.eventDate].toString(),
                        eventType = row[BookingsTable.eventType],
                        location = row[BookingsTable.location],
                        status = BookingStatus.valueOf(row[BookingsTable.status]),
                        notes = row[BookingsTable.notes],
                        createdAt = row[BookingsTable.createdAt].toString()
                    )
                }

            val teamInvoices = InvoicesTable.selectAll().where { InvoicesTable.teamId eq tUuid }.toList()
            val totalInvoices = teamInvoices.size.toLong()
            val totalRevenue = teamInvoices.sumOf { it[InvoicesTable.totalAmount].toDouble() }
            val avgOrderValue = if (totalInvoices > 0) totalRevenue / totalInvoices else 0.0
            val totalCollected = ReceiptsTable.selectAll()
                .where { ReceiptsTable.teamId eq tUuid }
                .sumOf { it[ReceiptsTable.amount].toDouble() }
            val totalPending = totalRevenue - totalCollected

            // Calculate Conversion Rate
            val allLeads = LeadsTable.selectAll().where { LeadsTable.teamId eq tUuid }.count()
            val wonLeads = LeadsTable.selectAll().where { 
                (LeadsTable.teamId eq tUuid) and (LeadsTable.status eq "WON") 
            }.count()
            val conversionRate = if (allLeads > 0) (wonLeads.toDouble() / allLeads.toDouble()) * 100.0 else 0.0

            // Calculate Revenue Trend (Daily for current month)
            val daysInMonth = now.lengthOfMonth()
            val dailyRevenue = DoubleArray(daysInMonth) { 0.0 }
            
            invoicesThisMonth.forEach { row ->
                val date = row[InvoicesTable.createdAt].atZone(ZoneOffset.UTC).toLocalDate()
                if (date.monthValue == now.monthValue && date.year == now.year) {
                    dailyRevenue[date.dayOfMonth - 1] += row[InvoicesTable.totalAmount].toDouble()
                }
            }
            
            val revenueTrend = dailyRevenue.toList()

            // Calculate Project P&L
            val projectPnLList = transaction {
                BookingsTable
                    .selectAll()
                    .where { BookingsTable.teamId eq tUuid }
                    .orderBy(BookingsTable.eventDate, SortOrder.DESC)
                    .limit(20)
                    .map { bookingRow ->
                        val bId = bookingRow[BookingsTable.id]
                        val revenue = InvoicesTable.selectAll()
                            .where { InvoicesTable.bookingId eq bId }
                            .sumOf { it[InvoicesTable.totalAmount].toDouble() }

                        val expenses = ProjectExpensesTable.selectAll()
                            .where { ProjectExpensesTable.bookingId eq bId }
                            .sumOf { it[ProjectExpensesTable.actualAmount].toDouble() }

                        val netProfit = revenue - expenses
                        val marginPercent = if (revenue > 0) (netProfit / revenue) * 100.0 else 0.0

                        ProjectPnL(
                            bookingId = bId.toString(),
                            eventType = bookingRow[BookingsTable.eventType],
                            eventDate = bookingRow[BookingsTable.eventDate].toString(),
                            revenue = revenue,
                            totalExpenses = expenses,
                            netProfit = netProfit,
                            marginPercent = marginPercent
                        )
                    }
            }

            // Current Month Goal and Actuals
            val currentMonthGoalRow = MonthlyGoalsTable.selectAll()
                .where { (MonthlyGoalsTable.month eq now.monthValue) and (MonthlyGoalsTable.year eq now.year) }
                .singleOrNull()

            val currentMonthGoal = currentMonthGoalRow?.let {
                MonthlyGoal(
                    id = it[MonthlyGoalsTable.id].toString(),
                    year = it[MonthlyGoalsTable.year],
                    month = it[MonthlyGoalsTable.month],
                    targetRevenue = it[MonthlyGoalsTable.targetRevenue].toDouble(),
                    targetProfit = it[MonthlyGoalsTable.targetProfit].toDouble()
                )
            }

            val currentMonthExpenses = ProjectExpensesTable.selectAll()
                .where { (ProjectExpensesTable.teamId eq tUuid) and (ProjectExpensesTable.expenseDate greaterEq now.withDayOfMonth(1)) and (ProjectExpensesTable.expenseDate lessEq now) }
                .sumOf { it[ProjectExpensesTable.actualAmount].toDouble() }
            
            val currentMonthActualProfit = totalRevenueThisMonth - currentMonthExpenses
            val isMonthBelowTarget = currentMonthGoal?.let { currentMonthActualProfit < it.targetProfit } ?: false

            // Previous Month Stats for Trends
            val prevMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            val prevMonthEnd = now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            
            val prevMonthInvoices = InvoicesTable.selectAll()
                .where { (InvoicesTable.teamId eq tUuid) and (InvoicesTable.createdAt greaterEq prevMonthStart) and (InvoicesTable.createdAt less prevMonthEnd) }
                .toList()
            val prevMonthRevenue = prevMonthInvoices.sumOf { it[InvoicesTable.totalAmount].toDouble() }
            val prevMonthAvgOrderValue = if (prevMonthInvoices.isNotEmpty()) prevMonthRevenue / prevMonthInvoices.size else 0.0
            
            val prevMonthLeads = LeadsTable.selectAll()
                .where { (LeadsTable.teamId eq tUuid) and (LeadsTable.createdAt greaterEq prevMonthStart) and (LeadsTable.createdAt less prevMonthEnd) }
                .count()
            val prevMonthWonLeads = LeadsTable.selectAll()
                .where { (LeadsTable.teamId eq tUuid) and (LeadsTable.status eq "WON") and (LeadsTable.createdAt greaterEq prevMonthStart) and (LeadsTable.createdAt less prevMonthEnd) }
                .count()
            val prevMonthConversionRate = if (prevMonthLeads > 0) (prevMonthWonLeads.toDouble() / prevMonthLeads.toDouble()) * 100.0 else 0.0

            val revenueTrendPercentage = if (prevMonthRevenue > 0) ((totalRevenueThisMonth - prevMonthRevenue) / prevMonthRevenue) * 100.0 else 0.0
            val conversionRateTrendPercentage = if (prevMonthConversionRate > 0) conversionRate - prevMonthConversionRate else 0.0
            val averageOrderValueTrendPercentage = if (prevMonthAvgOrderValue > 0) ((avgOrderValue - prevMonthAvgOrderValue) / prevMonthAvgOrderValue) * 100.0 else 0.0

            DashboardStats(
                totalLeadsThisMonth = totalLeadsThisMonth,
                totalBookingsThisMonth = totalBookingsThisMonth,
                totalRevenueThisMonth = totalRevenueThisMonth,
                averageOrderValue = avgOrderValue,
                conversionRate = conversionRate,
                revenueTrend = revenueTrend,
                pendingPayments = pendingPayments,
                openLeads = openLeads,
                pendingDeliveries = pendingDeliveries,
                leadsBySource = leadsBySource,
                recentLeads = recentLeads,
                upcomingBookings = upcomingBookings,
                totalRevenue = totalRevenue,
                totalCollected = totalCollected,
                totalPending = totalPending,
                projectPnLList = projectPnLList,
                currentMonthGoal = currentMonthGoal,
                currentMonthActualProfit = currentMonthActualProfit,
                isMonthBelowTarget = isMonthBelowTarget,
                revenueTrendPercentage = revenueTrendPercentage,
                conversionRateTrendPercentage = conversionRateTrendPercentage,
                averageOrderValueTrendPercentage = averageOrderValueTrendPercentage
            )
        }
    }
}
