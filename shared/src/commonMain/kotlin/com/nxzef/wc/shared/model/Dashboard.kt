package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
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
    val recentLeads: List<Lead>,
    val upcomingBookings: List<Booking>,
    val totalRevenue: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val projectPnLList: List<ProjectPnL> = emptyList(),
    val currentMonthGoal: MonthlyGoal? = null,
    val currentMonthActualProfit: Double = 0.0,
    val isMonthBelowTarget: Boolean = false
)