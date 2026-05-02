package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.QuoteItemsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteItem
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class QuoteRepository {

    private fun rowToQuoteItem(row: ResultRow): QuoteItem {
        return QuoteItem(
            id = row[QuoteItemsTable.id].toString(),
            quoteId = row[QuoteItemsTable.quoteId].toString(),
            description = row[QuoteItemsTable.description],
            price = row[QuoteItemsTable.price].toDouble(),
            createdAt = row[QuoteItemsTable.createdAt].toString()
        )
    }

    private fun getItemsForQuote(quoteId: String): List<QuoteItem> {
        return QuoteItemsTable
            .selectAll()
            .where {
                QuoteItemsTable.quoteId eq java.util.UUID.fromString(quoteId)
            }
            .map { rowToQuoteItem(it) }
    }

    private fun rowToQuote(row: ResultRow): Quote {
        val quoteId = row[QuotesTable.id].toString()
        val items = getItemsForQuote(quoteId)
        return Quote(
            id = quoteId,
            leadId = row[QuotesTable.leadId].toString(),
            createdBy = row[QuotesTable.createdBy].toString(),
            validUntil = row[QuotesTable.validUntil]?.toString(),
            notes = row[QuotesTable.notes],
            status = QuoteStatus.valueOf(row[QuotesTable.status]),
            items = items,
            totalAmount = items.sumOf { it.price },
            fileName = row[QuotesTable.fileName],
            createdAt = row[QuotesTable.createdAt].toString()
        )
    }

    fun getByLeadId(leadId: String): List<Quote> {
        return transaction {
            QuotesTable
                .selectAll()
                .where { QuotesTable.leadId eq java.util.UUID.fromString(leadId) }
                .map { rowToQuote(it) }
        }
    }

    fun getById(id: String): Quote? {
        return transaction {
            QuotesTable
                .selectAll()
                .where { QuotesTable.id eq java.util.UUID.fromString(id) }
                .singleOrNull()
                ?.let { rowToQuote(it) }
        }
    }

    fun sendQuote(
        leadId: String,
        createdByUserId: String,
        fileName: String
    ): Quote {
        return transaction {
            val insertedId = QuotesTable.insert {
                it[QuotesTable.leadId] = java.util.UUID.fromString(leadId)
                it[QuotesTable.createdBy] = java.util.UUID.fromString(createdByUserId)
                it[QuotesTable.status] = QuoteStatus.SENT.name
                it[QuotesTable.fileName] = fileName
                it[QuotesTable.createdAt] = Instant.now()
            }[QuotesTable.id]

            getById(insertedId.toString())!!
        }
    }

    fun updateStatus(
        id: String,
        request: UpdateQuoteStatusRequest
    ): Quote? {
        return transaction {
            QuotesTable.update(
                { QuotesTable.id eq java.util.UUID.fromString(id) }
            ) {
                it[status] = request.status.name
            }
            getById(id)
        }
    }
}
