package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.InvoicesTable
import com.nxzef.wc.shared.model.CreateInvoiceRequest
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate

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

    fun getByBookingId(bookingId: String): Invoice? {
        return transaction {
            InvoicesTable
                .selectAll()
                .where {
                    InvoicesTable.bookingId eq
                            java.util.UUID.fromString(bookingId)
                }
                .singleOrNull()
                ?.let { rowToInvoice(it) }
        }
    }

    fun getAll(): List<Invoice> {
        return transaction {
            InvoicesTable
                .selectAll()
                .orderBy(InvoicesTable.createdAt, SortOrder.DESC)
                .map { rowToInvoice(it) }
        }
    }

    fun create(request: CreateInvoiceRequest): Invoice {
        return transaction {
            val newId = java.util.UUID.randomUUID()
            InvoicesTable.insert {
                it[id] = newId
                it[bookingId] = java.util.UUID.fromString(
                    request.bookingId
                )
                it[totalAmount] = request.totalAmount.toBigDecimal()
                it[depositAmount] = request.depositAmount.toBigDecimal()
                it[depositPaid] = false
                it[finalPaid] = false
                it[notes] = request.notes
                it[createdAt] = Instant.now()
            }

            InvoicesTable
                .selectAll()
                .where { InvoicesTable.id eq newId }
                .single()
                .let { rowToInvoice(it) }
        }
    }

    fun updatePayment(
        id: String,
        request: UpdatePaymentRequest
    ): Invoice? {
        return transaction {
            InvoicesTable.update(
                { InvoicesTable.id eq java.util.UUID.fromString(id) }
            ) {
                request.depositPaid?.let { value ->
                    it[depositPaid] = value
                }
                request.depositPaidDate?.let { value ->
                    it[depositPaidDate] = LocalDate.parse(value)
                }
                request.finalPaid?.let { value ->
                    it[finalPaid] = value
                }
                request.finalPaidDate?.let { value ->
                    it[finalPaidDate] = LocalDate.parse(value)
                }
                request.notes?.let { value ->
                    it[notes] = value
                }
            }

            InvoicesTable
                .selectAll()
                .where {
                    InvoicesTable.id eq
                            java.util.UUID.fromString(id)
                }
                .singleOrNull()
                ?.let { rowToInvoice(it) }
        }
    }
}