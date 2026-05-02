package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.data.db.tables.LeadStatusesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DashboardRepository {

    fun getStats(): DashboardStats {
        return transaction {

            val now = LocalDate.now()
            val monthStart = now.withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
            val monthEnd = Instant.now()

            val totalLeadsThisMonth = LeadsTable
                .selectAll()
                .where {
                    LeadsTable.createdAt greaterEq monthStart and
                            (LeadsTable.createdAt lessEq monthEnd)
                }
                .count().toInt()

            val openLeads = LeadsTable.selectAll().count().toInt()

            val totalBookingsThisMonth = BookingsTable
                .selectAll()
                .where {
                    BookingsTable.createdAt greaterEq monthStart and
                            (BookingsTable.createdAt lessEq monthEnd)
                }
                .count().toInt()

            val pendingDeliveries = BookingsTable
                .selectAll()
                .where {
                    BookingsTable.status inList listOf(
                        BookingStatus.BOOKED.name,
                        BookingStatus.SHOOT_DONE.name,
                        BookingStatus.EDITING.name
                    )
                }
                .count().toInt()

            val invoicesThisMonth = InvoicesTable.selectAll().toList()
            val totalRevenueThisMonth = invoicesThisMonth
                .filter { row ->
                    val createdAt = row[InvoicesTable.createdAt]
                    createdAt in monthStart..monthEnd
                }
                .sumOf { it[InvoicesTable.totalAmount].toDouble() }

            val pendingPayments = InvoicesTable
                .selectAll()
                .where { InvoicesTable.finalPaid eq false }
                .toList()
                .sumOf { row ->
                    val total = row[InvoicesTable.totalAmount].toDouble()
                    val deposit = row[InvoicesTable.depositAmount].toDouble()
                    val depPaid = row[InvoicesTable.depositPaid]
                    if (depPaid) total - deposit else total
                }

            val leadsBySource = LeadsTable
                .selectAll()
                .groupBy { it[LeadsTable.leadSource] }
                .mapValues { it.value.size }

            val recentLeads = LeadsTable
                .selectAll()
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
                    BookingsTable.eventDate greaterEq now and
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

            val totalInvoices = InvoicesTable.selectAll().count()
            val avgOrderValue = if (totalInvoices > 0)
                InvoicesTable.selectAll().sumOf { it[InvoicesTable.totalAmount].toDouble() } / totalInvoices
            else 0.0

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
                upcomingBookings = upcomingBookings
            )
        }
    }
}
