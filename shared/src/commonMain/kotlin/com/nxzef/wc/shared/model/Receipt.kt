package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReceiptType { ADVANCE, FINAL }

@Serializable
data class Receipt(
    val id: String,
    val invoiceId: String,
    val bookingId: String,
    val receiptType: ReceiptType,
    val amount: Double,
    val paidDate: String,
    val createdAt: String,
    val emailSent: Boolean = false
)
