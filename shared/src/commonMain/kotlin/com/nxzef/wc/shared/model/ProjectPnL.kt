package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectPnL(
    val bookingId: String,
    val eventType: String,
    val eventDate: String,
    val revenue: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val marginPercent: Double
)
