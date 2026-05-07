package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object ReceiptsTable : Table("receipts") {
    val id         = uuid("id").autoGenerate()
    val invoiceId  = uuid("invoice_id").references(InvoicesTable.id)
    val bookingId  = uuid("booking_id").references(BookingsTable.id)
    val receiptType = varchar("receipt_type", 20)
    val amount     = decimal("amount", precision = 12, scale = 2)
    val paidDate   = date("paid_date")
    val teamId     = uuid("team_id").references(TeamsTable.id).nullable()
    val createdAt  = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
