package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.Invoice
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDto(
    val id: String,
    val bookingId: String,
    val totalAmount: Double,
    val depositAmount: Double,
    val depositPaid: Boolean,
    val depositPaidDate: String? = null,
    val finalPaid: Boolean,
    val finalPaidDate: String? = null,
    val remainingAmount: Double,
    val notes: String? = null,
    val createdAt: String
)

fun InvoiceDto.toDomain(): Invoice {
    return Invoice(
        id = id,
        bookingId = bookingId,
        totalAmount = totalAmount,
        depositAmount = depositAmount,
        depositPaid = depositPaid,
        depositPaidDate = depositPaidDate,
        finalPaid = finalPaid,
        finalPaidDate = finalPaidDate,
        remainingAmount = remainingAmount,
        notes = notes,
        createdAt = createdAt
    )
}

fun Invoice.toDto(): InvoiceDto {
    return InvoiceDto(
        id = id,
        bookingId = bookingId,
        totalAmount = totalAmount,
        depositAmount = depositAmount,
        depositPaid = depositPaid,
        depositPaidDate = depositPaidDate,
        finalPaid = finalPaid,
        finalPaidDate = finalPaidDate,
        remainingAmount = remainingAmount,
        notes = notes,
        createdAt = createdAt
    )
}
