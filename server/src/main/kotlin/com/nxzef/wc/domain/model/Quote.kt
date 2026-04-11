package com.nxzef.wc.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuoteStatus {
    DRAFT, SENT, ACCEPTED, REJECTED
}

@Serializable
data class QuoteItem(
    val id: String,
    val quoteId: String,
    val description: String,
    val price: Double,
    val createdAt: String
)

@Serializable
data class Quote(
    val id: String,
    val leadId: String,
    val createdBy: String,
    val validUntil: String? = null,
    val notes: String? = null,
    val status: QuoteStatus,
    val items: List<QuoteItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val createdAt: String
)

@Serializable
data class CreateQuoteItemRequest(
    val description: String,
    val price: Double
)

@Serializable
data class CreateQuoteRequest(
    val leadId: String,
    val validUntil: String? = null,
    val notes: String? = null,
    val items: List<CreateQuoteItemRequest>
)

@Serializable
data class UpdateQuoteStatusRequest(
    val status: QuoteStatus
)