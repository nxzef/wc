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
        val total = row[InvoicesTable.totalAmount].toDouble()
        val deposit = row[InvoicesTable.depositAmount].toDouble()
        val depositPaid = row[InvoicesTable.depositPaid]
        val finalPaid = row[InvoicesTable.finalPaid]

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
            depositPaidDate = row[InvoicesTable.depositPaidDate]?.toString(),
            finalPaid = finalPaid,
            finalPaidDate = row[InvoicesTable.finalPaidDate]?.toString(),
            remainingAmount = remaining,
            notes = row[InvoicesTable.notes],
            createdAt = row[InvoicesTable.createdAt].toString()
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
            val id = InvoicesTable.insert {
                it[bookingId] = java.util.UUID.fromString(
                    request.bookingId
                )
                it[totalAmount] = request.totalAmount.toBigDecimal()
                it[depositAmount] = request.depositAmount.toBigDecimal()
                it[depositPaid] = false
                it[finalPaid] = false
                it[notes] = request.notes
                it[createdAt] = Instant.now()
            } get InvoicesTable.id

            getByBookingId(request.bookingId)!!
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