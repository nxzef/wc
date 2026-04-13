package com.nxzef.wc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
    val totalLeadsThisMonth: Int,
    val totalBookingsThisMonth: Int,
    val totalRevenueThisMonth: Double,
    val pendingPayments: Double,
    val openLeads: Int,
    val pendingDeliveries: Int,
    val leadsBySource: Map<String, Int>,
    val recentLeads: List<Lead>,
    val upcomingBookings: List<Booking>
)