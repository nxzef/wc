package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Receipt
import com.nxzef.wc.shared.model.ReceiptType
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptDto(
    val id: String,
    val invoiceId: String,
    val bookingId: String,
    val receiptType: String,
    val amount: Double,
    val paidDate: String,
    val createdAt: String
)

fun ReceiptDto.toDomain(): Receipt = Receipt(
    id = id,
    invoiceId = invoiceId,
    bookingId = bookingId,
    receiptType = ReceiptType.valueOf(receiptType),
    amount = amount,
    paidDate = paidDate,
    createdAt = createdAt
)

fun Receipt.toDto(): ReceiptDto = ReceiptDto(
    id = id,
    invoiceId = invoiceId,
    bookingId = bookingId,
    receiptType = receiptType.name,
    amount = amount,
    paidDate = paidDate,
    createdAt = createdAt
)
