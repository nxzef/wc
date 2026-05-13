package com.nxzef.wc.data.repository

import com.nxzef.wc.data.db.tables.QuoteItemsTable
import com.nxzef.wc.data.db.tables.QuotesTable
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteItem
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

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
                QuoteItemsTable.quoteId eq UUID.fromString(quoteId)
            }
            .map { rowToQuoteItem(it) }
    }

    private fun rowToQuote(row: ResultRow): Quote {
        val quoteId = row[QuotesTable.id].toString()
        val items = getItemsForQuote(quoteId)
        val storedAmount = row[QuotesTable.totalAmount].toDouble()
        // If items exist they take precedence (legacy line-item quotes); otherwise use the
        // manually entered totalAmount column populated when sending a Canva PDF quote.
        val total = if (items.isNotEmpty()) items.sumOf { it.price } else storedAmount
        return Quote(
            id = quoteId,
            leadId = row[QuotesTable.leadId].toString(),
            createdBy = row[QuotesTable.createdBy].toString(),
            validUntil = row[QuotesTable.validUntil]?.toString(),
            notes = row[QuotesTable.notes],
            status = QuoteStatus.valueOf(row[QuotesTable.status]),
            items = items,
            totalAmount = total,
            fileName = row[QuotesTable.fileName],
            createdAt = row[QuotesTable.createdAt].toString()
        )
    }

    fun getByLeadId(leadId: String, teamId: String): List<Quote> {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return emptyList() }
        return transaction {
            QuotesTable
                .selectAll()
                .where { (QuotesTable.leadId eq UUID.fromString(leadId)) and (QuotesTable.teamId eq tUuid) }
                .map { rowToQuote(it) }
        }
    }

    fun getById(id: String, teamId: String): Quote? {
        val tUuid = try { UUID.fromString(teamId) } catch (_: Exception) { return null }
        return transaction {
            QuotesTable
                .selectAll()
                .where { (QuotesTable.id eq UUID.fromString(id)) and (QuotesTable.teamId eq tUuid) }
                .singleOrNull()
                ?.let { rowToQuote(it) }
        }
    }

    fun sendQuote(
        leadId: String,
        createdByUserId: String,
        fileName: String,
        totalAmount: Double,
        notes: String?,
        teamId: String
    ): Quote {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            val insertedId = QuotesTable.insert {
                it[QuotesTable.leadId] = UUID.fromString(leadId)
                it[QuotesTable.createdBy] = UUID.fromString(createdByUserId)
                it[QuotesTable.status] = QuoteStatus.SENT.name
                it[QuotesTable.fileName] = fileName
                it[QuotesTable.totalAmount] = totalAmount.toBigDecimal()
                it[QuotesTable.notes] = notes
                it[QuotesTable.teamId] = tUuid
                it[QuotesTable.createdAt] = Instant.now()
            }[QuotesTable.id]

            getById(insertedId.toString(), teamId)!!
        }
    }

    fun updateStatus(
        id: String,
        request: UpdateQuoteStatusRequest,
        teamId: String
    ): Quote? {
        val tUuid = UUID.fromString(teamId)
        return transaction {
            QuotesTable.update(
                { (QuotesTable.id eq UUID.fromString(id)) and (QuotesTable.teamId eq tUuid) }
            ) {
                it[status] = request.status.name
            }
            getById(id, teamId)
        }
    }
}
