package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.MonthlyGoal
import kotlinx.serialization.Serializable

@Serializable
data class MonthlyGoalDto(
    val id: String,
    val year: Int,
    val month: Int,
    val targetRevenue: Double,
    val targetProfit: Double
)

fun MonthlyGoalDto.toDomain(): MonthlyGoal = MonthlyGoal(
    id = id, year = year, month = month,
    targetRevenue = targetRevenue, targetProfit = targetProfit
)

fun MonthlyGoal.toDto(): MonthlyGoalDto = MonthlyGoalDto(
    id = id, year = year, month = month,
    targetRevenue = targetRevenue, targetProfit = targetProfit
)
