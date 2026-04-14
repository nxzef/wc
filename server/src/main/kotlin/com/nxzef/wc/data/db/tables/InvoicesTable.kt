package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object InvoicesTable : Table("invoices") {
    val id = uuid("id").autoGenerate()
    val bookingId = uuid("booking_id").references(BookingsTable.id)
    val totalAmount = decimal("total_amount", precision = 10, scale = 2)
    val depositAmount = decimal("deposit_amount", precision = 10, scale = 2)
    val depositPaid = bool("deposit_paid").default(false)
    val depositPaidDate = date("deposit_paid_date").nullable()
    val finalPaid = bool("final_paid").default(false)
    val finalPaidDate = date("final_paid_date").nullable()
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}