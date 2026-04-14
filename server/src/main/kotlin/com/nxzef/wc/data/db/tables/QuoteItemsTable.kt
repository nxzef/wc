package com.nxzef.wc.data.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object QuoteItemsTable : Table("quote_items") {
    val id = uuid("id").autoGenerate()
    val quoteId = uuid("quote_id").references(QuotesTable.id)
    val description = varchar("description", 500)
    val price = decimal("price", precision = 10, scale = 2)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}