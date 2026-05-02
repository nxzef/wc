package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteItem
import com.nxzef.wc.shared.model.QuoteStatus
import kotlinx.serialization.Serializable

@Serializable
data class QuoteItemDto(
    val id: String,
    val quoteId: String,
    val description: String,
    val price: Double,
    val createdAt: String
)

@Serializable
data class QuoteDto(
    val id: String,
    val leadId: String,
    val createdBy: String,
    val validUntil: String? = null,
    val notes: String? = null,
    val status: String,
    val items: List<QuoteItemDto> = emptyList(),
    val totalAmount: Double = 0.0,
    val fileName: String? = null,
    val createdAt: String
)

fun QuoteItemDto.toDomain(): QuoteItem {
    return QuoteItem(
        id = id,
        quoteId = quoteId,
        description = description,
        price = price,
        createdAt = createdAt
    )
}

fun QuoteItem.toDto(): QuoteItemDto {
    return QuoteItemDto(
        id = id,
        quoteId = quoteId,
        description = description,
        price = price,
        createdAt = createdAt
    )
}

fun QuoteDto.toDomain(): Quote {
    return Quote(
        id = id,
        leadId = leadId,
        createdBy = createdBy,
        validUntil = validUntil,
        notes = notes,
        status = try { QuoteStatus.valueOf(status) } catch (e: Exception) { QuoteStatus.DRAFT },
        items = items.map { it.toDomain() },
        totalAmount = totalAmount,
        fileName = fileName,
        createdAt = createdAt
    )
}

fun Quote.toDto(): QuoteDto {
    return QuoteDto(
        id = id,
        leadId = leadId,
        createdBy = createdBy,
        validUntil = validUntil,
        notes = notes,
        status = status.name,
        items = items.map { it.toDto() },
        totalAmount = totalAmount,
        fileName = fileName,
        createdAt = createdAt
    )
}
