package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.QuoteItemsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.domain.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate

class QuoteRepository {

    private fun rowToQuoteItem(row: ResultRow): QuoteItem {
        return QuoteItem(
            id          = row[QuoteItemsTable.id].toString(),
            quoteId     = row[QuoteItemsTable.quoteId].toString(),
            description = row[QuoteItemsTable.description],
            price       = row[QuoteItemsTable.price].toDouble(),
            createdAt   = row[QuoteItemsTable.createdAt].toString()
        )
    }

    private fun getItemsForQuote(quoteId: String): List<QuoteItem> {
        return QuoteItemsTable
            .selectAll()
            .where {
                QuoteItemsTable.quoteId eq
                        java.util.UUID.fromString(quoteId)
            }
            .map { rowToQuoteItem(it) }
    }

    private fun rowToQuote(row: ResultRow): Quote {
        val quoteId = row[QuotesTable.id].toString()
        val items   = getItemsForQuote(quoteId)
        return Quote(
            id          = quoteId,
            leadId      = row[QuotesTable.leadId].toString(),
            createdBy   = row[QuotesTable.createdBy].toString(),
            validUntil  = row[QuotesTable.validUntil]?.toString(),
            notes       = row[QuotesTable.notes],
            status      = QuoteStatus.valueOf(row[QuotesTable.status]),
            items       = items,
            totalAmount = items.sumOf { it.price },
            createdAt   = row[QuotesTable.createdAt].toString()
        )
    }

    fun getByLeadId(leadId: String): List<Quote> {
        return transaction {
            QuotesTable
                .selectAll()
                .where {
                    QuotesTable.leadId eq
                            java.util.UUID.fromString(leadId)
                }
                .map { rowToQuote(it) }
        }
    }

    fun getById(id: String): Quote? {
        return transaction {
            QuotesTable
                .selectAll()
                .where {
                    QuotesTable.id eq
                            java.util.UUID.fromString(id)
                }
                .singleOrNull()
                ?.let { rowToQuote(it) }
        }
    }

    fun create(
        request: CreateQuoteRequest,
        createdByUserId: String
    ): Quote {
        return transaction {
            val quoteId = QuotesTable.insert {
                it[leadId]      = java.util.UUID.fromString(request.leadId)
                it[createdBy]   = java.util.UUID.fromString(createdByUserId)
                it[validUntil]  = request.validUntil?.let {
                        d -> LocalDate.parse(d)
                }
                it[notes]       = request.notes
                it[status]      = QuoteStatus.DRAFT.name
                it[createdAt]   = Instant.now()
            } get QuotesTable.id

            request.items.forEach { item ->
                QuoteItemsTable.insert {
                    it[QuoteItemsTable.quoteId]      = quoteId
                    it[QuoteItemsTable.description]  = item.description
                    it[QuoteItemsTable.price]        = item.price.toBigDecimal()
                    it[QuoteItemsTable.createdAt]    = Instant.now()
                }
            }

            getById(quoteId.toString())!!
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