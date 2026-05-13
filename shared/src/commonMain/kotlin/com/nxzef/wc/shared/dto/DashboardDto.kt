package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.MonthlyRevenuePoint
import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    val totalLeadsThisMonth: Int,
    val totalBookingsThisMonth: Int,
    val totalRevenueThisMonth: Double,
    val averageOrderValue: Double,
    val conversionRate: Double,
    val revenueTrend: List<Double>,
    val pendingPayments: Double,
    val openLeads: Int,
    val pendingDeliveries: Int,
    val leadsBySource: Map<String, Int>,
    val recentLeads: List<LeadDto>,
    val upcomingBookings: List<BookingDto>,
    val totalRevenue: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val projectPnLList: List<ProjectPnLDto> = emptyList(),
    val currentMonthGoal: MonthlyGoalDto? = null,
    val currentMonthActualProfit: Double = 0.0,
    val isMonthBelowTarget: Boolean = false,
    val revenueTrendPercentage: Double = 0.0,
    val conversionRateTrendPercentage: Double = 0.0,
    val averageOrderValueTrendPercentage: Double = 0.0,
    val monthlyRevenue: List<MonthlyRevenuePoint> = emptyList(),
    val cpaThisMonth: Double = 0.0,
    val totalExpensesThisMonth: Double = 0.0
)

fun DashboardStatsDto.toDomain(): DashboardStats {
    return DashboardStats(
        totalLeadsThisMonth = totalLeadsThisMonth,
        totalBookingsThisMonth = totalBookingsThisMonth,
        totalRevenueThisMonth = totalRevenueThisMonth,
        averageOrderValue = averageOrderValue,
        conversionRate = conversionRate,
        revenueTrend = revenueTrend,
        pendingPayments = pendingPayments,
        openLeads = openLeads,
        pendingDeliveries = pendingDeliveries,
        leadsBySource = leadsBySource,
        recentLeads = recentLeads.map { it.toDomain() },
        upcomingBookings = upcomingBookings.map { it.toDomain() },
        totalRevenue = totalRevenue,
        totalCollected = totalCollected,
        totalPending = totalPending,
        projectPnLList = projectPnLList.map { it.toDomain() },
        currentMonthGoal = currentMonthGoal?.toDomain(),
        currentMonthActualProfit = currentMonthActualProfit,
        isMonthBelowTarget = isMonthBelowTarget,
        revenueTrendPercentage = revenueTrendPercentage,
        conversionRateTrendPercentage = conversionRateTrendPercentage,
        averageOrderValueTrendPercentage = averageOrderValueTrendPercentage,
        monthlyRevenue = monthlyRevenue,
        cpaThisMonth = cpaThisMonth,
        totalExpensesThisMonth = totalExpensesThisMonth
    )
}

fun DashboardStats.toDto(): DashboardStatsDto {
    return DashboardStatsDto(
        totalLeadsThisMonth = totalLeadsThisMonth,
        totalBookingsThisMonth = totalBookingsThisMonth,
        totalRevenueThisMonth = totalRevenueThisMonth,
        averageOrderValue = averageOrderValue,
        conversionRate = conversionRate,
        revenueTrend = revenueTrend,
        pendingPayments = pendingPayments,
        openLeads = openLeads,
        pendingDeliveries = pendingDeliveries,
        leadsBySource = leadsBySource,
        recentLeads = recentLeads.map { it.toDto() },
        upcomingBookings = upcomingBookings.map { it.toDto() },
        totalRevenue = totalRevenue,
        totalCollected = totalCollected,
        totalPending = totalPending,
        projectPnLList = projectPnLList.map { it.toDto() },
        currentMonthGoal = currentMonthGoal?.toDto(),
        currentMonthActualProfit = currentMonthActualProfit,
        isMonthBelowTarget = isMonthBelowTarget,
        revenueTrendPercentage = revenueTrendPercentage,
        conversionRateTrendPercentage = conversionRateTrendPercentage,
        averageOrderValueTrendPercentage = averageOrderValueTrendPercentage,
        monthlyRevenue = monthlyRevenue,
        cpaThisMonth = cpaThisMonth,
        totalExpensesThisMonth = totalExpensesThisMonth
    )
}
