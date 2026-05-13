package com.nxzef.wc.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentResponse(
    val invoice: InvoiceDto,
    val emailSent: Boolean
)
