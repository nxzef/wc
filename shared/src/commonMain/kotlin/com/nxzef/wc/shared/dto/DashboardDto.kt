package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.DashboardStats
import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    val totalLeadsThisMonth: Int,
    val totalBookingsThisMonth: Int,
    val totalRevenueThisMonth: Double,
    val pendingPayments: Double,
    val openLeads: Int,
    val pendingDeliveries: Int,
    val leadsBySource: Map<String, Int>,
    val recentLeads: List<LeadDto>,
    val upcomingBookings: List<BookingDto>
)

fun DashboardStatsDto.toDomain(): DashboardStats {
    return DashboardStats(
        totalLeadsThisMonth = totalLeadsThisMonth,
        totalBookingsThisMonth = totalBookingsThisMonth,
        totalRevenueThisMonth = totalRevenueThisMonth,
        pendingPayments = pendingPayments,
        openLeads = openLeads,
        pendingDeliveries = pendingDeliveries,
        leadsBySource = leadsBySource,
        recentLeads = recentLeads.map { it.toDomain() },
        upcomingBookings = upcomingBookings.map { it.toDomain() }
    )
}

fun DashboardStats.toDto(): DashboardStatsDto {
    return DashboardStatsDto(
        totalLeadsThisMonth = totalLeadsThisMonth,
        totalBookingsThisMonth = totalBookingsThisMonth,
        totalRevenueThisMonth = totalRevenueThisMonth,
        pendingPayments = pendingPayments,
        openLeads = openLeads,
        pendingDeliveries = pendingDeliveries,
        leadsBySource = leadsBySource,
        recentLeads = recentLeads.map { it.toDto() },
        upcomingBookings = upcomingBookings.map { it.toDto() }
    )
}