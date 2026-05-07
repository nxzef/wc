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

            val allBookings = BookingsTable.selectAll().where { BookingsTable.teamId eq tUuid }.toList()
            val invoiceByBooking = teamInvoices
                .associate { it[InvoicesTable.bookingId].toString() to it[InvoicesTable.totalAmount].toDouble() }
            val expenseSumByBooking = ProjectExpensesTable
                .selectAll()
                .where { ProjectExpensesTable.teamId eq tUuid }
                .groupBy { it[ProjectExpensesTable.bookingId].toString() }
                .mapValues { (_, rows) -> rows.sumOf { it[ProjectExpensesTable.actualAmount].toDouble() } }

            val projectPnLList = allBookings.mapNotNull { row ->
                val bid = row[BookingsTable.id].toString()
                val revenue = invoiceByBooking[bid] ?: return@mapNotNull null
                val expenses = expenseSumByBooking[bid] ?: 0.0
                val netProfit = revenue - expenses
                val margin = if (revenue > 0) (netProfit / revenue) * 100 else 0.0
                ProjectPnL(
                    bookingId = bid,
                    eventType = row[BookingsTable.eventType],
                    eventDate = row[BookingsTable.eventDate].toString(),
                    revenue = revenue,
                    totalExpenses = expenses,
                    netProfit = netProfit,
                    marginPercent = margin
                )
            }

            val currentMonth = now.monthValue
            val currentYear  = now.year
            val currentMonthGoal = MonthlyGoalsTable.selectAll()
                .where { (MonthlyGoalsTable.month eq currentMonth) and (MonthlyGoalsTable.year eq currentYear) }
                .singleOrNull()
                ?.let { r ->
                    MonthlyGoal(
                        id = r[MonthlyGoalsTable.id].toString(),
                        year = r[MonthlyGoalsTable.year],
                        month = r[MonthlyGoalsTable.month],
                        targetRevenue = r[MonthlyGoalsTable.targetRevenue].toDouble(),
                        targetProfit = r[MonthlyGoalsTable.targetProfit].toDouble()
                    )
                }

            val currentMonthActualProfit = projectPnLList
                .filter {
                    try {
                        val date = LocalDate.parse(it.eventDate)
                        date.monthValue == currentMonth && date.year == currentYear
                    } catch (e: Exception) { false }
                }
                .sumOf { it.netProfit }

            val isMonthBelowTarget = currentMonthGoal != null &&
                currentMonthActualProfit < currentMonthGoal.targetProfit

            DashboardStats(
                totalLeadsThisMonth = totalLeadsThisMonth,
                totalBookingsThisMonth = totalBookingsThisMonth,
                totalRevenueThisMonth = totalRevenueThisMonth,
                averageOrderValue = avgOrderValue,
                conversionRate = 0.0,
                revenueTrend = listOf(0.2, 0.4, 0.35, 0.6, 0.55, 0.85, 0.75, 0.95),
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
                isMonthBelowTarget = isMonthBelowTarget
            )
        }
    }
}
