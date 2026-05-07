package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class MonthlyGoal(
    val id: String,
    val year: Int,
    val month: Int,
    val targetRevenue: Double,
    val targetProfit: Double
)

@Serializable
data class UpsertMonthlyGoalRequest(
    val year: Int,
    val month: Int,
    val targetRevenue: Double,
    val targetProfit: Double
)
