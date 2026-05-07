package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.BookingsTable
import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.data.db.tables.LeadsTable
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class InvoiceWithClient(
    val invoice: Invoice,
    val clientName: String,
    val clientEmail: String?,
    val eventType: String,
    val eventDate: String
)

class InvoiceRepository {

    private fun rowToInvoice(row: ResultRow): Invoice {
        val total = row.getOrNull(InvoicesTable.totalAmount)?.toDouble() ?: 0.0
        val deposit = row.getOrNull(InvoicesTable.depositAmount)?.toDouble() ?: 0.0
        val depositPaid = row.getOrNull(InvoicesTable.depositPaid) ?: false
        val finalPaid = row.getOrNull(InvoicesTable.finalPaid) ?: false

        val remaining = when {
            finalPaid -> 0.0
            depositPaid -> total - deposit
            else -> total
        }

        return Invoice(
            id = row[InvoicesTable.id].toString(),
            bookingId = row[InvoicesTable.bookingId].toString(),
            totalAmount = total,
            depositAmount = deposit,
            depositPaid = depositPaid,
            depositPaidDate = row.getOrNull(InvoicesTable.depositPaidDate)?.toString(),
            finalPaid = finalPaid,
            finalPaidDate = row.getOrNull(InvoicesTable.finalPaidDate)?.toString(),
            remainingAmount = remaining,
            notes = row.getOrNull(InvoicesTable.notes),
            createdAt = row.getOrNull(InvoicesTable.createdAt)?.toString() ?: ""
        )
    }

    fun getById(id: String, teamId: String): Invoice? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            InvoicesTable
                .selectAll()
                .where { (InvoicesTable.id eq UUID.fromString(id)) and (InvoicesTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { rowToInvoice(it) }
        }
    }

    fun getByBookingId(bookingId: String, teamId: String): Invoice? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            InvoicesTable
                .selectAll()
                .where {
                    (InvoicesTable.bookingId eq UUID.fromString(bookingId)) and
                            (InvoicesTable.teamId eq tUuid)
                }
                .singleOrNull()
                ?.let { rowToInvoice(it) }
        }
    }

    fun getAll(teamId: String): List<Invoice> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            InvoicesTable
                .selectAll()
                .where { InvoicesTable.teamId eq tUuid }
                .orderBy(InvoicesTable.createdAt, SortOrder.DESC)
                .map { rowToInvoice(it) }
        }
    }

    fun create(request: CreateInvoiceRequest, teamId: String): Invoice {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val newId = UUID.randomUUID()
            InvoicesTable.insert {
                it[id] = newId
                it[bookingId] = UUID.fromString(request.bookingId)
                it[totalAmount] = request.totalAmount.toBigDecimal()
                it[depositAmount] = request.depositAmount.toBigDecimal()
                it[depositPaid] = false
                it[finalPaid] = false
                it[notes] = request.notes
                it[InvoicesTable.teamId] = tUuid
                it[createdAt] = Instant.now()
            }

            InvoicesTable
                .selectAll()
                .where { InvoicesTable.id eq newId }
                .single()
                .let { rowToInvoice(it) }
        }
    }

    /**
     * Joins invoices → bookings → leads to fetch the data needed for receipt emails
     * (client name + email + event metadata) in a single query.
     */
    fun getInvoiceWithClientDetails(invoiceId: String, teamId: String): InvoiceWithClient? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        val invUuid = try { UUID.fromString(invoiceId) } catch (_: Exception) { return null }
        return transaction {
            InvoicesTable
                .join(BookingsTable, JoinType.INNER, additionalConstraint = { InvoicesTable.bookingId eq BookingsTable.id })
                .join(LeadsTable, JoinType.INNER, additionalConstraint = { BookingsTable.leadId eq LeadsTable.id })
                .selectAll()
                .where { (InvoicesTable.id eq invUuid) and (InvoicesTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { row ->
                    InvoiceWithClient(
                        invoice    = rowToInvoice(row),
                        clientName = row[LeadsTable.fullName],
                        clientEmail = row[LeadsTable.email],
                        eventType  = row[BookingsTable.eventType],
                        eventDate  = row[BookingsTable.eventDate].toString()
                    )
                }
        }
    }

    fun updatePayment(
        id: String,
        request: UpdatePaymentRequest,
        teamId: String
    ): Invoice? {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            InvoicesTable.update(
                { (InvoicesTable.id eq UUID.fromString(id)) and (InvoicesTable.teamId eq tUuid) }
            ) {
                request.depositPaid?.let { value -> it[depositPaid] = value }
                request.depositPaidDate?.let { value -> it[depositPaidDate] = LocalDate.parse(value) }
                request.finalPaid?.let { value -> it[finalPaid] = value }
                request.finalPaidDate?.let { value -> it[finalPaidDate] = LocalDate.parse(value) }
                request.notes?.let { value -> it[notes] = value }
            }
            getById(id, teamId)
        }
    }
}
