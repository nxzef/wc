package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.ProjectPnL
import kotlinx.serialization.Serializable

@Serializable
data class ProjectPnLDto(
    val bookingId: String,
    val eventType: String,
    val eventDate: String,
    val revenue: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val marginPercent: Double
)

fun ProjectPnLDto.toDomain(): ProjectPnL = ProjectPnL(
    bookingId = bookingId, eventType = eventType, eventDate = eventDate,
    revenue = revenue, totalExpenses = totalExpenses,
    netProfit = netProfit, marginPercent = marginPercent
)

fun ProjectPnL.toDto(): ProjectPnLDto = ProjectPnLDto(
    bookingId = bookingId, eventType = eventType, eventDate = eventDate,
    revenue = revenue, totalExpenses = totalExpenses,
    netProfit = netProfit, marginPercent = marginPercent
)
