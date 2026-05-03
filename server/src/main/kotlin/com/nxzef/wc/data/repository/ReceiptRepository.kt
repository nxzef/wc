package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.ReceiptsTable
import com.nxzef.wc.shared.model.Receipt
import com.nxzef.wc.shared.model.ReceiptType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ReceiptRepository {

    private fun rowToReceipt(row: ResultRow): Receipt = Receipt(
        id          = row[ReceiptsTable.id].toString(),
        invoiceId   = row[ReceiptsTable.invoiceId].toString(),
        bookingId   = row[ReceiptsTable.bookingId].toString(),
        receiptType = ReceiptType.valueOf(row[ReceiptsTable.receiptType]),
        amount      = row[ReceiptsTable.amount].toDouble(),
        paidDate    = row[ReceiptsTable.paidDate].toString(),
        createdAt   = row[ReceiptsTable.createdAt].toString()
    )

    fun create(invoiceId: String, bookingId: String, type: ReceiptType, amount: Double, paidDate: String): Receipt {
        return transaction {
            val newId = ReceiptsTable.insert {
                it[ReceiptsTable.invoiceId]   = UUID.fromString(invoiceId)
                it[ReceiptsTable.bookingId]   = UUID.fromString(bookingId)
                it[ReceiptsTable.receiptType] = type.name
                it[ReceiptsTable.amount]      = amount.toBigDecimal()
                it[ReceiptsTable.paidDate]    = LocalDate.parse(paidDate)
                it[ReceiptsTable.createdAt]   = Instant.now()
            } get ReceiptsTable.id

            ReceiptsTable.selectAll()
                .where { ReceiptsTable.id eq newId }
                .single()
                .let { rowToReceipt(it) }
        }
    }

    fun getByInvoiceId(invoiceId: String): List<Receipt> {
        return transaction {
            ReceiptsTable.selectAll()
                .where { ReceiptsTable.invoiceId eq UUID.fromString(invoiceId) }
                .orderBy(ReceiptsTable.createdAt, SortOrder.ASC)
                .map { rowToReceipt(it) }
        }
    }

    fun getByBookingId(bookingId: String): List<Receipt> {
        return transaction {
            ReceiptsTable.selectAll()
                .where { ReceiptsTable.bookingId eq UUID.fromString(bookingId) }
                .orderBy(ReceiptsTable.createdAt, SortOrder.ASC)
                .map { rowToReceipt(it) }
        }
    }

    fun getTotalCollected(): Double {
        return transaction {
            ReceiptsTable.selectAll()
                .sumOf { it[ReceiptsTable.amount].toDouble() }
        }
    }
}
