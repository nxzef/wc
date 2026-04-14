package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Invoice(
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

@Serializable
data class CreateInvoiceRequest(
    val bookingId: String,
    val totalAmount: Double,
    val depositAmount: Double,
    val notes: String? = null
)

@Serializable
data class UpdatePaymentRequest(
    val depositPaid: Boolean? = null,
    val depositPaidDate: String? = null,
    val finalPaid: Boolean? = null,
    val finalPaidDate: String? = null,
    val notes: String? = null
)